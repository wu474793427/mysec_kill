package com.wuxc.myseckill.controller;

import com.wuxc.myseckill.service.StockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author wwz
 * @version 1.0
 * @date 2021/4/22 15:29
 */
@Controller
@RequestMapping("/S")
public class StockController {
    private static final Logger LOGGER = LoggerFactory.getLogger(StockController.class);

    @Autowired
    StockService stockService;
    /**
     * 查询库存 通过数据库查询
     */
    @RequestMapping("/getStockByDB/{sid}")
    @ResponseBody
    public String getStockByDb(@PathVariable int sid){
        int count;
        try{
            count = stockService.getStockCountByDB(sid);
        }catch (Exception e){
            LOGGER.error("查询库存失败：[{}]", e.getMessage());
            return "查询库存失败";
        }
        LOGGER.info("商品Id: [{}] 剩余库存为: [{}]", sid, count);
        return String.format("商品Id: %d 剩余库存为：%d", sid, count);
    }

    /**
     * 查询库存 通过缓存查询
     */
    @RequestMapping("/getStockByCache/{sid}")
    @ResponseBody
    public String getStockCountByCache(@PathVariable int sid){
        Integer count;
        try {
            count = stockService.getStockCount(sid);
        } catch (Exception e) {
            LOGGER.error("查询库存失败：[{}]", e.getMessage());
            return "查询库存失败";
        }
        LOGGER.info("商品Id: [{}] 剩余库存为: [{}]", sid, count);
        return String.format("商品Id: %d 剩余库存为：%d", sid, count);
    }
}
