package com.miaoshaproject.controller;

import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.error.EmBusinessError;
import com.miaoshaproject.model.UserModel;
import com.miaoshaproject.mq.MqProducer;
import com.miaoshaproject.respones.CommonReturnType;
import com.miaoshaproject.service.ItemService;
import com.miaoshaproject.service.OrderService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

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

    //封装下单请求
    @RequestMapping(path = {"/createorder"}, method = {RequestMethod.POST}, consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType createOrder(@RequestParam("itemId") Integer itemId,
                                        @RequestParam("amount") Integer amount,
                                        @RequestParam(value = "promoId", required = false) Integer promoId) throws BusinessException {
        //判断用户是否登录，使用token方法后注释
        //Boolean isLogin = (Boolean)httpServletRequest.getSession().getAttribute("IS_LOGIN");
//        if(isLogin == null || !isLogin.booleanValue()){
//            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "请先登录用户");
//        }
        //获取用户登录信息--注意需要强转类型---值是在用户登录存储在对应的request的session里的
        //UserModel userModel = (UserModel) httpServletRequest.getSession().getAttribute("LOGIN_USER");

        //使用token验证登录
        String token = httpServletRequest.getParameterMap().get("token")[0];
        if(StringUtils.isEmpty(token)){
            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN, "请先登录用户");
        }
        //获取用户登录信息也通过token去redis中寻找
        UserModel userModel = (UserModel) redisTemplate.opsForValue().get(token);
        if(userModel == null){
            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN, "请先登录用户");
        }

        //此时使用异步发送事务型消息，不通过下面的普通流程创建订单
        //OrderModel orderModel = orderService.createOrder(userModel.getId(), itemId, amount, promoId);

        //通过库存售罄标识是否存在，来判断库存是否已售罄，售罄则直接返回失败
        if(redisTemplate.hasKey("promo_item_stock_invalid_"+itemId)){
            throw new BusinessException(EmBusinessError.STOCK_NOT_ENOUGH);
        }

        //先加入库存流水init状态，然后再去完成对应的下单事务型消息机制
        String stockLogId = itemService.initStockLog(itemId, amount);

        //这里需要false才返回下单失败
        if(!mqProducer.transactionAsyncReduceStock(userModel.getId(), itemId, amount, promoId, stockLogId)){
            throw new BusinessException(EmBusinessError.UNKNOW_ERROR, "下单失败");
        }

        return CommonReturnType.create(null);
    }
}
