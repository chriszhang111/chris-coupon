package com.chris.coupon.service.impl;

import com.alibaba.fastjson.JSON;
import com.chris.coupon.Exception.CouponException;
import com.chris.coupon.constant.Constant;
import com.chris.coupon.constant.CouponStatus;
import com.chris.coupon.entity.Coupon;
import com.chris.coupon.service.IRedisService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <h1>Redis 相关的操作服务接口实现</h1>

 */
@Slf4j
@Service
public class RedisServiceImpl implements IRedisService{

    private final StringRedisTemplate redisTemplate;

    @Autowired
    public RedisServiceImpl(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * <h2>根据 userId 和状态找到缓存的优惠券列表数据</h2>
     * @param userId 用户 id
     * @param status 优惠券状态 {@link com.chris.coupon.constant.CouponStatus}
     * @return {@link Coupon}s, 注意, 可能会返回 null, 代表从没有过记录
     */
    @Override
    public List<Coupon> getCachedCoupons(Long userId, Integer status) {
        log.info("Get Coupons From Cache: {}, {}", userId, status);
        String redisKey = status2RedisKey(status, userId);

        List<String> couponStrs = redisTemplate.opsForHash().values(redisKey)
                .stream()
                .map(o -> Objects.toString(o, null))
                .collect(Collectors.toList());
        if(CollectionUtils.isEmpty(couponStrs)){
            saveEmptyCouponListToCache(userId, Collections.singletonList(status));
            return Collections.emptyList();
        }

        return couponStrs.stream()
                .map(cs -> JSON.parseObject(cs, Coupon.class))
                .collect(Collectors.toList());
    }



    /**
     * <h2>保存空的优惠券列表到缓存中</h2>
     * 目的: 避免缓存穿透
     * @param userId 用户 id
     * @param status 优惠券状态列表
     */
    @Override
    public void saveEmptyCouponListToCache(Long userId, List<Integer> status) {

        log.info("Save Empty List To Cache For User: {}, Status: {}",
                userId, JSON.toJSONString(status));
        Map<String, String> invalidCouponMap = new HashMap<>();
        invalidCouponMap.put("-1", JSON.toJSONString(Coupon.invalidCoupon()));

        //用户优惠券
        //K： status+ userId -> imooc_user_coupon_useable_10, imooc_user_coupon_expired_10...
        //V： hashmap:  {coupon_ID: Json(coupon.class)}

        //使用sessioncallback 把数据命令放入redis的pipeline, 批量操作
        SessionCallback<Object> sessionCallback = new SessionCallback<Object>() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {

                status.forEach(s -> {
                    String redisKey = status2RedisKey(s, userId);   //for example: imooc_user_coupon_useable_10
                    redisOperations.opsForHash().putAll(redisKey, invalidCouponMap);

                });
                return null;
            }
        };

        log.info("Pipeline Exe Result: {}",
                JSON.toJSONString(redisTemplate.executePipelined(sessionCallback)));
    }


    /**
     * <h2>尝试从 Cache 中获取一个优惠券码</h2>
     * @param templateId 优惠券<h1>模板</h1> 主键
     * @return 优惠券码
     */
    @Override
    public String tryToAcquireCouponCodeFromCache(Integer templateId) {
        String redisKey = String.format("%s%s",
                Constant.RedisPrefix.COUPON_TEMPLATE, templateId.toString());
        String couponCode = redisTemplate.opsForList().leftPop(redisKey);
        log.info("Acquire Coupon Code : {}, {}, {}", templateId, redisKey, couponCode);
        return couponCode;
    }

    /**
     * <h2>将优惠券保存到 Cache 中</h2>
     * @param userId  用户 id
     * @param coupons {@link Coupon}s
     * @param status  优惠券状态
     * @return 保存成功的个数
     */
    @Override
    public Integer addCouponToCache(Long userId, List<Coupon> coupons, Integer status)
            throws CouponException {

        log.info("Add Coupon To Cache: {}, {}, {}", userId, JSON.toJSONString(coupons), status);

        Integer result = -1;
        CouponStatus couponStatus = CouponStatus.of(status);
        switch (couponStatus){
            case USABLE:
                break;
            case USED:
                break;
            case EXPIRED:
                break;
        }
        return null;
    }


    private String status2RedisKey(Integer status, Long userId){
        String redisKey = null;
        CouponStatus couponstatus = CouponStatus.of(status);
        switch (couponstatus){
            case USED:
                redisKey = String.format("%s%s", Constant.RedisPrefix.USER_COUPON_USED, userId);
                break;
            case USABLE:
                redisKey = String.format("%s%s", Constant.RedisPrefix.USER_COUPON_USABLE, userId);
                break;
            case EXPIRED:
                redisKey = String.format("%s%s", Constant.RedisPrefix.USER_COUPON_EXPIRED, userId);
                break;
        }
        return redisKey;

    }

    /**
     * <h1>获取随机过期时间</h1>
     * 避免缓存雪崩， 可以同一时间失效
     * @return [min, max] 之间随机秒数
     * */
    private Long getRandomExiprationTime(Integer min, Integer max){
        return RandomUtils.nextLong(
                min * 60 * 60,
                max * 60 * 60
        );
    }
}
