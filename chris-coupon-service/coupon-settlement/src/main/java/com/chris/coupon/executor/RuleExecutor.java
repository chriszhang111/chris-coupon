package com.chris.coupon.executor;


import com.chris.coupon.constant.RuleFlag;
import com.chris.coupon.vo.SettlementInfo;

/**
 * <h1>规则处理器接口定义</h1>
 * */
public interface RuleExecutor {

    /**
     * <h2>规则类型标记</h2>
     * @return {@link RuleFlag}
     * */
    RuleFlag ruleConfig();

    /**
     * <h2>规则计算方法实现</h2>
     * @Param settlement 包含选择的优惠券
     * @return 包含修正过的结算信息
     * */
    SettlementInfo computeRule(SettlementInfo settlement);
}
