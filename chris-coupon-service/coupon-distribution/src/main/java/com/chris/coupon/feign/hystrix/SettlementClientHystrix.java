package com.chris.coupon.feign.hystrix;

import com.chris.coupon.Exception.CouponException;
import com.chris.coupon.feign.SettlementClient;
import com.chris.coupon.vo.CommonResponse;
import com.chris.coupon.vo.SettlementInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SettlementClientHystrix implements SettlementClient{

    @Override
    public CommonResponse<SettlementInfo> computeRule(SettlementInfo settlement) throws CouponException {
        log.error("[eureka-client-coupon-settlement] computeRule request error");
        settlement.setEmploy(false);
        settlement.setCost(-1.0);
        return new CommonResponse<>(-1, "[eureka-client-coupon-settlement] computeRule request error",
                settlement);
    }
}
