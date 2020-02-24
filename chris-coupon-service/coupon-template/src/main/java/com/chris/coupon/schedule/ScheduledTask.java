package com.chris.coupon.schedule;

import com.chris.coupon.Dao.CouponTemplateDao;
import com.chris.coupon.Entity.CouponTemplate;
import com.chris.coupon.vo.TemplateRule;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * <h1>定时清理已过期的模版</h1>
 * */

@Slf4j
@Component
public class ScheduledTask {

    private final CouponTemplateDao templateDao;

    @Autowired
    public ScheduledTask(CouponTemplateDao templateDao) {
        this.templateDao = templateDao;
    }

    @Scheduled(fixedRate = 60*60*1000)
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
}
