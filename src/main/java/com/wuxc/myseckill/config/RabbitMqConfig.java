package com.wuxc.myseckill.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;



/**
 * @author wwz
 * @version 1.0
 * @date 2021/4/22 19:04
 */
@Configuration
public class RabbitMqConfig {
    @Bean
    public Queue delCacheQueue(){
        return new Queue("delCache");
    }

    @Bean
    public Queue orderQueue(){
        return new Queue("orderQueue");
    }
}
