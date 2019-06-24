package com.miaoshaproject.mq;

import com.alibaba.fastjson.JSON;
import com.miaoshaproject.DAO.ItemStockDOMapper;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

/**
 * Author: XiangL
 * Date: 2019/6/23 18:07
 * Version 1.0
 */
@Component
public class MqConsumer {

    private DefaultMQPushConsumer consumer;

    @Value("${mq.nameserver.addr}")
    private String nameAddr;

    @Value("${mq.topicname}")
    private String topicName;

    @Autowired
    private ItemStockDOMapper itemStockDOMapper;

    @PostConstruct
    public void init() throws MQClientException {
        //初始化consumer,设置consumerGroup，这个是有意义的
        consumer = new DefaultMQPushConsumer("stock_consumer_group");
        consumer.setNamesrvAddr(nameAddr);

        //设置consumer订阅对应topicName的所有消息
        consumer.subscribe(topicName, "*");

        //设置消息监听器返回后，如何处理消息---即实现库存真正到数据库内扣减的逻辑
        consumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext consumeConcurrentlyContext) {
                Message msg = msgs.get(0);
                String jsonString = new String(msg.getBody());
                Map<String, Object> map = JSON.parseObject(jsonString, Map.class);
                Integer itemId = (Integer) map.get("itemId");
                Integer amount = (Integer) map.get("amount");

                itemStockDOMapper.decreaseStock(itemId, amount);

                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });

        //启动consumer
        consumer.start();
    }
}
