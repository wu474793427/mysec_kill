package com.wuxc.myseckill.service.impl;

import com.wuxc.myseckill.controller.OrderController;
import com.wuxc.myseckill.entity.Stock;
import com.wuxc.myseckill.entity.StockOrder;
import com.wuxc.myseckill.mapper.StockOrderMapper;
import com.wuxc.myseckill.service.OrderService;
import com.wuxc.myseckill.service.StockService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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


}
