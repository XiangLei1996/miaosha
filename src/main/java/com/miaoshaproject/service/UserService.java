package com.miaoshaproject.service;

import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.model.UserModel;

/**
 * Author: XiangL
 * Date: 2019/6/12 12:46
 * Version 1.0
 */
public interface UserService {

    public UserModel getUserById(int id);

    //通过缓存获取用户对象
    UserModel getUserByIdInCache(int id);


    UserModel validateLogin(String telephone, String encrptPassword) throws BusinessException;

    void register(UserModel userModel) throws BusinessException;
}
