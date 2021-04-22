package com.wuxc.myseckill.mapper;

import com.wuxc.myseckill.entity.Stock;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author wwz
 * @version 1.0
 * @date 2021/4/16 15:33
 */
@Mapper
public interface StockMapper {

    Stock selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Stock record);

    int updateByOptimistic(Stock stock);

    //对应的xml 中 for update 相当于对行记录加上行一个X锁 排他锁
    Stock selectByPrimaryKeyForUpdate(Integer id);

    Stock getStockCountByDB(Integer id);

}
