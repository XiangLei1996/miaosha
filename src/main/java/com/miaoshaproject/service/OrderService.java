package com.miaoshaproject.service;

import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.model.OrderModel;

/**
 * Author: XiangL
 * Date: 2019/6/15 7:57
 * Version 1.0
 */
public interface OrderService {

    OrderModel createOrder(Integer userId, Integer ItemId, Integer amount) throws BusinessException;
}
