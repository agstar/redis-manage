package com.redis.manager.handler;

import com.redis.manager.model.RedisKey;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.Optional;

/**
 * 默认的类型处理器，根据已编码的
 * @author agstar
 */
@Component
public class TypeHandler {

    public RedisKey getRedisKey(String base64keyName, StringRedisTemplate stringRedisTemplate) {
        return stringRedisTemplate.execute((RedisCallback<RedisKey>) redisConnection -> {
            byte[] decode = Base64.getDecoder().decode(base64keyName);
            DataType type = redisConnection.type(decode);
//            byte[] bytes = redisConnection.get(decode);
            return RedisKey.builder().base64KeyName(base64keyName)
                    .type(Optional.ofNullable(type)
                            .map(DataType::code)
                            .orElse(null)
                    )
                    .build();
        });
    }


}
