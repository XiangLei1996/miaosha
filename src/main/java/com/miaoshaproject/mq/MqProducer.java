package com.miaoshaproject.mq;

import com.alibaba.fastjson.JSON;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * Author: XiangL
 * Date: 2019/6/23 18:07
 * Version 1.0
 */
@Component
public class MqProducer {

    private DefaultMQProducer producer;

    @Value("${mq.nameserver.addr}")
    private String nameAddr;

    @Value("${mq.topicname}")
    private String topicName;

    @PostConstruct
    public void init() throws MQClientException {
        //做mq producer的初始化,producerGroup只是一个标识，没有实际意义
        producer = new DefaultMQProducer("producer_group");
        producer.setNamesrvAddr(nameAddr);

        producer.start();
    }

    //同步库存扣减消息
    public boolean asyncReduceStock(Integer itemId, Integer amount){
        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("itemId", itemId);
        bodyMap.put("amount", amount);

        //使用rocketMQ自带的Message类
        Message message = new Message(topicName, "increase",
                JSON.toJSONString(bodyMap).toString().getBytes(Charset.forName("UTF-8")));

        //发送消息的操作，只有成功和出现异常，
        //若发生异常则返回false代表发送失败以便上层判断结果
        try {
            producer.send(message);
        } catch (MQClientException e) {
            e.printStackTrace();
            return false;
        } catch (RemotingException e) {
            e.printStackTrace();
            return false;
        } catch (MQBrokerException e) {
            e.printStackTrace();
            return false;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
