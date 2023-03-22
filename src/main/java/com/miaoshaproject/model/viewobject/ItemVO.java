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

    //记录商品是否正在秒杀活动中，以及对应的状态
    //0无秒杀活动，1还未开始，2活动正在进行
    private Integer promoStatus;

    //秒杀活动价格
    private BigDecimal promoPrice;

    //秒杀互动id
    private Integer promoId;

    //秒杀活动开始时间 -- 用String表示是为了传给前端之后不显示序列化多余的信息
    private String startDate;

    public Integer getPromoStatus() {
        return promoStatus;
    }

    public void setPromoStatus(Integer promoStatus) {
        this.promoStatus = promoStatus;
    }

    public BigDecimal getPromoPrice() {
        return promoPrice;
    }

    public void setPromoPrice(BigDecimal promoPrice) {
        this.promoPrice = promoPrice;
    }

    public Integer getPromoId() {
        return promoId;
    }

    public void setPromoId(Integer promoId) {
        this.promoId = promoId;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

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
