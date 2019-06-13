package com.miaoshaproject.controller;

import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.error.EmBusinessError;
import com.miaoshaproject.model.UserModel;
import com.miaoshaproject.model.viewobject.UserVO;
import com.miaoshaproject.respones.CommonReturnType;
import com.miaoshaproject.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Author: XiangL
 * Date: 2019/6/12 12:44
 * Version 1.0
 */
@Controller("user")
@RequestMapping("/user")
public class UserController {

    @Autowired
    UserService userService;

    //自动注入的httpServletRequest时，单例模式不会影响多并发访问，
    //因为通过Bean注入的该对象，其本质是一个proxy，里面有一个ThreadLocal
    @Autowired
    HttpServletRequest httpServletRequest;

    /**
     * 手机注册，发送Otp短信（即手机验证码）
     * @param telephone 注册的手机号
     * @return
     */
    @RequestMapping("/getotp")
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

    //定义exceptionHandler解决未被controller层吸收的exception
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Object handlerException(HttpServletResponse response, Exception ex){

        Map<String, Object> responseData = new HashMap<>();

        if(ex instanceof  BusinessException){
            //强转
            BusinessException businessException = (BusinessException)ex;

            responseData.put("errCode", businessException.getErrCode());
            responseData.put("errMsg", businessException.getErrMsg());
            return CommonReturnType.create(responseData, "fail");
        }else{
            responseData.put("errCode", EmBusinessError.UNKNOW_ERROR.getErrCode());
            responseData.put("errMsg", EmBusinessError.UNKNOW_ERROR.getErrMsg());
        }

        return CommonReturnType.create(responseData, "fail");
    }
}
