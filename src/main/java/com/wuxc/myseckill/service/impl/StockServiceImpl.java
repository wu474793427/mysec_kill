package com.wuxc.myseckill.service.impl;

import com.wuxc.myseckill.entity.Stock;
import com.wuxc.myseckill.mapper.StockMapper;
import com.wuxc.myseckill.service.StockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author wwz
 * @version 1.0
 * @date 2021/4/16 15:30
 */
@Service
public class StockServiceImpl implements StockService {

    private static final Logger LOGGER = LoggerFactory.getLogger(StockServiceImpl.class);

    @Autowired
    private StockMapper stockMapper;

    @Override
    public Stock getStockById(int sid) {
        return stockMapper.selectByPrimaryKey(sid);
    }

    @Override
    public int updateStockById(Stock stock) {
        return stockMapper.updateByPrimaryKeySelective(stock);
    }

    @Override
    public int updateStockByOptimistic(Stock stock) {
        return stockMapper.updateByOptimistic(stock);
    }
}
