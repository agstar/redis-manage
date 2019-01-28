package com.redis.redismanage;

import com.redis.redismanage.util.RedisServerUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RedisManageApplication {

    public static void main(String[] args) {
        SpringApplication.run(RedisManageApplication.class, args);
        //读取所有redis
        RedisServerUtil.readAllServer();
        RedisServerUtil.initKeyCount();
    }
}
