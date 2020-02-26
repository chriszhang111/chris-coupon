package com.chris.coupon.vo;

import com.chris.coupon.constant.CouponStatus;
import com.chris.coupon.constant.PeriodType;
import com.chris.coupon.entity.Coupon;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang.time.DateUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Date;


/**
 * <h1>用户优惠券的分类, 根据优惠券状态</h1>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CouponClassify {

    /** 可以使用的 */
    private List<Coupon> usable;

    /** 已使用的 */
    private List<Coupon> used;

    /** 已过期的 */
    private List<Coupon> expired;

    public static CouponClassify classify(List<Coupon> coupons){
        List<Coupon> usable = new ArrayList<>(coupons.size());
        List<Coupon> used = new ArrayList<>(coupons.size());
        List<Coupon> expired = new ArrayList<>(coupons.size());

        coupons.forEach(c -> {
            boolean isTimeExpire;
            long curTime = new Date().getTime();

            //regular 1:固定日期 2：变动日期
            if(c.getTemplateSDK().getRule().getExpiration().getPeriod().equals(PeriodType.REGULAR.getCode())){
                isTimeExpire = c.getTemplateSDK().getRule().getExpiration().getDeadline() <= curTime;
            }else{
                isTimeExpire = DateUtils.addDays(
                        c.getAssignTime(),
                        c.getTemplateSDK().getRule().getExpiration().getGap()
                ).getTime() <= curTime;
            }

            if(c.getStatus() == CouponStatus.USED){
                used.add(c);
            }else if(c.getStatus() == CouponStatus.EXPIRED || isTimeExpire){
                expired.add(c);
            }else{
                usable.add(c);
            }
        });
        return new CouponClassify(usable, used, expired);
    }
}
