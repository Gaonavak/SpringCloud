/**
 * @Author :Novak
 * @Description : 订购
 * @Date: 2025/4/17 23:14
 */
package com.demo.commonmodule.entity;


public class Order {

    private String id;
    private String productionName;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProductionName() {
        return productionName;
    }

    public void setProductionName(String productionName) {
        this.productionName = productionName;
    }
}