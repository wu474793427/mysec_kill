package com.wuxc.myseckill.service.impl;

import com.wuxc.myseckill.controller.OrderController;
import com.wuxc.myseckill.entity.Stock;
import com.wuxc.myseckill.entity.StockOrder;
import com.wuxc.myseckill.entity.User;
import com.wuxc.myseckill.mapper.StockOrderMapper;
import com.wuxc.myseckill.mapper.UserMapper;
import com.wuxc.myseckill.service.OrderService;
import com.wuxc.myseckill.service.StockService;

import com.wuxc.myseckill.utils.CacheKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author wwz
 * @version 1.0
 * @date 2021/4/16 15:17
 */
@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    StockService stockService;

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private StockOrderMapper orderMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private UserMapper userMapper;

    @Override
    public int createWrongOrder(int sid) {
        Stock stock = checkStock(sid);
        saleStock(stock);
        int id = createOrder(stock);
        return id;
    }

    //乐观锁实现
    @Override
    public int createOptimisticOrder(int sid) {
        //校验库存
        Stock stock = checkStock(sid);
        //乐观锁更新库存
        saleStockOptimistic(stock);
        //创建订单
        int id = createOrder(stock);
        return stock.getCount() - (stock.getSale() + 1);
    }

    private void saleStockOptimistic(Stock stock){
        LOGGER.info("查询数据库，更新库存");
        //实际上在此校验版本 并不是真正的版本号，这里校验的是sale属性
        int count = stockService.updateStockByOptimistic(stock);
        if(count == 0){
            throw new RuntimeException("并发更新库存失败，version不匹配");
        }
    }

    @Transactional(rollbackFor = Exception.class,propagation = Propagation.REQUIRED)
    @Override
    public int createPessimisticOrder(int sid) {
        //校验库存 （悲观锁 for update）
        Stock stock = checkStockForUpdate(sid);
        saleStock(stock);
        int id = createOrder(stock);
        return stock.getCount() - stock.getSale();
    }

    @Override
    public int createVerifiedOrder(Integer sid, Integer userId, String verifyHash) throws Exception {
        //验证是否在抢购时间内
        LOGGER.info("请自行验证是否在抢购时间内,假设此处验证成功");

        //验证verifyHash是否合法
        String hashKey = CacheKey.HASH_KEY.getKey() + "_" + sid + "_" + userId;
        String verifyHashInRedis = stringRedisTemplate.opsForValue().get(hashKey);
        if(!verifyHashInRedis.equals(verifyHash)){
            throw new Exception("hash值与redis中不符");
        }
        // 检查用户合法性
        User user = userMapper.selectByPrimaryKey(userId.longValue());
        if (user == null) {
            throw new Exception("用户不存在");
        }
        LOGGER.info("用户信息验证成功：[{}]", user.toString());

        // 检查商品合法性
        Stock stock = stockService.getStockById(sid);
        if (stock == null) {
            throw new Exception("商品不存在");
        }
        LOGGER.info("商品信息验证成功：[{}]", stock.toString());

        checkStock(sid);

        //乐观锁更新库存
        saleStockOptimistic(stock);
        LOGGER.info("乐观锁更新库存成功");

        //创建订单
        createOrderWithUserInfo(stock, userId);
        LOGGER.info("创建订单成功");

        return stock.getCount() - (stock.getSale()+1);
    }

    //检查库存
    private Stock checkStockForUpdate(int sid){
        Stock stock = stockService.getStockByIdForUpdate(sid);
        if(stock.getSale().equals(stock.getCount())){
            throw new RuntimeException("库存不足");
        }
        return stock;
    }




    private Stock checkStock(int sid){
        Stock stock = stockService.getStockById(sid);
        if(stock.getSale().equals(stock.getCount())){
            throw new RuntimeException("库存不足");
        }
        return stock;
    }
    private int saleStock(Stock stock){
        stock.setSale(stock.getSale() + 1);
        return stockService.updateStockById(stock);
    }
    private int createOrder(Stock stock){
        StockOrder order = new StockOrder();

        order.setSid(stock.getId());
        order.setName(stock.getName());
        int id = orderMapper.insertSelective(order);
        return id;
    }

    /**
     * 创建订单：保存用户信息到数据库
     * @param stock
     * @param userId
     */
    private int createOrderWithUserInfo(Stock stock,Integer userId){
        StockOrder order = new StockOrder();
        order.setSid(stock.getId());
        order.setName(stock.getName());
        order.setUserId(userId);
        return orderMapper.insertSelective(order);
    }


}
