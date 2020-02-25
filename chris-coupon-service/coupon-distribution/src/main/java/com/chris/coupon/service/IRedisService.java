package com.chris.coupon.service;

import com.chris.coupon.Exception.CouponException;
import com.chris.coupon.entity.Coupon;

import java.util.List;

/**
 * <h1>redis相关接口定义</h1>
 * 1.用户的三个优惠券 Cache操作
 * 2. 优惠券模版生成的优惠券码Cache操作
 * */
public interface IRedisService {

    /**
     * <h2>根据 userId 和状态找到缓存的优惠券列表数据</h2>
     * @param userId 用户 id
     * @param status 优惠券状态 {@link com.chris.coupon.constant.CouponStatus}
     * @return {@link Coupon}s, 注意, 可能会返回 null, 代表从没有过记录
     * */
    List<Coupon> getCachedCoupons(Long userId, Integer status);

    /**
     * <h2>保存空的优惠券列表到缓存中</h2>
     * @param userId 用户 id
     * @param status 优惠券状态列表
     * */
    void saveEmptyCouponListToCache(Long userId, List<Integer> status);


    /**
     * <h2>尝试从 Cache 中获取一个优惠券码</h2>
     * @param templateId 优惠券模板主键
     * @return 优惠券码
     * */
    String tryToAcquireCouponCodeFromCache(Integer templateId);


    /**
     * <h2>将优惠券保存到 Cache 中</h2>
     * @param userId 用户 id
     * @param coupons {@link Coupon}s
     * @param status 优惠券状态
     * @return 保存成功的个数
     * */
    Integer addCouponToCache(Long userId, List<Coupon> coupons, Integer status) throws CouponException;

}
