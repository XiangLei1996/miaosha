package com.miaoshaproject.controller;

import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.error.EmBusinessError;
import com.miaoshaproject.model.UserModel;
import com.miaoshaproject.model.viewobject.UserVO;
import com.miaoshaproject.respones.CommonReturnType;
import com.miaoshaproject.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import sun.misc.BASE64Encoder;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

/**
 * Author: XiangL
 * Date: 2019/6/12 12:44
 * Version 1.0
 *
 * 继承BaseController
 * BaseController中定义了自定义的处理Exception的方法和自定义的请求头类型
 * 即用父类管理所有Controller的通用资源
 *
 * CrossOrigin注解用来处理浏览前拦截ajax跨域访问的问题因此，但仍然没办法解决ajax无法共享session
 * 所以需要指定allowCredentials和allowHeaders。
 * 因为，DEFAULT_ALLOWED_HEADERS 允许跨域传输所有header参数，将用于使用token放入header，用作session共享的跨域请求
 */
@Controller("user")
@RequestMapping("/user")
@CrossOrigin(allowCredentials = "true", allowedHeaders = "*")
public class UserController extends BaseController{

    @Autowired
    private UserService userService;

    //自动注入的httpServletRequest时，单例模式不会影响多并发访问，
    //因为通过Bean注入的该对象，其本质是一个proxy，里面有一个ThreadLocal
    @Autowired
    private HttpServletRequest httpServletRequest;

    @RequestMapping(path = {"/login"}, method = {RequestMethod.POST}, consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType login(@RequestParam("telephone") String telephone,
                                  @RequestParam("password") String password) throws BusinessException, UnsupportedEncodingException, NoSuchAlgorithmException {
        //入参校验
        if(org.apache.commons.lang3.StringUtils.isEmpty(telephone)
                || StringUtils.isEmpty(password)){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "手机号或密码不能为空");
        }

        //用户登录服务，注意，传入的是加密的密码
        UserModel userModel = userService.validateLogin(telephone, this.EncodeByMd5(password));

        //将登录凭证加入到用户登录成功的session内
        this.httpServletRequest.getSession().setAttribute("IS_LOGIN", true);
        this.httpServletRequest.getSession().setAttribute("LOGIN_USER", userModel);

        return CommonReturnType.create(null);
    }

    /**
     * 用户注册入口
     * @param telephone
     * @param otpCode
     * @param gender
     * @param name
     * @param age
     * @param password
     * @return
     * @throws BusinessException
     * @throws NoSuchAlgorithmException
     * @throws UnsupportedEncodingException
     */
    @RequestMapping(path = {"/register"}, method = {RequestMethod.POST}, consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType register(@RequestParam("telephone") String telephone,
                                     @RequestParam("otpCode") String otpCode,
                                     @RequestParam("gender") Integer gender,
                                     @RequestParam("name") String name,
                                     @RequestParam("age") Integer age,
                                     @RequestParam("password") String password) throws BusinessException, NoSuchAlgorithmException, UnsupportedEncodingException{
        //验证手机号和对应的otpCode是否符合；符合才进行相关注册流程;注意这里调用的是this. 因为httpServletRequest自动注入的特点
        String inSessionOptCode = (String) this.httpServletRequest.getSession().getAttribute(telephone);
        if(!com.alibaba.druid.util.StringUtils.equals(inSessionOptCode, otpCode)){
            //可以直接使用工具类，里面已经编写了判空等情况，避免重复造轮子？
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "短信验证码不符合");
        }

        //用户注册流程
        UserModel userModel = new UserModel();
        userModel.setTelephone(telephone);
        userModel.setAge(age);
        userModel.setGender(new Byte(String.valueOf(gender.intValue())));
        userModel.setName(name);
        userModel.setRegisterMode("byphone");
        userModel.setEncrptPassword(this.EncodeByMd5(password));

        userService.register(userModel);

        return CommonReturnType.create(null);
    }

    public String EncodeByMd5(String str) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        //确定一个计算方法
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        BASE64Encoder base64Encoder = new BASE64Encoder();

        //加密字符串
        String newstr = base64Encoder.encode(md5.digest(str.getBytes("utf-8")));

        return newstr;
    }

    /**
     * 手机注册，发送Otp短信（即手机验证码）
     * consumes为指定能处理的请求数据格式，对应request中的Content-Type
     * @param telephone 注册的手机号
     * @return
     */
    @RequestMapping(path = {"/getotp"}, method = {RequestMethod.POST}, consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType getOtp(@RequestParam("telephone") String telephone){
        //按照一定规则，生成Otp验证码
        Random random = new Random();
        int randomInt = 10000 + random.nextInt(99999);//[0,99999) + 10000
        String otpCode = String.valueOf(randomInt);

        //将Otp验证码与手机号关联---kv堆---分布式采用redis---目前暂时借助session
        httpServletRequest.getSession().setAttribute(telephone, otpCode);

        //将OTP验证码通过短信通道发送给用户，（省略，因为涉及短信通道流程，购买第三方通道，将验证码post给相应手机用户即可）
        System.out.println("telephone:" + telephone +"& otpCode:" + otpCode);

        return CommonReturnType.create(null);
    }

    @RequestMapping("/get")
    @ResponseBody
    public CommonReturnType getUser(@RequestParam("id") int id) throws BusinessException{
        //调用对应的Service服务获取对应id的用户对象，返回给前端
        UserModel userModel = userService.getUserById(id);

        //如果获取的对应用户信息不存在
        if(userModel == null){
            //抛出自定义异常(通过传入枚举类构建)，同时方法处要声明可能抛出的异常。
            throw new BusinessException(EmBusinessError.USER_NOT_EXIST);
        }

        //前端视图所需对象
        UserVO userVO = convertFromModel(userModel);

        //返回通用对象
        return CommonReturnType.create(userVO);
    }

    /**
     * 封装将 核心领域模型对象UserModel转换为返回给供前端使用的视图模型UserVO的方法
     * @param userModel 核心领域模型对象
     * @return
     */
    private UserVO convertFromModel(UserModel userModel){
        if(userModel == null){
            return null;
        }

        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(userModel, userVO);

        return userVO;
    }

}
