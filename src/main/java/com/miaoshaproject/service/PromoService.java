package com.miaoshaproject.service;

import com.miaoshaproject.model.PromoModel;

/**
 * Author: XiangL
 * Date: 2019/6/15 19:20
 * Version 1.0
 */
public interface PromoService {

    //根据itemId获取即将进行的或正在进行的秒杀活动
    PromoModel getPromoByItemId(Integer itemId);

    //活动发布
    void publishPromo(Integer promoId);
}
