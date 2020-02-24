package com.chris.coupon.vo;

import com.chris.coupon.constant.PeriodType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

/**
* <h1>优惠券规则对象定义</h1>
*
* */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TemplateRule {

    /**
     * <h1>有效期规则</h1>
     */

    private Expiration expiration;

    private Discount discount;

    private Integer limitation;  // 每人最多领几张

    private Usage usage;

    private String weight; // 权重，可以和哪些优惠券叠加使用, 同一类不能叠加

    public boolean validate() {

        return expiration.validate() && discount.validate()
                && limitation > 0 && usage.validate()
                && StringUtils.isNotEmpty(weight);
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Expiration{
        private Integer period;  //对应periodType 的 code
        private Integer gap;     //有效间隔， 只对变动有效期有效
        private Long deadline;  // 失效日期
        boolean validate(){
            return null != PeriodType.of(period) && gap > 0 && deadline > 0;
        }
    }

    /**
     * <h1>折扣，与类型配合决定</h1>
     */

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Discount{
        private Integer quota; // 额度

        private Integer base; // 只对满减有效, 需要满多少才可用

        boolean validate(){
            return quota > 0 && base > 0;
        }
    }


    /**
     * <h1>使用范围</h1>
     * */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Usage{

        private String province;
        private String city;
        private String goodsType;  //商品类型  fruit, media ....

        boolean validate(){
            return StringUtils.isNotEmpty(province) && StringUtils.isNotEmpty(city);
        }
    }
}
