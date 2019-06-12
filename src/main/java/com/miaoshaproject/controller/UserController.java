package com.miaoshaproject.controller;

import com.miaoshaproject.model.UserModel;
import com.miaoshaproject.model.viewobject.UserVO;
import com.miaoshaproject.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

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

    @RequestMapping("/get")
    @ResponseBody
    public UserVO getUser(@RequestParam("id") int id){
        //调用对应的Service服务获取对应id的用户对象，返回给前端
        UserModel userModel = userService.getUserById(id);

        return convertFromModel(userModel);
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
}
