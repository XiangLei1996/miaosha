package com.miaoshaproject.error;

/**
 * Author: XiangL
 * Date: 2019/6/12 17:32
 * Version 1.0
 */
public interface CommonError {

    public int getErrCode();

    public String getErrMsg();

    public CommonError setErrMsg(String errMsg);
}
