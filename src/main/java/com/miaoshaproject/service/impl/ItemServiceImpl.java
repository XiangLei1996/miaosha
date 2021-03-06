package com.miaoshaproject.service.impl;

import com.miaoshaproject.DAO.ItemDOMapper;
import com.miaoshaproject.DAO.ItemStockDOMapper;
import com.miaoshaproject.DAO.StockLogDOMapper;
import com.miaoshaproject.DO.ItemDO;
import com.miaoshaproject.DO.ItemStockDO;
import com.miaoshaproject.DO.StockLogDO;
import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.error.EmBusinessError;
import com.miaoshaproject.model.ItemModel;
import com.miaoshaproject.model.PromoModel;
import com.miaoshaproject.mq.MqProducer;
import com.miaoshaproject.service.ItemService;
import com.miaoshaproject.service.PromoService;
import com.miaoshaproject.validator.ValidationResult;
import com.miaoshaproject.validator.ValidatorImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Author: XiangL
 * Date: 2019/6/14 11:46
 * Version 1.0
 *
 * 注意：Service实现类一定要@Service注解，否则无法自动注入
 */
@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    private ValidatorImpl validator;

    @Autowired
    private ItemDOMapper itemDOMapper;

    @Autowired
    private ItemStockDOMapper itemStockDOMapper;

    @Autowired
    private PromoService promoService;

    @Autowired
    private RedisTemplate redisTemplate;

    //注意，这里需要导入的时我们自定义的类，不是rocketMQ自带的
    @Autowired
    private MqProducer mqProducer;

    @Autowired
    private StockLogDOMapper stockLogDOMapper;

    //初始化对应的库存流水,并返回对应流水的主键id stock_log_id
    @Override
    @Transactional
    public String initStockLog(Integer itemId, Integer amount) {
        StockLogDO stockLogDO = new StockLogDO();
        stockLogDO.setItemId(itemId);
        stockLogDO.setAmount(amount);
        //使用UUID作为主键，记得要替换 -
        stockLogDO.setStockLogId(UUID.randomUUID().toString().replace("-",""));
        stockLogDO.setStatus(1);

        //插入初始化的库存流水记录
        stockLogDOMapper.insertSelective(stockLogDO);

        return stockLogDO.getStockLogId();
    }

    //异步更新库存操作
    @Override
    public boolean asyncDecreaseStock(Integer itemId, Integer amount) {
        boolean mqResult = mqProducer.asyncReduceStock(itemId, amount);
        return mqResult;
    }

    @Override
    public ItemModel getItemByIdInCache(Integer id) {
        ItemModel itemModel = (ItemModel) redisTemplate.opsForValue().get("item_validate_"+id);
        if(itemModel == null){
            itemModel = this.getItemById(id);
            redisTemplate.opsForValue().set("item_validate_"+id, itemModel);
            redisTemplate.expire("item_validate_"+id, 10, TimeUnit.MINUTES);
        }

        return itemModel;
    }

    //库存回滚
    @Override
    public boolean increaseStock(Integer itemId, Integer amount) {
        redisTemplate.opsForValue().increment("promo_item_stock_"+itemId,amount.intValue()*1);
        return true;
    }

    //涉及修改数据库，要开启事务
    //2.使用redis缓存商品库存后，一旦下单导致需要更新数据库库存时，需要同步更新redis
    @Override
    @Transactional
    public boolean decreaseStock(Integer itemId, Integer amount) {
        //int affectedRow = itemStockDOMapper.decreaseStock(itemId, amount);
        /*使用increment一个 -1 即减1;返回的result为更新手剩余的值*/
        long result = redisTemplate.opsForValue().increment("promo_item_stock_"+itemId,amount.intValue()*-1);
//        if(affectedRow > 0){
        if(result > 0){
            //更新库存成功
            return true;
        }else if(result == 0){
            //添加库存已售罄标识
            redisTemplate.opsForValue().set("promo_item_stock_invalid_"+itemId, "true");

            //更新库存成功
            return true;
        }else{
            //更新库存失败--result<0，代表没有库存了
            increaseStock(itemId, amount);
            return false;
        }
    }

    @Override
    @Transactional
    public void increaseSales(Integer itemId, Integer amount) throws BusinessException {
        itemDOMapper.increaseSales(itemId, amount);
    }

    @Override
    @Transactional//开启事务
    public ItemModel createItem(ItemModel itemModel) throws BusinessException {
        //校验入参；入库前的校验入参是必须的
        ValidationResult result = validator.validate(itemModel);
        if(result.isHasErrors()){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, result.getErrMsg());
        }

        //转换ItemModel为data Object
        ItemDO itemDO = this.convertItemDOFromItemModel(itemModel);

        //写入数据库
        itemDOMapper.insertSelective(itemDO);

        //这部分要写在后面，因为itemDO使用insertSelective插入后，会获得自增的主键id（通过对应的mapper.xml设置）
        itemModel.setId(itemDO.getId());
        ItemStockDO itemStockDO = this.convertItemStockDoFromItemModel(itemModel);
        itemStockDOMapper.insertSelective(itemStockDO);

        //返回创建完成的对像
        return this.getItemById(itemModel.getId());
    }

    @Override
    public List<ItemModel> listItem() {
        List<ItemDO> itemDOList = itemDOMapper.listItem();

        //使用了Java8的API stream()
        List<ItemModel> itemModelList = itemDOList.stream().map(itemDO -> {
            ItemStockDO itemStockDO = itemStockDOMapper.selectByItemId(itemDO.getId());
            ItemModel itemModel = this.convertModelFromDataObject(itemDO, itemStockDO);
            return itemModel;
        }).collect(Collectors.toList());
        return itemModelList;
    }

    @Override
    public ItemModel getItemById(Integer id) {
        ItemDO itemDO = itemDOMapper.selectByPrimaryKey(id);
        if(itemDO == null){
            return null;
        }

        //获取库存数量
        ItemStockDO itemStockDO = itemStockDOMapper.selectByItemId(id);

        //将data Object 转换为 Model
        ItemModel itemModel = convertModelFromDataObject(itemDO, itemStockDO);

        //获取活动商品信息---用于秒杀业务
        PromoModel promoModel = promoService.getPromoByItemId(itemModel.getId());
        if(promoModel != null && promoModel.getStatus().intValue() != 3){
            //存在配置了秒杀活动且秒杀活动还未结束（即未开始或正在进行）的商品
            itemModel.setPromoModel(promoModel);
        }

        return itemModel;
    }


    /**
     * 下面均为领域模型和数据库模型之间的转换方法
     * @param itemModel
     * @return
     */
    private ItemDO convertItemDOFromItemModel(ItemModel itemModel){
        if(itemModel == null){
            return null;
        }
        ItemDO itemDO = new ItemDO();
        BeanUtils.copyProperties(itemModel, itemDO);
        itemDO.setPrice(itemModel.getPrice().doubleValue());
        return itemDO;
    }

    private ItemStockDO convertItemStockDoFromItemModel(ItemModel itemModel){
        if(itemModel == null){
            return null;
        }
        ItemStockDO itemStockDO = new ItemStockDO();
        itemStockDO.setItemId(itemModel.getId());
        itemStockDO.setStock(itemModel.getStock());

        return itemStockDO;
    }

    private ItemModel convertModelFromDataObject(ItemDO itemDO, ItemStockDO itemStockDO){
        ItemModel itemModel = new ItemModel();

        BeanUtils.copyProperties(itemDO, itemModel);

        //因为Price属性Model为BigDecimal而DAO中为double，所以不能自动对应的填充，需要手动设置
        itemModel.setPrice(new BigDecimal((itemDO.getPrice())));
        itemModel.setStock(itemStockDO.getStock());

        return itemModel;
    }
}
