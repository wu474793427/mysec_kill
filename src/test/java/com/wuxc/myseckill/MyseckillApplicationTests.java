package com.wuxc.myseckill;

import com.wuxc.myseckill.entity.Stock;
import com.wuxc.myseckill.service.OrderService;
import com.wuxc.myseckill.service.StockService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class MyseckillApplicationTests {

    @Autowired
    StockService stockService;

    @Autowired
    OrderService orderService;

    @Test
    public void t1(){
        Stock stock = stockService.getStockById(1);
        System.out.println(stock.toString());
    }
    @Test
    public void t2(){
        int wrongOrder = orderService.createWrongOrder(1);
        System.out.println(wrongOrder);
    }
}
