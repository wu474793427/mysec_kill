package com.wuxc.myseckill.service;

/**
 * @author wwz
 * @version 1.0
 * @date 2021/4/16 15:17
 */
public interface OrderService {

    int createWrongOrder(int sid);

    int createOptimisticOrder(int sid);

    int createPessimisticOrder(int sid);
}
