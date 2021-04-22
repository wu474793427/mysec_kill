package com.wuxc.myseckill.controller;

import com.google.common.util.concurrent.RateLimiter;
import com.wuxc.myseckill.service.OrderService;
import com.wuxc.myseckill.service.StockService;
import com.wuxc.myseckill.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

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
     * 事务 for update更新库存
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

    @Autowired
    UserService userService;
    /**
     * 获取验证码
     */
    @ResponseBody
    @RequestMapping(value = "/getVerifyHash", method = {RequestMethod.GET})
    public String getVerifyHash(@RequestParam(value = "sid") Integer sid,
                                @RequestParam(value = "userId") Integer userId){
        String hash;
        try {
            hash = userService.getVerifyHash(sid,userId);
        }catch (Exception e){
            LOGGER.error("获取验证HASH失败，原因[{}]",e.getMessage());
            return "获取验证HASH失败";
        }
        return String.format("请求抢购验证hash值为:%s",hash);
    }

    /**
     * 带验证的抢购接口
     */
    @GetMapping("/createOrderWithVerifiedUrl")
    @ResponseBody
    public String createOrderWithVerifiedUrl(@RequestParam(value = "sid") Integer sid,
                                             @RequestParam(value = "userId") Integer userId,
                                             @RequestParam(value = "verifyHash") String verifyHash){
        int stockLeft;
        try{
            stockLeft = orderService.createVerifiedOrder(sid, userId, verifyHash);
            LOGGER.info("购买成功，剩余库存为: [{}]", stockLeft);
        }catch (Exception e){
            LOGGER.error("购买失败：[{}]", e.getMessage());
            return e.getMessage();
        }
        return String.format("购买成功，剩余库存为：%d", stockLeft);
    }

    /**
     * 带HASH验证+ 单用户限制访问频率
     * @param sid
     * @param userId
     * @param verifyHash
     * @return
     */
    @ResponseBody
    @GetMapping("/createOrderWithVerifiedUrlAndLimit")
    public String createOrderWithVerifiedUrlAndLimit(@RequestParam(value = "sid") Integer sid,
                                                     @RequestParam(value = "userId") Integer userId,
                                                     @RequestParam(value = "verifyHash") String verifyHash){
        int stockLeft;
        try {
            int count = userService.addUserCount(userId);
            LOGGER.info("用户截至该次的访问次数为: [{}]", count);
            boolean isBanned = userService.getUserIsBanned(userId);
            if (isBanned) {
                return "购买失败，超过频率限制";
            }
            stockLeft = orderService.createVerifiedOrder(sid, userId, verifyHash);
            LOGGER.info("购买成功，剩余库存为: [{}]", stockLeft);
        } catch (Exception e) {
            LOGGER.error("购买失败：[{}]", e.getMessage());
            return e.getMessage();
        }
        return String.format("购买成功，剩余库存为 %d",stockLeft);
    }

    @Autowired
    StockService stockService;
    /**
     * 下单接口：先删除缓存，再更新数据库
     */
    @RequestMapping("/createOrderWithCacheV1/{sid}")
    @ResponseBody
    public String createOrderWithCacheV1(@PathVariable int sid){
        int count = 0;
        try {
            //先删除缓存
            stockService.delStockCountCache(sid);
            //完成扣库存下单任务
            orderService.createPessimisticOrder(sid);
        }catch (Exception e){
            LOGGER.error("购买失败，[{}]",e.getMessage());
            return "购买失败 库存不足";
        }
        LOGGER.info("购买成功，剩余库存为[{}]",count);
        return String.format("购买成功，剩余库存为%d",count);
    }

    /**
     * 先更新数据库，再删除缓存
     */
    @ResponseBody
    @RequestMapping("/createOrderWithCacheV2/{sid}")
    public String createOrderWithCacheV2(@PathVariable int sid){
        int count = 0;
        try {
            // 完成扣库存下单事务
            orderService.createPessimisticOrder(sid);
            // 删除库存缓存
            stockService.delStockCountCache(sid);
        } catch (Exception e) {
            LOGGER.error("购买失败：[{}]", e.getMessage());
            return "购买失败，库存不足";
        }
        LOGGER.info("购买成功，剩余库存为: [{}]", count);
        return String.format("购买成功，剩余库存为：%d", count);
    }

    /**
     * 先删除缓存、再更新数据库、缓存延时双删
     * 采用线程池实现
     */
    private static final int DELAY_MILLSECONDS  = 1000;
    private static ExecutorService cachedThreadPool
                    = new ThreadPoolExecutor(1,
                                            Integer.MAX_VALUE,
                                            60L,
                                            TimeUnit.SECONDS,
                                            new SynchronousQueue<Runnable>());

    @ResponseBody
    @RequestMapping("/createOrderWithCacheV3/{sid}")
    public String createOrderWithCacheV3(@PathVariable int sid){
        int count;
        try {
            // 删除库存缓存
            stockService.delStockCountCache(sid);
            // 完成扣库存下单事务
            count = orderService.createPessimisticOrder(sid);
            // 延时指定时间后再次删除缓存
            cachedThreadPool.execute(new delCacheByThread(sid));
        } catch (Exception e) {//catch到检查库存抛出的异常
            LOGGER.error("购买失败：[{}]", e.getMessage());
            return "购买失败，库存不足";
        }
        LOGGER.info("购买成功，剩余库存为: [{}]", count);
        return String.format("购买成功，剩余库存为：%d", count);
    }

    /**
     * 缓存再删除线程
     */
    private class delCacheByThread implements Runnable{
        private int sid;
        delCacheByThread(int sid){
            this.sid = sid;
        }
        @Override
        public void run() {
            try {
                LOGGER.info("异步执行缓存再删除，商品id：[{}]， 首先休眠：[{}] 毫秒", sid, DELAY_MILLSECONDS);
                Thread.sleep(DELAY_MILLSECONDS);
                stockService.delStockCountCache(sid);
                LOGGER.info("再次删除商品id：[{}] 缓存", sid);
            } catch (Exception e) {
                LOGGER.info("延迟删除出错",e);
            }
        }
    }

    /**
     * 下单接口：先更新数据库，再删缓存，删除缓存重试机制
     * @param sid
     * @return
     */
    @RequestMapping("/createOrderWithCacheV4/{sid}")
    @ResponseBody
    public String createOrderWithCacheV4(@PathVariable int sid){
        int count;
        try {
            // 完成扣库存下单事务
            count = orderService.createPessimisticOrder(sid);
            // 删除库存缓存
            stockService.delStockCountCache(sid);
            // 延时指定时间后再次删除缓存
//             cachedThreadPool.execute(new delCacheByThread(sid));
            // 假设上述再次删除缓存没成功，通知消息队列进行删除缓存
            sendDelCache(String.valueOf(sid));
        }catch(Exception e){
            LOGGER.error("购买失败：[{}]", e.getMessage());
            return "购买失败，库存不足";
        }
        LOGGER.info("购买成功，剩余库存为: [{}]", count);
        return String.format("购买成功，剩余库存为：%d", count);
    }

    /**
     * 向消息队列delCache发送消息
     * @param message
     */
    @Autowired
    private AmqpTemplate rabbitTemplate;

    private void sendDelCache(String message) {

        LOGGER.info("这就去通知消息队列开始重试删除缓存：[{}]", message);
        this.rabbitTemplate.convertAndSend("delCache", message);
    }
}
