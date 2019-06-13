package com.miaoshaproject.error;

/**
 * Author: XiangL
 * Date: 2019/6/12 17:42
 * Version 1.0
 *
 * 异常类，用于请求处理时发现错误抛出
 * 特定的业务异常
 * 以便其他地方捕获并根据业务异常的信息判断错误
 *
 * 注意：这是一种设计模式
 *
 * BusinessException和EmBusinessError都实现了CommonError的方法
 * 以至于都拥有errCode和errMsg的组装定义
 */
//包装器业务类异常实现
public class BusinessException extends Exception implements CommonError{

    private CommonError commonError;

    //直接接手EmBusinessError的传参，用于构造业务异常
    public BusinessException(CommonError commonError){
        super();//需要调用super()，因为继承了Exception，而Exception中有一些初始化步骤
        this.commonError = commonError;
    }

    //接收自定义errMsg的方式构造业务异常
    public BusinessException(CommonError commonError, String errMsg){
        super();
        this.commonError = commonError;
        //通过二次改写errMsg来实现自定义errMsg
        this.commonError.setErrMsg(errMsg);
    }

    @Override
    public int getErrCode() {
        return 0;
    }

    @Override
    public String getErrMsg() {
        return null;
    }

    @Override
    public CommonError setErrMsg(String errMsg) {
        this.commonError.setErrMsg(errMsg);
        return this;
    }
}
