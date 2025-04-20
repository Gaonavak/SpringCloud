/**
 * @Author :Novak
 * @Description :
 * @Date: 2025/4/18 0:04
 */
package com.demo.orderservice.feignClient;

import com.demo.commonmodule.entity.Order;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "point-service")
public interface PointServiceFeignClient {

    @PostMapping(value = "/point/add1")
    String addPoint1(@RequestBody Order order);

    @PostMapping(value = "/point/add2")
    String addPoint2(@RequestParam("productionName") String productionName);
}