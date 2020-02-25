package com.chris.coupon.service;


import com.chris.coupon.Entity.CouponTemplate;
import com.chris.coupon.Exception.CouponException;
import com.chris.coupon.constant.CouponCategory;
import com.chris.coupon.constant.DistributeTarget;
import com.chris.coupon.constant.ProductLine;
import com.chris.coupon.vo.TemplateRequest;
import com.chris.coupon.vo.TemplateRule;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import java.util.*;

@SpringBootTest
@RunWith(SpringRunner.class)
@Slf4j
public class CouponTemplateTest {

    @Autowired
    private IBuildTemplateService buildTemplateService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Test
    public void testCreateTemplate() throws CouponException{
        TemplateRequest request = mockTemplateRequest();
        CouponTemplate template = buildTemplateService.buildTemplate(request);
//        Set<String> set = new HashSet<>();
//        set.add("123");
//        set.add("456");
//        set.add("789");
//        redisTemplate.opsForList().rightPushAll("imooc_coupon_template_code_0", set);
//        log.info("Completed!!!");

    }


    public TemplateRequest mockTemplateRequest(){
        TemplateRule rule = generateRule();
        TemplateRequest templateRequest = new TemplateRequest();
        templateRequest.setCategory(CouponCategory.ZHEKOU.getCode());
        templateRequest.setCount(15);
        templateRequest.setDesc("A Test Template For Coupon");
        templateRequest.setLogo("http://chrisz9.github.io");
        templateRequest.setName("Test Template19-"+new Date().getTime());
        templateRequest.setProductLine(ProductLine.DM.getCode());
        templateRequest.setTarget(DistributeTarget.MULTI.getCode());
        templateRequest.setUserId(2L);
        templateRequest.setRule(rule);
        return templateRequest;

    }

    public TemplateRule generateRule(){
        TemplateRule rule = new TemplateRule();
        TemplateRule.Expiration expiration = new TemplateRule.Expiration(1, 100, new Date(2021,1,1).getTime());
        TemplateRule.Usage usage = new TemplateRule.Usage("Beijing","Beijing","Frult");
        TemplateRule.Discount discount = new TemplateRule.Discount(10,100);

        rule.setExpiration(expiration);
        rule.setDiscount(discount);
        rule.setUsage(usage);
        rule.setWeight("list");
        rule.setLimitation(2);
        return rule;
    }
}
