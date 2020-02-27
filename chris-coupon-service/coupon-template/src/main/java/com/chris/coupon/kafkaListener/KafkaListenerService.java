package com.chris.coupon.kafkaListener;

import com.alibaba.fastjson.JSON;
import com.chris.coupon.Dao.CouponTemplateDao;
import com.chris.coupon.Entity.CouponTemplate;
import com.chris.coupon.constant.Constant;
import com.chris.coupon.vo.ExpriedCouponTemplateKafka;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Component;
import org.springframework.kafka.annotation.KafkaListener;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class KafkaListenerService {

    private final CouponTemplateDao couponTemplateDao;

    public KafkaListenerService(CouponTemplateDao couponTemplateDao) {
        this.couponTemplateDao = couponTemplateDao;
    }


    @KafkaListener(topics = {Constant.COUPON_EXPIRE_TOPIC}, groupId = "imooc-coupon-1")
    public void consumeCouponExpiredKafkaMessage(ConsumerRecord<?, ?> record) {
        log.info("Timed to Clear Expired CouponTemplate");
        Optional<?> kafkaMessage = Optional.ofNullable(record.value());
        if(kafkaMessage.isPresent()){
            Object message = kafkaMessage.get();
            ExpriedCouponTemplateKafka expired = JSON.parseObject(message.toString(), ExpriedCouponTemplateKafka.class);
            List<CouponTemplate> list = couponTemplateDao.findAllById(expired.getIds());
            list.forEach(c->c.setExpired(true));
            log.info("CouponKafkaMessage Couple Template Exipres Count: {}",
                    couponTemplateDao.saveAll(list).size());

        }
    }
}
