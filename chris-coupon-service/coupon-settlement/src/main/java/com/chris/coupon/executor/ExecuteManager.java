package com.chris.coupon.executor;

import com.chris.coupon.Exception.CouponException;
import com.chris.coupon.constant.CouponCategory;
import com.chris.coupon.constant.RuleFlag;
import com.chris.coupon.vo.SettlementInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <h1>优惠券结算规则执行管理器</h1>
 * 根据用户请求， 找到对应的exectutor去做结算
 *
 * */
@Slf4j
@Component
//Beanpostprocess: 后置处理器
public class ExecuteManager implements BeanPostProcessor{

    /** 规则执行器映射*/
    private static Map<RuleFlag, RuleExecutor> executorIndex
             = new HashMap<>();

    /**
     * <h2>优惠券结算规则计算入口</h2>
     * @notice: 传递进来优惠券个数>=1
     * */
    public SettlementInfo computeRule(SettlementInfo settlement) throws CouponException{
        SettlementInfo result = null;

        //单类优惠券
        if(settlement.getCouponAndTemplateInfos().size() == 1){
            //get coupon type
            CouponCategory couponCategory = CouponCategory.of(settlement.getCouponAndTemplateInfos().get(0).getTemplate().getCategory());
            switch (couponCategory){
                case MANJIAN:
                    result = executorIndex.get(RuleFlag.MANJIAN).computeRule(settlement);
                    break;
                case ZHEKOU:
                    result = executorIndex.get(RuleFlag.ZHEKOU).computeRule(settlement);
                    break;
                case LIJIAN:
                    result = executorIndex.get(RuleFlag.LIJIAN).computeRule(settlement);
                    break;
            }
        }else{
            //多类优惠券
            List<CouponCategory> categories = new ArrayList<>();
            settlement.getCouponAndTemplateInfos().forEach(ct -> categories.add(CouponCategory.of(ct.getTemplate().getCategory())));
            if(categories.size() != 2){
                throw new CouponException("Not Support For More Template Category");
            }else{
                if(categories.contains(CouponCategory.MANJIAN) && categories.contains(CouponCategory.ZHEKOU)){
                    result = executorIndex.get(RuleFlag.MANJIAN_ZHEKOU).computeRule(settlement);
                }else{
                    throw new CouponException("Not Support For Other Template Category");
                }
            }
        }
        return result;
    }

    /**
     * <h2>bean初始化之前执行</h2>
     * */
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName)
            throws BeansException {

        if(!(bean instanceof RuleExecutor))
            return bean;
        RuleExecutor executor = (RuleExecutor) bean;
        RuleFlag ruleFlag = executor.ruleConfig();
        if(executorIndex.containsKey(ruleFlag)){
            throw new IllegalStateException("There is already has an executor "+"for fule flag: "+ruleFlag);
        }
        log.info("Load executor {} for rule flag {}.", executor.getClass(), ruleFlag);
        executorIndex.put(ruleFlag, executor);
        return null;
    }

    /**
     * <h2>bean初始化之后执行</h2>
     * */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName)
            throws BeansException {
        return bean;
    }
}
