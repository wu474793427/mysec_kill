package com.wuxc.myseckill.service;

import com.wuxc.myseckill.entity.Stock;

/**
 * @author wwz
 * @version 1.0
 * @date 2021/4/16 15:29
 */
public interface StockService {
    Stock getStockById(int sid);

    int updateStockById(Stock stock);

    int updateStockByOptimistic(Stock stock);
}
