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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
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

    @Override
    public List<CouponTemplateSDK> findAvailableTemplate(Long userId) {
        return null;
    }

    @Override
    public Coupon acquireTemplate(AcquireTemplateRequest request) throws CouponException {
        return null;
    }

    @Override
    public SettlementInfo settlement(SettlementInfo info) throws CouponException {
        return null;
    }
}
