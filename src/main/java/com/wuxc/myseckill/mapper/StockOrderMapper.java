package com.wuxc.myseckill.mapper;

import com.wuxc.myseckill.entity.StockOrder;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author wwz
 * @version 1.0
 * @date 2021/4/16 15:47
 */
@Mapper
public interface StockOrderMapper {

    int insertSelective(StockOrder order);
}
