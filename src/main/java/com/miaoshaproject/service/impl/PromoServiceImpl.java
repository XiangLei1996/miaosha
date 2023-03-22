package com.miaoshaproject.service.impl;

import com.miaoshaproject.DAO.PromoDOMapper;
import com.miaoshaproject.DO.PromoDO;
import com.miaoshaproject.model.ItemModel;
import com.miaoshaproject.model.PromoModel;
import com.miaoshaproject.model.UserModel;
import com.miaoshaproject.service.PromoService;
import com.miaoshaproject.service.UserService;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

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

    @Autowired
    private UserService userService;


    //发放秒杀令牌
    @Override
    public String generateSecondKillToken(Integer promoId, Integer itemId, Integer userId) {
        //获取对应商品的秒杀活动信息，主键查询
        PromoDO promoDO = promoDOMapper.selectByPrimaryKey(promoId);

        PromoModel promoModel = convertFromDataObject(promoDO);
        if(promoModel == null){
            return null;
        }

//        //判断当前时间是否秒杀活动即将开始，或者正在进行
//        //通过当前时间判断
//        if(promoModel.getStartDate().isAfterNow()){
//            promoModel.setStatus(1);
//        }else if(promoModel.getEndDate().isBeforeNow()){
//            promoModel.setStatus(3);
//        }else{
//            //正在进行中
//            promoModel.setStatus(2);
//        }

        //通过库存售罄标识是否存在，来判断库存是否已售罄，售罄则直接返回失败
        //库存售罄从下单前判断前置到秒杀令牌发放之中
        if(redisTemplate.hasKey("promo_item_stock_invalid_"+itemId)){
            return null;
        }

        //判断是否允许生成令牌,只有活动商品才能生成令牌
        if(promoModel.getStatus() != 2){
            return null;
        }

        //判断商品信息是否存在
        ItemModel itemModel = itemService.getItemByIdInCache(itemId);
        if(itemModel == null){
            return null;
        }

        //判断用户信息是否存在；
        //用户风控策略前置到秒杀令牌的发放当中
        UserModel userModel = userService.getUserByIdInCache(userId);
        if(userModel == null){
            return null;
        }

        //获取秒杀大闸的count数量
        long result = (long) redisTemplate.opsForValue().get("promo_door_count_"+promoId);
        if(result < 0){
            return null;
        }

        //生成token令牌并返回，同时存储到redis中，设定有效期为5分钟
        String token = UUID.randomUUID().toString().replace("-","");

        //思考为什么要加userId，itemId这些
        redisTemplate.opsForValue().set("promo_token_"+promoId+"_userid_"+userId+"_itemid_"+itemId, token);
        redisTemplate.expire("promo_token_"+promoId, 5, TimeUnit.MINUTES);

        return token;

    }

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


        //将大闸的限制数设置到redis内;目前设置为5倍库存，较宽松
        redisTemplate.opsForValue().set("promo_door_count_"+promoId, itemModel.getStock().intValue()*5);
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
