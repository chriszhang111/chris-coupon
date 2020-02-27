package com.chris.coupon.schedule;

import com.alibaba.fastjson.JSON;
import com.chris.coupon.Dao.CouponTemplateDao;
import com.chris.coupon.Entity.CouponTemplate;
import com.chris.coupon.constant.Constant;
import com.chris.coupon.vo.ExpriedCouponTemplateKafka;
import com.chris.coupon.vo.TemplateRule;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <h1>定时清理已过期的模版</h1>
 * */

@Slf4j
@Component
public class ScheduledTask {

    private final CouponTemplateDao templateDao;

    private final StringRedisTemplate redisTemplate;

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    public ScheduledTask(CouponTemplateDao templateDao,
                         StringRedisTemplate redisTemplate,
                         KafkaTemplate<String, String> kafkaTemplate) {
        this.templateDao = templateDao;
        this.redisTemplate = redisTemplate;
        this.kafkaTemplate = kafkaTemplate;
    }


    public void offLineCouponTemplate(){
        log.info("Start to Expire Coupon Template");
        List<CouponTemplate> templates = templateDao.findAllByExpired(false);

        if(CollectionUtils.isEmpty(templates)){
            log.info("Done to Expire CouponTemplate");
            return;
        }

        Date cur = new Date();
        List<CouponTemplate> expried = new ArrayList<>(templates.size());
        templates.forEach(t->{
            //根据过期规则校验
            TemplateRule rule = t.getRule();
            if(rule.getExpiration().getDeadline() < cur.getTime()){
                t.setExpired(true);
                expried.add(t);
            }
        });

        if(CollectionUtils.isNotEmpty(expried)){
            log.info("Expried Coupon Templates Num:{}",
                    templateDao.saveAll(expried));
        }
        log.info("Done to Expire CouponTemplate");
    }

    @Scheduled(fixedRate = 60*60*1000)
    public void offLineCouponTemplateFromRedis(){
        log.info("Start to Expire Coupon Template");
        Set<String> set = redisTemplate.opsForHash().keys(Constant.RedisPrefix.COUPONTEMPLATE_EXPIRE).stream().map(k->k.toString()).collect(Collectors.toSet());
        List<Integer> exipredIds = new ArrayList<>();
        Long curTime = new Date().getTime();
        for(String key: set){
            Long time = JSON.parseObject((String)redisTemplate.opsForHash().get(Constant.RedisPrefix.COUPONTEMPLATE_EXPIRE, key), Long.class);
            if(time < curTime){
                redisTemplate.opsForHash().delete(Constant.RedisPrefix.COUPONTEMPLATE_EXPIRE, key);
                exipredIds.add(Integer.parseInt(key));
            }
        }
        if(exipredIds.size() > 0) {
            log.info("Send {} to Kafka, set these couponTemplate expire field to true", JSON.toJSONString(exipredIds));
            kafkaTemplate.send(Constant.COUPON_EXPIRE_TOPIC,
                    JSON.toJSONString(new ExpriedCouponTemplateKafka(exipredIds)));
            log.info("Done to Expire CouponTemplate");
        }else{
            log.info("No Expired CouponTemplate Has Been Found!");
        }

    }
}
