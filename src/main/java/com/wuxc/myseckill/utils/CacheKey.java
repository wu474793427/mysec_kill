package com.wuxc.myseckill.utils;

/**
 * @author wwz
 * @version 1.0
 * @date 2021/4/20 16:35
 */
public enum CacheKey {
    HASH_KEY("MIAOSHA_HASH"),
    LIMIT_KEY("MIAOSHA_LIMIT"),
    STOCK_COUNT("miaosha_v1_stock_count");

    private String key;
    private CacheKey(String key){
        this.key = key;
    }
    public String getKey(){
        return key;
    }
}
