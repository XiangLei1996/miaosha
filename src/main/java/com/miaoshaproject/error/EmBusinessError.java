package com.miaoshaproject.error;

/**
 * Author: XiangL
 * Date: 2019/6/12 17:34
 * Version 1.0
 *
 * 定义枚举类，用来判断错误属于那种类型
 * 类中通过枚举指定多种不同类型的参数
 *
 * 注意：枚举类型之间使用 逗号 相分隔
 *
 * commonError的具体类EmBusinessError也要重写getErrMsg等方法以供外部获取错误信息
 */
public enum EmBusinessError implements CommonError {

    //通用错误类型10001
    PARAMETER_VALIDATION_ERROR(10001, "参数不合法"),

    //未知错误10002
    UNKNOW_ERROR(10002, "未知错误"),

    //20000开头为用户信息相关错误定义
    USER_NOT_EXIST(20001, "用户不存在"),
    USER_LOGIN_FIAL(20002, "用户手机号或密码不正确"),
    USER_NOT_LOGIN(20003, "用户还未登录"),

    //30000开头为交易信息错误
    STOCK_NOT_ENOUGH(300001, "库存不足"),
    MQ_SEND_FAIL(30002, "库存异步消息失败");

    private EmBusinessError(int errCode, String errMsg){
        this.errCode = errCode;
        this.errMsg = errMsg;
    }

    private int errCode;
    private String errMsg;

    @Override
    public int getErrCode() {
        return this.errCode;
    }

    @Override
    public String getErrMsg() {
        return this.errMsg;
    }

    /**
     * 用于可以自己传入参数来覆盖原有errMsg，自己设定errMsg
     * @param errMsg
     * @return
     */
    @Override
    public CommonError setErrMsg(String errMsg) {
        this.errMsg = errMsg;
        return this;
    }
}
