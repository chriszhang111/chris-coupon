package com.chris.coupon.serialzation;


import com.alibaba.fastjson.JSON;
import com.chris.coupon.Entity.CouponTemplate;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.text.SimpleDateFormat;

public class CouponTemplateSerializer extends JsonSerializer<CouponTemplate>{

    @Override
    public void serialize(CouponTemplate couponTemplate,
                          JsonGenerator generator,
                          SerializerProvider serializerProvider) throws IOException {
        generator.writeStartObject();

        generator.writeStringField("id", couponTemplate.getId().toString());
        generator.writeStringField("name", couponTemplate.getName());
        generator.writeStringField("logo", couponTemplate.getLogo());
        generator.writeStringField("desc", couponTemplate.getDesc());
        generator.writeStringField("category", couponTemplate.getCategory().getDescription());
        generator.writeStringField("productLine", couponTemplate.getProductLine().getDescription());
        generator.writeStringField("count", couponTemplate.getCount().toString());
        generator.writeStringField("createTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(couponTemplate.getCreateTime()));
        generator.writeStringField("userId", couponTemplate.getUserId().toString());
        generator.writeStringField("key", couponTemplate.getKey()+String.format("%4d", couponTemplate.getId()));
        generator.writeStringField("target", couponTemplate.getTarget().getDesrciption());
        generator.writeStringField("rule", JSON.toJSONString(couponTemplate.getRule()));
        generator.writeEndObject();
    }
}
