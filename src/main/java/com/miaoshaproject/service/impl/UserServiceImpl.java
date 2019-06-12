package com.miaoshaproject.service.impl;

import com.miaoshaproject.DAO.UserDOMapper;
import com.miaoshaproject.DAO.UserPasswordDOMapper;
import com.miaoshaproject.DO.UserDO;
import com.miaoshaproject.DO.UserPasswordDO;
import com.miaoshaproject.model.UserModel;
import com.miaoshaproject.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Author: XiangL
 * Date: 2019/6/12 12:47
 * Version 1.0
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserDOMapper userDOMapper;

    @Autowired
    UserPasswordDOMapper userPasswordDOMapper;

    @Override
    public UserModel getUserById(int id) {
        UserDO userDO = userDOMapper.selectByPrimaryKey(id);
        UserPasswordDO userPasswordDO = userPasswordDOMapper.selectByUserId(id);

        return convertFromDataObject(userDO, userPasswordDO);
    }

    /**
     * 封装数据库层模型UserDo和UserPasswordDo转换为核心领域模型对象 UserModel的方法
     * @param userDO
     * @param userPasswordDO
     * @return
     */
    private UserModel convertFromDataObject(UserDO userDO, UserPasswordDO userPasswordDO){
        if(userDO == null){
            return null;
        }

        UserModel userModel = new UserModel();
        //使用copyProperties要求两个模型的属性字段名和类型一致
        BeanUtils.copyProperties(userDO, userModel);

        if(userPasswordDO != null){
            userModel.setEncrptPassword(userPasswordDO.getEncrptPassword());
        }

        return userModel;
    }
}
