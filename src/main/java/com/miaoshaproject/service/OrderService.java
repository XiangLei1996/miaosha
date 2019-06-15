package com.miaoshaproject.service;

import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.model.OrderModel;

/**
 * Author: XiangL
 * Date: 2019/6/15 7:57
 * Version 1.0
 */
public interface OrderService {

    /*两种实现秒杀下单方式
    1.通过前端url传过来秒杀活动id,然后下单接口内校验对应id是否属于对应商品活动已开始
    2。直接在下单接口内判断对应的商品是否存在秒杀活动，若存在进行中的则以秒杀价格下单
    各有优点，推荐使用第一种。同一款商品可能存在不同秒杀活动？（便于扩展）如果用第二种，平销商品也要查询
     */
    OrderModel createOrder(Integer userId, Integer ItemId, Integer amount, Integer promoId) throws BusinessException;
}
