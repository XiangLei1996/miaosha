package com.miaoshaproject.controller;

import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.error.EmBusinessError;
import com.miaoshaproject.model.UserModel;
import com.miaoshaproject.mq.MqProducer;
import com.miaoshaproject.respones.CommonReturnType;
import com.miaoshaproject.service.ItemService;
import com.miaoshaproject.service.OrderService;
import com.miaoshaproject.service.PromoService;
import com.miaoshaproject.util.CodeUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Author: XiangL
 * Date: 2019/6/15 12:14
 * Version 1.0
 * 注解@RequestParam(value = "promoId", required = false)中required代表可以不加参数
 * 若不写，则使用@RequestParam后要求必须传入对应的参数
 */
@Controller("order")
@RequestMapping("/order")
@CrossOrigin(allowCredentials = "true", allowedHeaders = "*")
public class OrderController extends BaseController{

    @Autowired
    private OrderService orderService;

    @Autowired
    private HttpServletRequest httpServletRequest;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private MqProducer mqProducer;

    @Autowired
    private ItemService itemService;

    @Autowired
    private PromoService promoService;

    //用于队列泄洪;利用了线程池实现
    private ExecutorService executorService;

    //要知道@PostConstruct的作用
    @PostConstruct
    public void init(){
        //生成固定大小的线程池
        executorService = Executors.newFixedThreadPool(20);
    }


    //生成验证码
    @RequestMapping(path = {"/generateverifycode"}, method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public void generateVerifyCode(HttpServletResponse response) throws BusinessException, IOException {
        String token = httpServletRequest.getParameterMap().get("token")[0];
        if(StringUtils.isEmpty(token)){
            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN,"用户还未登录，不能生成验证码");
        }

        UserModel userModel = (UserModel) redisTemplate.opsForValue().get(token);
        if(userModel == null){
            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN, "请先登录用户");
        }

        Map<String, Object> map = CodeUtil.generateCodeAndPic();

        //并将该验证码与用户做一个绑定
        redisTemplate.opsForValue().set("verify_code_"+userModel.getId(), map.get("code"));
        //设置验证码有效期
        redisTemplate.expire("verify_code_"+userModel.getId(), 10, TimeUnit.MINUTES);

        //将生成的图片写到httpServletResponse中，返回给前端
        ImageIO.write((RenderedImage) map.get("codePic"), "jpeg", response.getOutputStream());
    }


    //生成秒杀令牌
    @RequestMapping(path = {"/generatetoken"}, method = {RequestMethod.POST}, consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType generatetoken(@RequestParam("itemId") Integer itemId,
                                          @RequestParam("promoId") Integer promoId,
                                          @RequestParam("verifyCode") String verifyCode) throws BusinessException{
        //根据token获取用户信息
        String token = httpServletRequest.getParameterMap().get("token")[0];
        if(StringUtils.isEmpty(token)){
            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN, "请先登录用户");
        }
        //获取用户登录信息也通过token去redis中寻找
        UserModel userModel = (UserModel) redisTemplate.opsForValue().get(token);
        if(userModel == null){
            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN, "请先登录用户");
        }

        //通过verifycode验证验证码的有效性
        String redisVerifyCode = (String) redisTemplate.opsForValue().get("verify_code_"+userModel.getId());
        if(StringUtils.isEmpty(redisVerifyCode)){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "请填写验证码");
        }
        //equalsIgnoreCase无视大小写区别
        if(!redisVerifyCode.equalsIgnoreCase(verifyCode)){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "验证码错误");
        }


        //获取秒杀访问令牌
        String promoToken = promoService.generateSecondKillToken(promoId, itemId, userModel.getId());

        if(promoToken == null){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "生成令牌失败");
        }

        //返回对应的结果
        return CommonReturnType.create(promoToken);
    }


    //封装下单请求
    @RequestMapping(path = {"/createorder"}, method = {RequestMethod.POST}, consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType createOrder(@RequestParam("itemId") Integer itemId,
                                        @RequestParam("amount") Integer amount,
                                        @RequestParam(value = "promoId", required = false) Integer promoId,
                                        @RequestParam(value = "promoToken", required = false) String promoToken) throws BusinessException {
        //判断用户是否登录，使用token方法后注释
        //Boolean isLogin = (Boolean)httpServletRequest.getSession().getAttribute("IS_LOGIN");
//        if(isLogin == null || !isLogin.booleanValue()){
//            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "请先登录用户");
//        }
        //获取用户登录信息--注意需要强转类型---值是在用户登录存储在对应的request的session里的
        //UserModel userModel = (UserModel) httpServletRequest.getSession().getAttribute("LOGIN_USER");

        String token = httpServletRequest.getParameterMap().get("token")[0];
        if(StringUtils.isEmpty(token)){
            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN, "请先登录用户");
        }
        //获取用户登录信息也通过token去redis中寻找
        UserModel userModel = (UserModel) redisTemplate.opsForValue().get(token);
        if(userModel == null){
            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN, "请先登录用户");
        }


        //校验秒杀令牌是否正确
        if(promoId != null){
            String inRedisPromoToken = (String) redisTemplate.opsForValue().get("promo_token_"+promoId+"_userid_"+userModel.getId()+"_itemid_"+itemId);
            if(inRedisPromoToken == null){
                throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "秒杀令牌校验失败");
            }
            if(!org.apache.commons.lang3.StringUtils.equals(promoToken, inRedisPromoToken)){
                throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "秒杀令牌校验失败");
            }
        }

        //此时使用异步发送事务型消息，不通过下面的普通流程创建订单
        //OrderModel orderModel = orderService.createOrder(userModel.getId(), itemId, amount, promoId);


        //同步调用线程池的submit方法；使用实现Callable接口重写call方法的方式创建线程
        //第三种方法创建的线程，存在返回值。必须使用Future进行包装

        //拥塞窗口为20的等待队列，用来队列泄洪
        Future<Object> future = executorService.submit(new Callable<Object>() {
            @Override
            public Object call() throws Exception {

                //先加入库存流水init状态，然后再去完成对应的下单事务型消息机制
                String stockLogId = itemService.initStockLog(itemId, amount);

                //这里需要false才返回下单失败
                if(!mqProducer.transactionAsyncReduceStock(userModel.getId(), itemId, amount, promoId, stockLogId)){
                    throw new BusinessException(EmBusinessError.UNKNOW_ERROR, "下单失败");
                }

                return null;
            }
        });

        try {
            //等待返回，返回后才会执行后序给前端返回信息
            future.get();
        } catch (InterruptedException e) {
            throw new BusinessException(EmBusinessError.UNKNOW_ERROR);
        } catch (ExecutionException e) {
            throw new BusinessException(EmBusinessError.UNKNOW_ERROR);
        }

        return CommonReturnType.create(null);
    }
}
