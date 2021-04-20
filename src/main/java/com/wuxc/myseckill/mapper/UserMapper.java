package com.wuxc.myseckill.mapper;

import com.wuxc.myseckill.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author wwz
 * @version 1.0
 * @date 2021/4/20 16:29
 */
@Mapper
public interface UserMapper {

    User selectByPrimaryKey(long longValue);
}
