package com.chris.coupon.feign;


import com.chris.coupon.feign.hystrix.TemplateClientHystrix;
import com.chris.coupon.vo.CommonResponse;
import com.chris.coupon.vo.CouponTemplateSDK;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 *
 * <h1>优惠券模版微服务 feign接口</h1>
 * */

@FeignClient(value="eureka-client-coupon-template", fallback = TemplateClientHystrix.class)
public interface TemplateClient {

    //查找所有可用的优惠券模版
    @RequestMapping(value="/coupon-template/template/sdk/all",
            method = RequestMethod.GET)
    CommonResponse<List<CouponTemplateSDK>> findAllUsableTemplate();

    @RequestMapping(value="/coupon-template/template/sdk/infos", method = RequestMethod.GET)
    CommonResponse<Map<Integer, CouponTemplateSDK>> findIdsTemplateSDK(@RequestParam("ids")
                    Collection<Integer> ids);
}
