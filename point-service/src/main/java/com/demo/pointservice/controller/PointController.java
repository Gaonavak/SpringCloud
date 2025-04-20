/**
 * @Author :Novak
 * @Description : 自定义控制类
 * @Date: 2025/4/17 20:22
 */
package com.demo.pointservice.controller;

import com.demo.commonmodule.entity.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/point")
public class PointController {


    @Value("${config.info}")
    private String configinfo;

    @GetMapping("/test")
    String test() {
        return " this is a point-service";
    }

    @GetMapping("/test/configinfo")
    String test1() {
        return configinfo;
    }

    @PostMapping(value = "/add1")
    public String addPoint(@RequestBody Order order) {
        return "add point success!商品名称222：" + order.getProductionName();
    }

    @PostMapping(value = "/add2")
    public String addPoint2(@RequestParam("productionName") String productionName) {
        return "add point success!商品名称：" + productionName;
    }


}