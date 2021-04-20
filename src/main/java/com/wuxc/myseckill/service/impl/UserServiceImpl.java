package com.wuxc.myseckill.service.impl;


import com.wuxc.myseckill.entity.Stock;
import com.wuxc.myseckill.entity.User;

import com.wuxc.myseckill.mapper.UserMapper;
import com.wuxc.myseckill.service.StockService;
import com.wuxc.myseckill.service.UserService;

import com.wuxc.myseckill.utils.CacheKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author wwz
 * @version 1.0
 * @date 2021/4/20 16:25
 */
@Service
public class UserServiceImpl implements UserService {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    private static final String SALT = "randomString";

    private static final int ALLOW_COUNT = 10;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    UserMapper userMapper;
    @Autowired
    StockService stockService;

    //生成验证hash
    @Override
    public String getVerifyHash(Integer sid, Integer userId) throws Exception {
        //验证是否在抢购时间内 自行实现
        LOGGER.info("自行验证是否在抢购时间内");
        //检查用户合法性
        User user = userMapper.selectByPrimaryKey(userId.longValue());
        if(user == null){
            throw new Exception("用户不存在");
        }
        LOGGER.info("用户信息：[{}]",user.toString());
        // 检查商品合法性
        Stock stock = stockService.getStockById(sid);
        if(stock == null){
            throw new Exception("商品不存在");
        }
        LOGGER.info("商品信息：[{}]",stock.toString());

        //生成hash
        //加盐防破解
        String verify = SALT + sid + userId;
        String verifyHash = DigestUtils.md5DigestAsHex(verify.getBytes());

        //将hash和用户商品信息存入redis
        //根据用户ID和商品ID 生成hashkey 加入自定义的hash_key盐 防止被破解
        String hashKey = CacheKey.HASH_KEY.getKey() + "_" + sid + "_" + userId;
        stringRedisTemplate.opsForValue().set(hashKey,verifyHash,3600, TimeUnit.SECONDS);
        LOGGER.info("Redis写入[{}]  [{}]",hashKey,verifyHash);

        return verifyHash;
    }

    //TODO limitNum的原子性保证尚未实现
    @Override
    public int addUserCount(Integer userId) {
        String limitKey = CacheKey.LIMIT_KEY.getKey() + "_" + userId;
        String limitNum = stringRedisTemplate.opsForValue().get(limitKey);
        int limit = -1;
//        AtomicInteger limit = new AtomicInteger(Integer.MIN_VALUE);
        if (limitNum == null) {
            stringRedisTemplate.opsForValue().set(limitKey, "0", 3600, TimeUnit.SECONDS);
        } else {
//            limit.set(Integer.parseInt(limitNum));
//            limit.incrementAndGet();
            limit = Integer.parseInt(limitNum) + 1;
            stringRedisTemplate.opsForValue().set(limitKey, String.valueOf(limit), 3600, TimeUnit.SECONDS);
        }
        return limit;
    }

    @Override
    public boolean getUserIsBanned(Integer userId) {
        String limitKey = CacheKey.LIMIT_KEY.getKey() + "_" + userId;
        String limitNum = stringRedisTemplate.opsForValue().get(limitKey);
        if (limitNum == null) {
            LOGGER.error("该用户没有访问申请验证值记录，疑似异常");
            return true;
        }
        return Integer.parseInt(limitNum) > ALLOW_COUNT;
    }

}
