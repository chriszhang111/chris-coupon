package com.chris.coupon.controller;

import com.alibaba.fastjson.JSON;
import com.chris.coupon.Entity.CouponTemplate;
import com.chris.coupon.Exception.CouponException;
import com.chris.coupon.service.IBuildTemplateService;
import com.chris.coupon.service.ITemplateBaseService;
import com.chris.coupon.vo.CouponTemplateSDK;
import com.chris.coupon.vo.TemplateRequest;
import com.fasterxml.jackson.databind.JsonSerializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
public class CouponTemplateController {

    private final IBuildTemplateService buildTemplateService;
    private final ITemplateBaseService templateBaseService;

    @Autowired
    public CouponTemplateController(IBuildTemplateService buildTemplateService, ITemplateBaseService templateBaseService) {
        this.buildTemplateService = buildTemplateService;
        this.templateBaseService = templateBaseService;
    }

    // 127.0.0.1:7001/coupon-template/template/build
    //127.0.0.1:9000/chris/coupon-template/template/build
    @PostMapping("/template/build")
    public CouponTemplate buildTemplate(@RequestBody TemplateRequest request) throws CouponException{
        log.info("Build Template: {}", JSON.toJSONString(request));
        return buildTemplateService.buildTemplate(request);
    }

    @GetMapping("/template/info")
    public CouponTemplate buildTemplateInfo(@RequestParam("id") Integer id) throws CouponException{
        log.info("Build Template Info for: {}", id);
        return templateBaseService.buildTemplateInfo(id);
    }

    @GetMapping("/template/sdk/all")
    public List<CouponTemplateSDK> findAllUsableTemplate(){
        log.info("Get All Usable Coupon Template");
        return templateBaseService.findAllUsableTemplate();
    }

    @GetMapping("template/sdk/infos")
    public Map<Integer, CouponTemplateSDK> findIdsTemplateSDK(
            @RequestParam("ids")
            Collection<Integer> ids){
        log.info("findids:{}", JSON.toJSONString(ids));
        return templateBaseService.findIds2TemplateSDK(ids);
    }

}
