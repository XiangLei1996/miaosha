package com.miaoshaproject.config;

import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.stereotype.Component;

/**
 * Author: XiangL
 * Date: 2019/6/19 17:23
 * Version 1.0
 *
 * EnableRedisHttpSession注解设定，默认1800
 * 将httpSession放到redis中
 */
//Redis配置
@Component
@EnableRedisHttpSession(maxInactiveIntervalInSeconds = 3600)
public class RedisConfig {

}
