package com.wuxc.myseckill.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

/**
 * @author wwz
 * @version 1.0
 * @date 2021/4/16 15:27
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StockOrder {
    private Integer id;
    private Integer sid;
    private String name;
    private Timestamp createTime;
}
