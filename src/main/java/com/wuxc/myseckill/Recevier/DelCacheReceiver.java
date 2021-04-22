package com.wuxc.myseckill.Recevier;

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
 * @date 2021/4/22 19:07
 */
@Component
@RabbitListener(queues = "delCache")
public class DelCacheReceiver{
    private static final Logger LOGGER = LoggerFactory.getLogger(DelCacheReceiver.class);

    @Autowired
    private StockService stockService;

    @RabbitHandler
    public void process(String message){
        LOGGER.info("DelCacheReceiver收到消息: " + message);
        LOGGER.info("DelCacheReceiver开始删除缓存: " + message);
        stockService.delStockCountCache(Integer.parseInt(message));
    }
}
