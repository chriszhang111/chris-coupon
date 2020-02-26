package com.chris.coupon.service.impl;


import com.alibaba.fastjson.JSON;
import com.chris.coupon.Exception.CouponException;
import com.chris.coupon.constant.Constant;
import com.chris.coupon.constant.CouponStatus;
import com.chris.coupon.dao.CouponDao;
import com.chris.coupon.entity.Coupon;
import com.chris.coupon.feign.SettlementClient;
import com.chris.coupon.feign.TemplateClient;
import com.chris.coupon.service.IKafkaService;
import com.chris.coupon.service.IRedisService;
import com.chris.coupon.service.IUserService;
import com.chris.coupon.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <h1>用户服务相关接口实现</h1>
 *
 * 所有的操作过程和状态都保存在redis中， 通过kafka把消息传递到MySQL中
 * 为什么使用Kafka，不直接使用springBoot异步处理？
 * 安全性： 异步任务有可能失败   kafka保证一致性
 * */

@Slf4j
@Service
public class UserServiceImpl implements IUserService{

    private final IRedisService redisService;
    private final CouponDao couponDao;
    private final TemplateClient templateClient;  //模版微服务客户端
    private final SettlementClient settlementClient;
    private final KafkaTemplate<String, String> kafkaTemplate;  //kafka客户端

    @Autowired
    public UserServiceImpl(IRedisService redisService,
                           CouponDao couponDao,
                           TemplateClient templateClient,
                           SettlementClient settlementClient,
                           KafkaTemplate<String, String> kafkaTemplate) {
        this.redisService = redisService;
        this.couponDao = couponDao;
        this.templateClient = templateClient;
        this.settlementClient = settlementClient;
        this.kafkaTemplate = kafkaTemplate;
    }


    /***
     * <h2>根据用户id和状态， 查询优惠券记录</h2>
     * 1.首先从redis中获取
     * 2.如果redis中对应记录为空， 从mysql中获取
     * 3.如果数据库中没有记录， 直接返回， 此时redis中已通过 getCachedCoupons() -> saveEmptyCouponListToCache()
     * 方法添加了一个无效优惠券， 避免缓存穿透
     * 4.如果数据库中有记录， 有可能redis key过期， 或第一次查询
     * 将 CouponTemplateSDK 填充进 从数据库得到的 List<Coupon> dbCoupons
     * 5.写会缓存
     * 6. 如果是获取可用优惠券usable， 将无效优惠券剔除， 通过 CouponClassify
     * 7.若有过期优惠券， 写回cache， 并发送给kafka异步处理
     *
     */

    @Override
    public List<Coupon> findCouponsByStatus(Long userId, Integer status) throws CouponException {
        //首先尝试从redis 中获取
        List<Coupon> curCached = redisService.getCachedCoupons(userId, status);
        List<Coupon> preTarget;

        if(CollectionUtils.isNotEmpty(curCached)){
            log.debug("coupon cache is not empty: {}, {}", userId, status);
            preTarget = curCached;
        }else{
            //redis 中记录为空

            log.debug("coupon cache is empty, get coupon from DB: {}, {}", userId, status);
            List<Coupon> dbCoupons = couponDao.findAllByUserIdAndStatus(userId, CouponStatus.of(status));
            //如果数据库没有记录, 直接返回， cache中已经加入了一张无效的优惠券
            if(CollectionUtils.isEmpty(dbCoupons)){
                log.debug("Current user does not have coupon: {}, {}", userId, status);
                return dbCoupons;
            }
            else{
                //有可能缓存过期， 填充dbCoupons的templateSDK字段
                Map<Integer, CouponTemplateSDK> id2TemplateSDK =
                        templateClient.findIdsTemplateSDK(
                                dbCoupons.stream().map(Coupon::getTemplateId)
                                .collect(Collectors.toList())
                        ).getData();
                dbCoupons.forEach(dc -> {
                    dc.setTemplateSDK(id2TemplateSDK.get(dc.getTemplateId()));
                });

                //数据库中存在记录
                preTarget = dbCoupons;

                //写回缓存
                redisService.addCouponToCache(userId, dbCoupons, status);

            }
        }

        // 将无效优惠券剔除
        preTarget = preTarget.stream().filter(c -> c.getId() != -1).collect(Collectors.toList());
        //如果当前获取的是【可用】优惠券， 还需要对已过期优惠券延迟处理， 剔除已过期的优惠券
        if(CouponStatus.of(status) == CouponStatus.USABLE){
            //将pretargt中的优惠券分成三类
            CouponClassify classify = CouponClassify.classify(preTarget);
            //若已过期状态不为空， 需要延迟处理
            if(CollectionUtils.isNotEmpty(classify.getExpired())){
                log.info("Add Expired Coupons to Cache from findCouponByStatus in distribution {}, {}", userId, status);
                redisService.addCouponToCache(userId, classify.getExpired(), CouponStatus.EXPIRED.getCode());

                //过期清除策略发送的kafka中异步处理， 更改mysql中的数据
                kafkaTemplate.send(Constant.TOPIC, JSON.toJSONString(
                        new CouponKafkaMessage(
                                CouponStatus.EXPIRED.getCode(),
                                classify.getExpired().
                                        stream().
                                        map(Coupon::getId).
                                        collect(Collectors.toList()))));
            }
            return classify.getUsable();
        }
        return preTarget;
    }


    /**
     * <h2>根据用户id, 查找当前可以领取的优惠券模版</h2>
     * */
    @Override
    public List<CouponTemplateSDK> findAvailableTemplate(Long userId)
            throws CouponException{
         long curTime = new Date().getTime();
         List<CouponTemplateSDK> templateSDKS = templateClient.findAllUsableTemplate().getData();
         log.debug("Find All Template From Template Client count:{}", templateSDKS.size());
         //过滤过期的优惠券模版
        templateSDKS = templateSDKS.stream().filter(t -> t.getRule().getExpiration().getDeadline() > curTime).collect(Collectors.toList());
        log.info("Find Available Template Count: {}", templateSDKS.size());

        //key: template ID, value: Pair<template 中的 limitation， CTSDK>
        Map<Integer, Pair<Integer, CouponTemplateSDK>> limit2Template = new HashMap<>(templateSDKS.size());

        templateSDKS.forEach( t -> limit2Template.put(
                t.getId(), Pair.of(t.getRule().getLimitation(), t)
        ));

        List<CouponTemplateSDK> result = new ArrayList<>(limit2Template.size());
        List<Coupon> userUsableCoupons = findCouponsByStatus(userId, CouponStatus.USABLE.getCode());
        log.debug("Current User Has Usable Coupons: {}, {}", userId, userUsableCoupons.size());
        // key: 模版ID
        Map<Integer, List<Coupon>> templateId2Coupons = userUsableCoupons.stream()
                .collect(Collectors.groupingBy(Coupon::getTemplateId));
        //根据template 的 Rule 判断是否可以领取
        limit2Template.forEach((k,v) -> {
            //k: template ID
            int limitation = v.getLeft();
            CouponTemplateSDK sdk = v.getRight();
            if(templateId2Coupons.containsKey(k) &&
                    templateId2Coupons.get(k).size() >= limitation){
                return;
            }
            result.add(sdk);
        });


        return result;
    }

    /**
     * <h1>用户领取优惠券</h1>
     *
     * 1.从TemplateClient 拿到对应的优惠券， 并检查是否过期， 根据limitation判断是否可以领取
     * 2。 save to db
     * 3。填充CouponTemplate SDK
     * 4。save to cache
     * */
    @Override
    public Coupon acquireTemplate(AcquireTemplateRequest request) throws CouponException {
        Map<Integer, CouponTemplateSDK> id2Template =
                templateClient.findIdsTemplateSDK(Collections.singletonList(request.getTemplateSDK().getId())).getData();
        // 优惠券模版需要存在
        if(id2Template.size() == 0){
            log.error("Can Not Acquire Template from TemplateClient: {}", request.getTemplateSDK().getId());
            throw new CouponException("Can Not Acquire Template from TemplateClient");
        }
        //用户是否可以领取
        List<Coupon> userUsableCoupons = findCouponsByStatus(request.getUserId(), CouponStatus.USABLE.getCode());
        Map<Integer, List<Coupon>> templateId2Coupons =
                userUsableCoupons.stream().collect(Collectors.groupingBy(Coupon::getTemplateId));


        if(templateId2Coupons.containsKey(request.getTemplateSDK().getId()) &&
                templateId2Coupons.get(request.getTemplateSDK().getId()).size() >= request.getTemplateSDK().getRule().getLimitation()){
            log.error("Exceed Template Assign Limitation:{}", request.getTemplateSDK().getId());
            throw new CouponException("Exceed Template Assign Limitation");
        }

        //尝试获取优惠券码
        String couponCode = redisService.tryToAcquireCouponCodeFromCache(request.getTemplateSDK().getId());
        if(StringUtils.isEmpty(couponCode)){
            log.error("Can Not Acquire Coupon Code: {}", request.getTemplateSDK().getId());
            throw new CouponException("Can Not Acquire Coupon Code");
        }

        Coupon newCoupon = new Coupon(request.getTemplateSDK().getId(),request.getUserId(), couponCode, CouponStatus.USABLE);
        newCoupon = couponDao.save(newCoupon);
        //填充coupon 对象的 couponSDK， 要在放入缓存前填充
        newCoupon.setTemplateSDK(request.getTemplateSDK());
        //放入缓存
        redisService.addCouponToCache(request.getUserId(), Collections.singletonList(newCoupon), CouponStatus.USABLE.getCode());
        return newCoupon;
    }

    /**
     *
     * <h2>规则相关处理由Settlement系统去做</h2>
     * 当前仅仅做业务处理
     * */
    @Override
    public SettlementInfo settlement(SettlementInfo info) throws CouponException {

        //当没有传递优惠券， 直接返回商品总价
        List<SettlementInfo.CouponAndTemplateInfo> ctInfos =
                info.getCouponAndTemplateInfos();
        if(CollectionUtils.isEmpty(ctInfos)){
            log.info("Empty Coupons for Settlement");
            double goodsSum = 0.0;
            for(GoodsInfo good: info.getGoodsInfos()){
                goodsSum += good.getPrice() * good.getCount();
            }
            //不存在优惠券核销
            info.setCost(retain2Decimals(goodsSum));
        }

        //校验传递的优惠券是否是用户自己的
        List<Coupon> coupons = findCouponsByStatus(
                info.getUserId(), CouponStatus.USABLE.getCode()
        );

        //key: coupon ID value: Coupon
        Map<Integer, Coupon> id2Coupon = coupons.stream()
                .collect(Collectors.toMap(
                        Coupon::getId,
                        Function.identity()
                ));

        //用户可用优惠券为空， 或者传递进来的优惠券 不是可用优惠券的子集， 错误
        if(MapUtils.isEmpty(id2Coupon) || !CollectionUtils.isSubCollection(
                ctInfos.stream().map(SettlementInfo.CouponAndTemplateInfo::getId).collect(Collectors.toList()),
                id2Coupon.keySet()
        )){
            log.info("{}", id2Coupon.keySet());
            log.error("User Coupon Has Some Problem");
            throw new CouponException("User Coupon Has Some Problem");
        }

        log.debug("Current Settlement Coupons Is Available");
        List<Coupon> settleCoupons = new ArrayList<>(ctInfos.size());
        ctInfos.forEach( ct -> settleCoupons.add(id2Coupon.get(ct.getId())));

        //通过结算服务 获取结算信息
        SettlementInfo processInfo = settlementClient.computeRule(info).getData();
        if(processInfo.getEmploy() && CollectionUtils.isNotEmpty(processInfo.getCouponAndTemplateInfos())){
            log.info("Settle User Coupon : {}, {}", info.getUserId(),
                    JSON.toJSONString(settleCoupons));
            //
            redisService.addCouponToCache(
                    info.getUserId(),
                    settleCoupons,
                    CouponStatus.USED.getCode()
            );

            //通过kafka，更新db
            kafkaTemplate.send(
                    Constant.TOPIC,
                    JSON.toJSONString(new CouponKafkaMessage(
                            CouponStatus.USED.getCode(),
                            settleCoupons.stream().map(Coupon::getId).collect(Collectors.toList())
                    ))
            );
        }
        return processInfo;
    }

    //保留两位小数
    private double retain2Decimals(double value){
        return new BigDecimal(value).setScale(2, BigDecimal.ROUND_HALF_UP)
                .doubleValue();
    }
}
