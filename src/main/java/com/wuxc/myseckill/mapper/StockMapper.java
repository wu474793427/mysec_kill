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
}
