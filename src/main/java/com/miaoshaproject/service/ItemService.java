package com.miaoshaproject.service;

import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.model.ItemModel;

import java.util.List;

/**
 * Author: XiangL
 * Date: 2019/6/14 11:43
 * Version 1.0
 */
public interface ItemService {

    //创建商品
    ItemModel createItem(ItemModel itemModel) throws BusinessException;

    //商品列表浏览
    List<ItemModel> listItem();

    //交易优化，验证item和promo model缓存模型
    ItemModel getItemByIdInCache(Integer id);

    //商品详情浏览
    ItemModel getItemById(Integer id);

    //库存扣减
    boolean decreaseStock(Integer itemId, Integer amount);

    //商品销量增加；每次成功一笔订单，对应的商品销量要增加相应值
    void increaseSales(Integer itemId, Integer amount) throws BusinessException;
}
