package com.miaoshaproject.service.impl;

import com.miaoshaproject.DAO.PromoDOMapper;
import com.miaoshaproject.DO.PromoDO;
import com.miaoshaproject.model.ItemModel;
import com.miaoshaproject.model.PromoModel;
import com.miaoshaproject.service.PromoService;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Author: XiangL
 * Date: 2019/6/15 19:21
 * Version 1.0
 */
@Service
public class PromoServiceImpl implements PromoService {

    @Autowired
    private PromoDOMapper promoDOMapper;

    @Autowired
    private ItemServiceImpl itemService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 实际上该活动应当让一个发布后台去发布
     * @param promoId
     */
    //发布活动
    @Override
    public void publishPromo(Integer promoId) {
        //通过活动id获取活动
        PromoDO promoDO = promoDOMapper.selectByPrimaryKey(promoId);
        if(promoDO == null || promoDO.getItemId().intValue() == 0){
            return;
        }
        //读出对应的库存
        ItemModel itemModel = itemService.getItemById(promoDO.getItemId());

        //注意，这里实际上可能会在读取之后发生变化；要如何处理呢？？

        //这里暂时默认不会变化,库存同步到缓存
        redisTemplate.opsForValue().set("promo_item_stock_"+itemModel.getId(), itemModel.getStock());
    }

    @Override
    public PromoModel getPromoByItemId(Integer itemId) {
        //获取对应商品的秒杀活动信息
        PromoDO promoDO = promoDOMapper.selectByItemId(itemId);

        //data object -> model
        PromoModel promoModel = convertFromDataObject(promoDO);
        if(promoModel == null){
            return null;
        }

        //判断当前时间是否秒杀活动即将开始，或者正在进行
        //通过当前时间判断
        if(promoModel.getStartDate().isAfterNow()){
            promoModel.setStatus(1);
        }else if(promoModel.getEndDate().isBeforeNow()){
            promoModel.setStatus(3);
        }else{
            //正在进行中
            promoModel.setStatus(2);
        }

        return promoModel;
    }

    private PromoModel convertFromDataObject(PromoDO promoDO){
        if(promoDO == null){
            return null;
        }

        PromoModel promoModel = new PromoModel();
        BeanUtils.copyProperties(promoDO, promoModel);
        promoModel.setPromoItemPrice(new BigDecimal(promoDO.getPromoItemPrice()));
        promoModel.setStartDate(new DateTime(promoDO.getStartDate()));
        promoModel.setEndDate(new DateTime(promoDO.getEndDate()));

        return promoModel;
    }
}
