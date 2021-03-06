package com.chris.coupon.service.impl;

import com.alibaba.fastjson.JSON;
import com.chris.coupon.Exception.CouponException;
import com.chris.coupon.constant.Constant;
import com.chris.coupon.constant.CouponStatus;
import com.chris.coupon.entity.Coupon;
import com.chris.coupon.service.IRedisService;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
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
import java.util.concurrent.TimeUnit;
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
                result = addCoupontoCacheForUsable(userId, coupons);
                break;
            case USED:
                result = addCoupontoCacheForUsed(userId, coupons);
                break;
            case EXPIRED:
                result = addCoupontoCacheForExpired(userId, coupons);
                break;
        }
        return result;
    }

    /**
     * <h1>新增加 可用优惠券到cache中</h1>
     * @return 优惠券个数
     * */
    private Integer addCoupontoCacheForUsable(Long userId, List<Coupon> coupons){
        //只会影响USER_COUPON_USABLE
        log.info("Add Coupon to Cache For Usable");
        Map<String, String> cachedObject = new HashMap<>();
        coupons.forEach(coupon -> {
            cachedObject.put(
                    coupon.getId().toString(), JSON.toJSONString(coupon)
            );
        });

        String redisKey = status2RedisKey(CouponStatus.USABLE.getCode(), userId);
        redisTemplate.opsForHash().putAll(redisKey, cachedObject);
        log.info("Add {} Coupons to Cache: {}, {}", cachedObject.size(), userId, redisKey);
        redisTemplate.expire(redisKey,
                getRandomExiprationTime(2,10),
                TimeUnit.SECONDS);
        return cachedObject.size();
    }

    /**
     * <h1>将已过期的优惠券加入cache</h1>
     * status 是EXPIRED， 代表已有的优惠券过期（Usable）， 影响到两个cache： usable 和 expired
     * */
    @SuppressWarnings("all")
    private Integer addCoupontoCacheForExpired(Long userId, List<Coupon> coupons) throws CouponException{
        log.debug("Add Coupon To Cache For Expired");
        //最终需要保存的cache
        Map<String, String> cacheForExpired = new HashMap<>(coupons.size());

        String redisKeyForUsable = status2RedisKey(CouponStatus.USABLE.getCode(), userId);
        String redisKeyForExpired = status2RedisKey(CouponStatus.EXPIRED.getCode(), userId);
        List<Coupon> curUsableCoupons = getCachedCoupons(userId, CouponStatus.USABLE.getCode());

        //可用的个数一定大于1
        assert  curUsableCoupons.size() > coupons.size();

        coupons.forEach(coupon -> {
            cacheForExpired.put(coupon.getId().toString(), JSON.toJSONString(coupon));
        });

        //校验当前优惠券参数是否与cache匹配
        List<Integer> curUsableIds = curUsableCoupons.stream().map(Coupon::getId).collect(Collectors.toList());
        List<Integer> paramIds = coupons.stream().map(Coupon::getId).collect(Collectors.toList());
        if(!CollectionUtils.isSubCollection(paramIds, curUsableIds)){
            log.error("Current Coupon is not equal to Cache:{},{},{}", userId, JSON.toJSONString(paramIds), JSON.toJSONString(curUsableIds));
            throw new CouponException("Current Coupon is not equal to Cache");
        }

        List<String> needCleanKey = paramIds.stream().map(i->i.toString()).collect(Collectors.toList());
        SessionCallback<Object> sessionCallback = new SessionCallback<Object>() {
            @Override
            public  Object execute(RedisOperations redisOperations) throws DataAccessException {
                //1. 已过期优惠券cache缓存
                redisOperations.opsForHash().putAll(redisKeyForExpired, cacheForExpired);

                //2. usable优惠券清理
                redisOperations.opsForHash().delete(redisKeyForUsable, needCleanKey.toArray());

                //3. 重置过期时间
                redisOperations.expire(redisKeyForUsable,
                        getRandomExiprationTime(2,10),
                        TimeUnit.SECONDS);

                redisOperations.expire(redisKeyForUsable, getRandomExiprationTime(2, 10), TimeUnit.SECONDS);
                return null;
            }
        };

        log.info("Pipline execute result:{}", JSON.toJSONString(redisTemplate.executePipelined(sessionCallback)));
        return cacheForExpired.size();
    }

    /***
     * <h1>将已使用的优惠券加入cache 若Status是used， 代表用户使用当前优惠券， 影响到两个cache：usable he used</h1>
     */

    @SuppressWarnings("all")
    private Integer addCoupontoCacheForUsed(Long userId, List<Coupon> coupons) throws  CouponException{
        log.debug("Add Coupon To Cache For Used");
        Map<String, String> cacheForUsed = new HashMap<>(coupons.size());

        String redisKeyForUsable = status2RedisKey(CouponStatus.USABLE.getCode(), userId);
        String redisKeyForUsed = status2RedisKey(CouponStatus.USED.getCode(), userId);

        //get all available coupon for current user
        List<Coupon> curUsableCoupons = getCachedCoupons(userId, CouponStatus.USABLE.getCode());

        //可用优惠券个数大于1
        assert curUsableCoupons.size() > coupons.size();
        coupons.forEach(coupon -> {
            cacheForUsed.put(coupon.getId().toString(), JSON.toJSONString(coupon));
        });
        //校验当前优惠券参数是否与cache中匹配

         List<Integer> curUsableIds = curUsableCoupons.stream().map(Coupon::getId).collect(Collectors.toList());
         List<Integer> paramIds = coupons.stream().map(Coupon::getId).collect(Collectors.toList());
         if(!CollectionUtils.isSubCollection(paramIds, curUsableIds)){
             log.error("Current Coupons is not equal to Cache:{}, {}, {}",
                     userId, JSON.toJSONString(curUsableIds), JSON.toJSONString(paramIds));
             throw new CouponException("Current Coupons is not equal to Cache!!");
         }

         List<String> needCleanKey = paramIds.stream()
                 .map(i -> i.toString()).collect(Collectors.toList());
         SessionCallback<Object> sessionCallback = new SessionCallback<Object>() {
             @Override
             public Object execute(RedisOperations redisOperations) throws DataAccessException {
                 //1. 已使用的优惠券 cache缓存添加
                 redisOperations.opsForHash().putAll(redisKeyForUsed, cacheForUsed);
                 //2. 可用优惠券 cache清理
                 redisOperations.opsForHash().delete(redisKeyForUsable, needCleanKey.toArray());

                 //3.重置过期时间
                 redisOperations.expire(redisKeyForUsable,
                         getRandomExiprationTime(2,10),
                         TimeUnit.SECONDS);
                 redisOperations.expire(redisKeyForUsed,
                         getRandomExiprationTime(2,10),
                         TimeUnit.SECONDS);
                 return null;
             }
         };

         log.info("PipeLine Execution Result:{}",
                 JSON.toJSONString(redisTemplate.executePipelined(sessionCallback)));
         return coupons.size();

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
