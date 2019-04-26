package com.redis.manage.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.redis.manage.entity.Result;
import com.redis.manage.entity.StatusCode;
import com.redis.manage.model.RedisServer;
import com.redis.manage.util.RedisServerUtil;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

import static com.redis.manage.util.Const.*;

@RestController
public class KeyController {
    private static final ScanOptions SCAN_OPTIONS = new ScanOptions.ScanOptionsBuilder().match("*").count(10000).build();

    @GetMapping("key/{serverName}/{dbIndex}")
    public Result getKeyList(@PathVariable("serverName") String serverName, @PathVariable("dbIndex") int dbIndex) {
        StringRedisTemplate stringRedisTemplate = REDIS_TEMPLATE_MAP.get(serverName + DEFAULT_SEPARATOR + dbIndex);
        if (stringRedisTemplate == null) {
            Optional<RedisServer> first = REDIS_SERVER.stream().filter(x -> x.getName().equals(serverName)).findFirst();
            if (first.isPresent()) {
                stringRedisTemplate = RedisServerUtil.initRedisConnection(first.get(), dbIndex);
            } else {
                return new Result(false, StatusCode.ERROR, "没有对应的服务");
            }
        }
        //scan 0 MATCH * COUNT 10000
        //加载初始化 template，点击server加载template
        Set<String> keys = stringRedisTemplate.execute((RedisCallback<Set<String>>) redisConnection -> {
            Set<String> binaryKeys = new HashSet<>();
            Cursor<byte[]> cursor = redisConnection.scan(SCAN_OPTIONS);
            while (cursor.hasNext()) {
                binaryKeys.add(new String(cursor.next()));
            }
            return binaryKeys;
        });
        JSONArray jsonArray = new JSONArray();
        if (keys != null) {
            keys.forEach(x -> RedisServerUtil.getKeyTree(x, jsonArray, serverName, dbIndex));
        }
        return new Result(true, StatusCode.OK, "查询成功", jsonArray);
    }


    @GetMapping("key/{serverName}/{dbIndex}/{keyName}")
    public Result getKey(@PathVariable("serverName") String serverName, @PathVariable("dbIndex") int dbIndex, @PathVariable("keyName") String keyName) {
        StringRedisTemplate stringRedisTemplate = REDIS_TEMPLATE_MAP.get(serverName + DEFAULT_SEPARATOR + dbIndex);
        if (stringRedisTemplate == null) {
            Optional<RedisServer> first = REDIS_SERVER.stream().filter(x -> x.getName().equals(serverName)).findFirst();
            if (first.isPresent()) {
                stringRedisTemplate = RedisServerUtil.initRedisConnection(first.get(), dbIndex);
            } else {
                return new Result(false, StatusCode.ERROR, "没有对应的服务");
            }
        }
        DataType type = stringRedisTemplate.type(keyName);

        if (type != null) {
            switch (type) {
                case SET:
                    stringRedisTemplate.opsForValue().get(keyName);
                    break;
                case HASH:
                    Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries(keyName);
                    break;
                case LIST:
                    List<String> range = stringRedisTemplate.opsForList().range(keyName, 0, 1000);
                    break;
                case STRING:
                    String s = stringRedisTemplate.opsForValue().get(keyName);
                    break;
                case NONE:
                    break;
                case ZSET:
                    Set<String> range1 = stringRedisTemplate.opsForZSet().range(keyName, 0, 1000);
                    break;
                default:
                    break;
            }
        }

        //加载初始化 template，点击server加载template
        Set<String> keys = stringRedisTemplate.execute((RedisCallback<Set<String>>) redisConnection -> {
            Set<String> binaryKeys = new HashSet<>();
            Cursor<byte[]> cursor = redisConnection.scan(SCAN_OPTIONS);
            //Properties info = redisConnection.info();
            while (cursor.hasNext()) {
                binaryKeys.add(new String(cursor.next()));
            }
            return binaryKeys;
        });
        List<JSONObject> list = new ArrayList<>();
        if (keys != null) {
            keys.forEach(x -> list.add(RedisServerUtil.getKeyTree(x)));
        }
        return new Result(true, StatusCode.OK, "查询成功", list);
    }


    @GetMapping("key/{serverName}")
    public Result getKey(@PathVariable("serverName") String serverName) {
        Optional<RedisServer> first = REDIS_SERVER.stream().filter(x -> x.getName().equals(serverName)).findFirst();
        if (first.isPresent()) {
            List<Integer> count = RedisServerUtil.getRedisKeyCount(first.get());
            return new Result(true, StatusCode.OK, "查询成功", count);
        } else {
            return new Result(false, StatusCode.ERROR, "未找到" + serverName);
        }
    }

    @GetMapping("keyCount/{serverName}")
    public Result getKeyCount(@PathVariable("serverName") String serverName) {
        Optional<RedisServer> first = REDIS_SERVER.stream().filter(x -> x.getName().equals(serverName)).findFirst();
        if (first.isPresent()) {
            List<Integer> count = RedisServerUtil.getRedisKeyCount(first.get());
            return new Result(true, StatusCode.OK, "查询成功", count);
        } else {
            return new Result(false, StatusCode.ERROR, "未找到" + serverName);
        }
    }


}
