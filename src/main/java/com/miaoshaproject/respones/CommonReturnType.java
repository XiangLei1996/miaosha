package com.miaoshaproject.respones;

/**
 * Author: XiangL
 * Date: 2019/6/12 17:21
 * Version 1.0
 */
public class CommonReturnType {
    //表明对应请求的返回处理结果 "success" 或 "fail"
    private String status;

    //若status为success则返回前端需要的JSON数据
    //若为fail，则data内使用通用的错误码格式
    private Object data;


    /**
     * 通过重载方式实现通用返回对象的创建定义
     * 请求处理正常是调用方法1
     * 请求处理错误时调用方法2
     * @param data
     * @return
     */
    public static CommonReturnType create(Object data){
        return CommonReturnType.create(data, "success");
    }

    public static CommonReturnType create(Object data, String status){
        CommonReturnType type = new CommonReturnType();

        type.data = data;
        type.status = status;
//        System.out.println(type.status);
        return type;
    }

    public String getStatus() {
        return status;
    }

    public Object getData() {
        return data;
    }

}
