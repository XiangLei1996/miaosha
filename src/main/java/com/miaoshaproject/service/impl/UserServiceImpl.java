package com.miaoshaproject.service.impl;

import com.alibaba.druid.util.StringUtils;
import com.miaoshaproject.DAO.UserDOMapper;
import com.miaoshaproject.DAO.UserPasswordDOMapper;
import com.miaoshaproject.DO.UserDO;
import com.miaoshaproject.DO.UserPasswordDO;
import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.error.EmBusinessError;
import com.miaoshaproject.model.UserModel;
import com.miaoshaproject.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
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

    //通过id获取用户服务
    @Override
    public UserModel getUserById(int id) {
        UserDO userDO = userDOMapper.selectByPrimaryKey(id);
        UserPasswordDO userPasswordDO = userPasswordDOMapper.selectByUserId(id);

        return convertFromDataObject(userDO, userPasswordDO);
    }

    @Override
    public UserModel validateLogin(String telephone, String encrptPassword) throws BusinessException{
        //通过用户的手机获取用户对象
        UserDO userDO = userDOMapper.selectByTelephone(telephone);
        if(userDO == null){
            throw new BusinessException(EmBusinessError.USER_LOGIN_FIAL);
        }
        UserPasswordDO userPasswordDO = userPasswordDOMapper.selectByUserId(userDO.getId());

        if(!StringUtils.equals(encrptPassword, userPasswordDO.getEncrptPassword())){
            throw new BusinessException(EmBusinessError.USER_LOGIN_FIAL);
        }

        UserModel userModel = convertFromDataObject(userDO, userPasswordDO);
        return userModel;
    }

    //注册服务
    @Override
    public void register(UserModel userModel) throws BusinessException{
        if(userModel == null){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR);
        }
        if(StringUtils.isEmpty(userModel.getName())
                || userModel.getGender() == null
                || userModel.getAge() == null
                || StringUtils.isEmpty(userModel.getTelephone())){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "参数不合法");
        }


        //调用Model转换为dataObject的方法
        UserDO userDO = convertFromModel(userModel);
        try{
            userDOMapper.insertSelective(userDO);
        }catch (DuplicateKeyException ex){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "手机号已注册");
        }

        //insertSelective方法插入后，自动为没有传入参数userModel的主键赋值？
        //所以这里才能设置？
        userModel.setId(userDO.getId());

        //密码存放到特殊的单表中（封装该部分方法，以便复用），理论上还需要通过MD5算法加密
        UserPasswordDO userPasswordDO = convertPasswordFromModel(userModel);
        userPasswordDOMapper.insertSelective(userPasswordDO);

        return;
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

    private UserDO convertFromModel(UserModel userModel){
        if(userModel == null){
            return null;
        }

        UserDO userDO = new UserDO();

        BeanUtils.copyProperties(userModel, userDO);

        return userDO;
    }

    private UserPasswordDO convertPasswordFromModel(UserModel userModel){
        if(userModel == null){
            return null;
        }

        UserPasswordDO userPasswordDO = new UserPasswordDO();

        //理论上应当编写一个MD5算法，然后对pwd+随机UUID进行加密，再存储到表中
        userPasswordDO.setEncrptPassword(userModel.getEncrptPassword());
        userPasswordDO.setUserId(userModel.getId());

        return userPasswordDO;
    }
}
