/**
 * @Author :Novak
 * @Description : 自定义控制类
 * @Date: 2025/4/17 20:22
 */
package com.demo.orderservice.controller;

import com.demo.commonmodule.entity.Order;
import com.demo.orderservice.feignClient.PointServiceFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/order")
@RefreshScope
public class OrderController {


    @Value("${config.info}")
    private String configInfo;

    @GetMapping("/test")
    String test() {
        return " this is a order-service";
    }

    @GetMapping("/test/getconfigInfo")
    public String getConfigInfo() {
        return configInfo;
    }


    @Autowired
    private PointServiceFeignClient pointServiceFeignClient;

    @PostMapping(value = "/add1")
    public String addOrder1() {
        Order order = new Order();
        order.setId("123");
        order.setProductionName("水杯");
        System.out.println(order);
        String res = pointServiceFeignClient.addPoint1(order);
        return res;
    }

    @PostMapping(value = "/add2")
    public String addOrder2() {
        String res = pointServiceFeignClient.addPoint2("水杯2");
        return res;
    }


}