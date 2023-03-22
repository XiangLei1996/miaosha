package com.miaoshaproject.controller;

/**
 * Author: XiangL
 * Date: 2019/6/13 11:54
 * Version 1.0
 *
 * 修复bug，
 * 1.HttpServletRequest 不下心写成了 HttpServletResponse
 * 2.BaseController的处理异常方法中，businessException中的方法没有重写，导致无法获取对应的值
 */
public class BaseController {

    public static final String CONTENT_TYPE_FORMED = "application/x-www-form-urlencoded";

    //定义exceptionHandler解决未被controller层吸收的exception
//    @ExceptionHandler(Exception.class)
//    @ResponseStatus(HttpStatus.OK)
//    @ResponseBody
//    public Object handlerException(HttpServletRequest request, Exception ex){
//
//        Map<String, Object> responseData = new HashMap<>();
//
//        if(ex instanceof BusinessException){
//            //强转
//            BusinessException businessException = (BusinessException)ex;
//
//
//            responseData.put("errCode", businessException.getErrCode());
//            responseData.put("errMsg", businessException.getErrMsg());
//        }else{
//            responseData.put("errCode", EmBusinessError.UNKNOW_ERROR.getErrCode());
//            responseData.put("errMsg", EmBusinessError.UNKNOW_ERROR.getErrMsg());
//        }
//
//        return CommonReturnType.create(responseData, "fail");
//    }
}
