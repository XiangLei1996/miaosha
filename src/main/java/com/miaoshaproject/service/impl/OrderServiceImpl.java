package com.miaoshaproject.service.impl;

import com.miaoshaproject.DAO.OrderDOMapper;
import com.miaoshaproject.DAO.SequenceDOMapper;
import com.miaoshaproject.DAO.StockLogDOMapper;
import com.miaoshaproject.DO.OrderDO;
import com.miaoshaproject.DO.SequenceDO;
import com.miaoshaproject.DO.StockLogDO;
import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.error.EmBusinessError;
import com.miaoshaproject.model.ItemModel;
import com.miaoshaproject.model.OrderModel;
import com.miaoshaproject.service.ItemService;
import com.miaoshaproject.service.OrderService;
import com.miaoshaproject.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Author: XiangL
 * Date: 2019/6/15 7:58
 * Version 1.0
 * Transactional注解开启事务，事务的作用，何时开启事务（凡是对表进行修改的操作）
 */
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserService userService;

    @Autowired
    private OrderDOMapper orderDOMapper;

    @Autowired
    private SequenceDOMapper sequenceDOMapper;

    @Autowired
    private StockLogDOMapper stockLogDOMapper;



    //改写,需要加上一个 stockLogId
    @Override
    @Transactional
    public OrderModel createOrder(Integer userId, Integer itemId, Integer amount, Integer promoId, String stockLogId) throws BusinessException {
        //1.校验下单状态，下单的商品是否存在，用户是否合法，购买数量是否正确
        //ItemModel itemModel = itemService.getItemById(itemId);
        //使用优化缓存模型
        ItemModel itemModel = itemService.getItemByIdInCache(itemId);
        if(itemModel == null){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "商品信息不存在");
        }

//
//        //UserModel userModel = userService.getUserById(userId);
//        //使用缓存模型优化
//        UserModel userModel = userService.getUserByIdInCache(userId);
//        if(userModel == null){
//            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "用户不存在");
//        }

        if(amount <= 0 || amount > 99){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "请填写正确数量信息");
        }

//        //校验秒杀活动信息
//        if(promoId != null){
//            //（1）校验对应活动是否存在这个适应商品
//            if(promoId.intValue() != itemModel.getPromoModel().getId()){
//                throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "活动信息不正确");
//                //校验活动是否正在进行中
//            }else if(itemModel.getPromoModel().getStatus().intValue() != 2){
//                throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "活动还未开始");
//            }
//        }


        //也可以支付减库存，即落单时只查看库存，但支付时可能库存不够
        //2.落单则更新数据库，即落单减库存，即落单即锁定库存。但用户可能存在故意锁库存的情况
        //   采用落单减库存
        boolean result = itemService.decreaseStock(itemId, amount);
        if(!result){
            throw new BusinessException(EmBusinessError.STOCK_NOT_ENOUGH);
        }

        //3.订单入库
        OrderModel orderModel = new OrderModel();
        orderModel.setUserId(userId);
        orderModel.setItemId(itemId);
        orderModel.setAmount(amount);
        if(promoId != null){
            //使用秒杀价格
            orderModel.setItemPrice(itemModel.getPromoModel().getPromoItemPrice());
        }else{
            orderModel.setItemPrice(itemModel.getPrice());
        }
        orderModel.setPromoId(promoId);
        //这里为什么要用multiply()而不是用*？；因为商品单价是BigDecimal？
        orderModel.setOrderPrice(orderModel.getItemPrice().multiply(new BigDecimal(amount)));

        //生成交易流水号，即订单表的主键id
        orderModel.setId(generateOrderNo());
        OrderDO orderDO = convertFromOrderModel(orderModel);
        orderDOMapper.insertSelective(orderDO);

        //订单表入库后，加上商品的销量（实际情况下等到支付完成再处理）
        itemService.increaseSales(itemId, amount);

        //设置库存流水状态为成功----由于对应的下单操作和设置流水状态在同一个事务内，所以只要订单成功入库，那么流水状态肯定也设置成功
        StockLogDO stockLogDO = stockLogDOMapper.selectByPrimaryKey(stockLogId);
        //其实肯定不会为空，因为在上层的controller中，只有先初始化库存流水，才会调用事务型下单消息机制
        //但这里也可以做一个保护
        if(stockLogDO == null){
            throw new BusinessException(EmBusinessError.UNKNOW_ERROR);
        }
        stockLogDO.setStatus(2);//2表示下单扣减库存成功
        stockLogDOMapper.updateByPrimaryKeySelective(stockLogDO);



//        //借用Spring提供的组件
//        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
//
//            //该方法会在最近的Transactional注解标注的事务成功提交后，才执行里面的方法体
//            @Override
//            public void afterCommit() {
//                //将异步更新库存的实现放入该方法内，以避免事务提交出错（不是执行失败）带来的库存减少
//                //此时无法回滚库存了，因此要保证消息发送一定成功
//                boolean mqResult = itemService.asyncDecreaseStock(itemId, amount);
////                if(!mqResult){
////                    //失败，回滚
////                    itemService.increaseStock(itemId, amount);
////                    throw new BusinessException(EmBusinessError.MQ_SEND_FAIL);
////                }
//
//            }
//        });

        //4.返回前端
        return orderModel;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String generateOrderNo(){
        //订单号有16位--老师用的是StringBuilder，但是此处用的StringBuffer
        StringBuffer stringBuffer = new StringBuffer();
        //前8位有时间信息，年月日
        LocalDateTime now = LocalDateTime.now();
        String nowDate = now.format(DateTimeFormatter.ISO_DATE).replace("-","");
        stringBuffer.append(nowDate);

        //中间6位为自增序列;MySQL通过新建一个递增的sequence_info表实现
        //（1）获取当前sequence
        int sequence = 0;
        SequenceDO sequenceDO = sequenceDOMapper.getSequenceByName("order_info");
        sequence = sequenceDO.getCurrentValue();
        //(2）每次获取之后，根据步长更新；步长设定为1
        sequenceDO.setCurrentValue(sequenceDO.getCurrentValue() + sequenceDO.getStep());
        sequenceDOMapper.updateByPrimaryKeySelective(sequenceDO);
        String sequenceStr = String.valueOf(sequence);
        for(int i = 0; i < 6 - sequenceStr.length(); i++){
            stringBuffer.append(0);//长度不足则前面用0填充。比如000001
        }
        stringBuffer.append(sequenceStr);

        //最后2位为分库分表位,暂时写死
        stringBuffer.append("00");

        return stringBuffer.toString();
    }

    private OrderDO convertFromOrderModel(OrderModel orderModel){
        if(orderModel == null){
            return null;
        }
        OrderDO orderDO = new OrderDO();
        BeanUtils.copyProperties(orderModel, orderDO);
        //有两个属性由于double的精度问题，领域模型中使用了 BigDecimal,所以要手动转换
        orderDO.setItemPrice(orderModel.getItemPrice().doubleValue());
        orderDO.setOrderPrice(orderModel.getOrderPrice().doubleValue());
        return orderDO;
    }
}
