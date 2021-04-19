package com.wuxc.myseckill.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author wwz
 * @version 1.0
 * @date 2021/4/16 15:19
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Stock {
    private Integer id;

    private String name;

    private Integer count;

    private Integer sale;

    private Integer version;
}
