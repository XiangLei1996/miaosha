package com.miaoshaproject.controller;

import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.error.EmBusinessError;
import com.miaoshaproject.model.OrderModel;
import com.miaoshaproject.model.UserModel;
import com.miaoshaproject.respones.CommonReturnType;
import com.miaoshaproject.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
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

    //封装下单请求
    @RequestMapping(path = {"/createorder"}, method = {RequestMethod.POST}, consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType createOrder(@RequestParam("itemId") Integer itemId,
                                        @RequestParam("amount") Integer amount,
                                        @RequestParam(value = "promoId", required = false) Integer promoId) throws BusinessException {
        //判断用户是否登录
        Boolean isLogin = (Boolean)httpServletRequest.getSession().getAttribute("IS_LOGIN");
        if(isLogin == null || !isLogin.booleanValue()){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "请先登录用户");
        }

        //获取用户登录信息--注意需要强转类型---值是在用户登录存储在对应的request的session里的
        UserModel userModel = (UserModel) httpServletRequest.getSession().getAttribute("LOGIN_USER");

        OrderModel orderModel = orderService.createOrder(userModel.getId(), itemId, amount, promoId);

        return CommonReturnType.create(null);
    }
}
