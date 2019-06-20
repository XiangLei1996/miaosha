package com.miaoshaproject.model;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * Author: XiangL
 * Date: 2019/6/12 12:56
 * Version 1.0
 *
 * 要存入Redis必须实现序列化
 * 1.可以实现序列化接口
 * 2.可以修改redis中的序列化方式，修改为json
 */
public class UserModel implements Serializable {
    private Integer id;
    //引入注释，表示对应字符串不能为空和null，里面参数为validator校验时发现错误后会给出的信息
    @NotBlank(message="用户名不能为空")
    private String name;

    @NotNull(message = "需要填写性别")
    private Byte gender;

    @NotNull(message = "需要填写年龄")
    @Min(value = 0,message = "年龄必须大于0")
    @Max(value = 150, message = "年龄必须小于150岁")
    private Integer age;

    @NotBlank(message = "手机号不能为空")
    private String telephone;
    private String registerMode;
    private String thirdPartyId;

    @NotBlank(message = "密码不能为空")
    private String encrptPassword;

    public String getEncrptPassword() {
        return encrptPassword;
    }

    public void setEncrptPassword(String encrptPassword) {
        this.encrptPassword = encrptPassword;
    }


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Byte getGender() {
        return gender;
    }

    public void setGender(Byte gender) {
        this.gender = gender;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getRegisterMode() {
        return registerMode;
    }

    public void setRegisterMode(String registerMode) {
        this.registerMode = registerMode;
    }

    public String getThirdPartyId() {
        return thirdPartyId;
    }

    public void setThirdPartyId(String thirdPartyId) {
        this.thirdPartyId = thirdPartyId;
    }
}
