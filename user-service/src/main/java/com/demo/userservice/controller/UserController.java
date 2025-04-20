/**
 * @Author :Novak
 * @Description : 自定义控制类
 * @Date: 2025/4/17 20:22
 */
package com.demo.userservice.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.demo.commonmodule.entity.util.JWTutil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping(value = "/user")
public class UserController {

    @Value("${config.redisTimeout}")
    private Long redisTimeout;
    @Autowired
    private RedisTemplate redisTemplate;

    @GetMapping("/login")
    String login() {

        // verity account
        // create token
        String token = JWTutil.getToken("123");
        redisTemplate.opsForValue().set(token, token, redisTimeout, TimeUnit.SECONDS);
        // pass token into redis
        // return token back to client
        return token;
    }


    @GetMapping(value = "/slowCall")
    public String timeout() {
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return "1111";
    }

    @GetMapping(value = "/exception")
    public String exception() {
        int i = 10 / 0;
        return "2222";
    }

    @GetMapping(value = "/hotkey")
    @SentinelResource(value = "testHotkey", blockHandler = "testHotKeyBlockHandler")
    public String testHotkey(@RequestParam(value = "p1", required = false) String p1, @RequestParam(value = "p2", required = false) String p2) {
        return "testHotkey";
    }

    public String testHotKeyBlockHandler(String p1, String p2, BlockException blockException) {
        return "触发了热点降级";
    }
}