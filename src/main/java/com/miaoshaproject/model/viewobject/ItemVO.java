package com.miaoshaproject.model.viewobject;

import java.math.BigDecimal;

/**
 * Author: XiangL
 * Date: 2019/6/14 12:12
 * Version 1.0
 * 暴露给前端使用的模型
 */
public class ItemVO {
    private Integer id;

    //商品名称
    private String title;

    //商品价格,不用double是因为存在精度问题如1.9显示为1.9999？
    private BigDecimal price;

    //商品库存
    private Integer stock;

    //商品描述
    private String description;

    //商品销量
    private Integer sales;

    //商品描述图片url
    private String imgUrl;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getSales() {
        return sales;
    }

    public void setSales(Integer sales) {
        this.sales = sales;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }
}
