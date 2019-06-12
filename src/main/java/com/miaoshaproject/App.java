package com.miaoshaproject;

import com.miaoshaproject.DAO.UserDOMapper;
import com.miaoshaproject.DO.UserDO;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Hello world!
 *
 * 1.开启EnableAutoConfiguration注解，将启动类当成可配置的bean，并开启spring自动化配置
 * 2.@RestController = @ResponseBody + @controller
 * 3.MapperScan开启Mapper扫描 --- 这种方式下，自动生成的DAO包内的类无需Mapper注释？---为什么会自动装配失败？
 */
//@EnableAutoConfiguration
@SpringBootApplication(scanBasePackages = {"com.miaoshaproject"})
@RestController
@MapperScan("com.miaoshaproject.DAO")
public class App {

    @Autowired
    private UserDOMapper userDOMapper;

    @RequestMapping(path = {"/"}, method = {RequestMethod.GET, RequestMethod.POST})
    public String home(){
        UserDO userDO = userDOMapper.selectByPrimaryKey(1);
        if(userDO == null){
            return "用户不存在";
        }else{
            return userDO.getName();
        }
    }

    public static void main( String[] args ) {
        System.out.println( "Hello World!" );
        //启动项目
        SpringApplication.run(App.class, args);
    }
}
