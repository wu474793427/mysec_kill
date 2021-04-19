package com.wuxc.myseckill.controller;

import com.google.common.util.concurrent.RateLimiter;
import com.wuxc.myseckill.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.TimeUnit;

/**
 * @author wwz
 * @version 1.0
 * @date 2021/4/16 15:10
 */


@Controller
@RequestMapping("/O")
public class OrderController {
    private static final Logger LOGGER = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    OrderService orderService;


    @GetMapping("/h")
    @ResponseBody
    public String h(){
        return "hello";
    }

    @RequestMapping("/createWrongOrder/{sid}")
    @ResponseBody
    public String createWrongOrder(@PathVariable int sid){
        LOGGER.info("购买商品ID sid = [{}]",sid);
        int id = 0;
        try {
            id = orderService.createWrongOrder(sid);
            LOGGER.info("创建订单 id = [{}]",id);
        }catch (Exception e){
            LOGGER.error("Exception",e);
        }
        return String.valueOf(id);
    }


    //基于令牌桶算法 每秒放行10个请求
    RateLimiter rateLimiter = RateLimiter.create(10);
    /**
     * 乐观锁更新库存 + 令牌桶限流
     * @param  sid
     * @return
     */
    @RequestMapping("/createOptimisticOrder/{sid}")
    @ResponseBody
    public String createOptimisticOrder(@PathVariable int sid){
        // 阻塞式获取令牌
        //LOGGER.info("等待时间" + rateLimiter.acquire());
        // 非阻塞式获取令牌
        if (!rateLimiter.tryAcquire(1000, TimeUnit.MILLISECONDS)) {
            LOGGER.warn("你被限流了，直接返回失败");
            return "购买失败，库存不足";
        }
        int id;

        try {
            id = orderService.createOptimisticOrder(sid);
            LOGGER.info("购买成功 剩余库存为 [{}]",id);
        }catch (Exception e){
            LOGGER.error("购买失败  [{}]",e.getMessage());
            return "购买失败 库存不足";
        }
        return String.format("购买成功，剩余库存为:%d",id);
    }

    /**
     * 事务for update更新库存
     * @param sid
     * @return
     */
    @RequestMapping("/createPessimisticOrder/{sid}")
    @ResponseBody
    public String createPessimisticOrder(@PathVariable int sid){
        int id;
        try {
            id = orderService.createPessimisticOrder(sid);
            LOGGER.info("购买成功，剩余库存为: [{}]", id);
        } catch (Exception e) {
            LOGGER.error("购买失败：[{}]", e.getMessage());
            return "购买失败，库存不足";
        }
        return String.format("购买成功，剩余库存为：%d", id);
    }
}
