package com.chris.coupon;

import com.chris.coupon.Exception.CouponException;
import com.chris.coupon.constant.CouponCategory;
import com.chris.coupon.service.IUserService;
import com.chris.coupon.vo.AcquireTemplateRequest;
import com.chris.coupon.vo.CouponTemplateSDK;
import com.chris.coupon.vo.TemplateRule;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {DistributionApplicationTests.class},  webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Slf4j
public class UserCouponTest {

    @Autowired
    IUserService userService;

    @Test
    public void testGetCoupon(){
        AcquireTemplateRequest requet = new AcquireTemplateRequest();
        requet.setUserId(10L);
        requet.setTemplateSDK(getSDK());
        try {
            userService.acquireTemplate(requet);
        } catch (CouponException e) {
            e.printStackTrace();
        }
    }

    public CouponTemplateSDK getSDK(){
        TemplateRule rule = generateRule();
        CouponTemplateSDK sdk = new CouponTemplateSDK();
        sdk.setRule(rule);
        sdk.setId(18);
        sdk.setCategory(CouponCategory.ZHEKOU.getCode());
        sdk.setKey("100220200224");
        return sdk;
    }

    @SuppressWarnings("all")
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
