package com.wuxc.myseckill.Recevier;

import com.alibaba.fastjson.JSONObject;
import com.wuxc.myseckill.service.OrderService;
import com.wuxc.myseckill.service.StockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author wwz
 * @version 1.0
 * @date 2021/4/24 14:30
 */
@Component
@RabbitListener(queues = "orderQueue")
public class OrderMqReceiver  {
    private static final Logger LOGGER = LoggerFactory.getLogger(OrderMqReceiver.class);

    @Autowired
    private StockService stockService;

    @Autowired
    private OrderService orderService;

    @RabbitHandler
    public void process(String messages){
        LOGGER.info("OrderMqReceiver收到消息开始用户下单流程: " + messages);
        JSONObject jsonObject = JSONObject.parseObject(messages);
        try {
            orderService.createOrderByMq(jsonObject.getInteger("sid"),jsonObject.getInteger("userId"));
        }catch (Exception e){
            LOGGER.error("消息处理异常:",e);
        }
    }
}
