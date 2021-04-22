package com.wuxc.myseckill.service.impl;

import com.wuxc.myseckill.entity.Stock;
import com.wuxc.myseckill.mapper.StockMapper;
import com.wuxc.myseckill.service.StockService;
import com.wuxc.myseckill.utils.CacheKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

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

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

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

    @Override
    public Stock getStockByIdForUpdate(int sid) {
        return stockMapper.selectByPrimaryKeyForUpdate(sid);
    }

    @Override
    public int getStockCountByDB(int sid) {
        Stock stock =  stockMapper.getStockCountByDB(sid);
        return stock.getCount() - stock.getSale();
    }

    @Override
    public Integer getStockCount(int sid) {
        Integer stockLeft;
        stockLeft = getStockCountByCache(sid);
        LOGGER.info("缓存中取得库存数:[{}]",stockLeft);
        if(stockLeft == null){
            stockLeft = getStockCountByDB(sid);
            LOGGER.info("缓存未命中，查询数据库并写入缓存");
            setStockCountCache(sid, stockLeft);
        }
        return stockLeft;
    }

    @Override
    public void delStockCountCache(int sid) {
        String hashKey = CacheKey.STOCK_COUNT.getKey() + "_" + sid;
        stringRedisTemplate.delete(hashKey);
        LOGGER.info("删除商品id:[{}]缓存",sid);
    }

    private Integer getStockCountByCache(int sid) {
        String hashKey = CacheKey.STOCK_COUNT.getKey() + "_" + sid;
        String countStr = stringRedisTemplate.opsForValue().get(hashKey);
        if(countStr != null){
            return Integer.parseInt(countStr);
        }else{
            return null;
        }
    }

    private void setStockCountCache(int sid, Integer stockLeft) {
        String hashKey = CacheKey.STOCK_COUNT.getKey() + "_" +sid;
        LOGGER.info("写入商品库存缓存: [{}] [{}]", hashKey, String.valueOf(stockLeft));
        stringRedisTemplate.opsForValue().set(hashKey,String.valueOf(stockLeft),3600, TimeUnit.SECONDS);
    }


}
