package com.chris.coupon.feign.hystrix;

import com.chris.coupon.feign.TemplateClient;
import com.chris.coupon.vo.CommonResponse;
import com.chris.coupon.vo.CouponTemplateSDK;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class TemplateClientHystrix implements TemplateClient {

    @Override
    public CommonResponse<List<CouponTemplateSDK>> findAllUsableTemplate() {
        log.error("[eureka-client-coupon-template] findAllUsableTemplate request error");
        return new CommonResponse<>(-1,
                "[eureka-client-coupon-template] findAllUsableTemplate request error",
                Collections.emptyList());
    }

    @Override
    public CommonResponse<Map<Integer, CouponTemplateSDK>>
    findIdsTemplateSDK(Collection<Integer> ids) {
        log.error("[eureka-client-coupon-template] findIdsTemplateSDK request error");
        return new CommonResponse<>(-1,
                "[eureka-client-coupon-template] findIdsTemplateSDK request error",
                Collections.emptyMap());
    }
}
