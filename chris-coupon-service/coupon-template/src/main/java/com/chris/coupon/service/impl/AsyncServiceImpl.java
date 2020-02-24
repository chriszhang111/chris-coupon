package com.chris.coupon.service.impl;

import com.chris.coupon.Dao.CouponTemplateDao;
import com.chris.coupon.Entity.CouponTemplate;
import com.chris.coupon.constant.Constant;
import com.chris.coupon.service.IAsyncService;
import com.google.common.base.Stopwatch;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;


import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * <h1>异步服务接口实现</h1>
 * */
@Slf4j
@Service
public class AsyncServiceImpl implements IAsyncService{


    private final CouponTemplateDao templateDao;
    private final StringRedisTemplate redisTemplate;

    @Autowired
    public AsyncServiceImpl(CouponTemplateDao templateDao, StringRedisTemplate redisTemplate) {
        this.templateDao = templateDao;
        this.redisTemplate = redisTemplate;
    }

    /***/
    @Async("getAsyncExecutor")
    @Override
    public void asyncConstructCouponByTemplate(CouponTemplate template) {
        Stopwatch watch = Stopwatch.createStarted();

        Set<String> couponCodes = buildCouponCode(template);

        //imooc_coupon_template_code_1
        String redisKey = String.format("%s%s", Constant.RedisPrefix.COUPON_TEMPLATE, template.getId().toString());
        log.info("RedisKey:{}", redisKey);
        //push to redis
        try {
            redisTemplate.opsForList().rightPushAll(redisKey, couponCodes);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            log.info("Push Coupon Code to Redis:{}");
            template.setAvailable(true);
            templateDao.save(template);
            watch.stop();
            log.info("Construct Coupon Code: {} ms", watch.elapsed(TimeUnit.MILLISECONDS));

            //TODO 发送短信通知运营人员

            log.info("CouponTemplate({}) is available", template.getId());
        }
        //redisTemplate.opsForValue().set("hahaha", "hehehe");
    }


    /** 构造优惠券码
     * 优惠券码18位
     * 前4位 产品线+类型
     * 中间六位 日期随机
     * 后八位 0-9随机数构成
     * @param template
     * @return Set<String> 与template.count 相同个数
     * */
    private Set<String> buildCouponCode(CouponTemplate template){
        Stopwatch watch = Stopwatch.createStarted();
        Set<String> result = new HashSet<>(template.getCount());

        //前4位生成
        String prefix4 = template.getProductLine().getCode().toString() + template.getCategory().getCode();
        String date = new SimpleDateFormat("yyMMdd").format(template.getCreateTime());
        for(int i=0;i!=template.getCount();i++){
            result.add(prefix4+buildCouponCodeSuffix(date));
        }

        while(result.size() < template.getCount()){
            result.add(prefix4+buildCouponCodeSuffix(date));
        }

        assert result.size() == template.getCount();
        watch.stop();
        log.info("Build Coupon Code Cost: {} ms", watch.elapsed(TimeUnit.MILLISECONDS));

        return result;
    }


    /**
     * 构造优惠券码后14位
     * @param date 创建优惠券码日期
     *
     * */
    private String buildCouponCodeSuffix(String date){

        char[] bases = new char[]{'1','2','3','4','5','6','7','8','9'};
        List<Character> chars = date.chars().mapToObj(e->(char)e).collect(Collectors.toList());
        Collections.shuffle(chars);
        String mid6 = chars.stream().
                map(Object::toString).collect(Collectors.joining());
        //last 8

        String suffix8 = RandomStringUtils.random(1, bases)
                +RandomStringUtils.randomNumeric(7);
        return mid6+suffix8;
    }


}
