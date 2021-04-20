package com.wuxc.myseckill.service;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author wwz
 * @version 1.0
 * @date 2021/4/20 16:24
 */
public interface UserService {
    String getVerifyHash(Integer sid,Integer userId) throws Exception;

    int addUserCount(Integer userId);

    boolean getUserIsBanned(Integer userId);
}
