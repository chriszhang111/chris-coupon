package com.chris.coupon.constant;

/**
 * <h1>通用常量定义</h1>
 * */
public class Constant {

    /** Kafka 消息topic
     * */
    public static final String TOPIC = "imooc_user_coupon_op";

    public static class RedisPrefix{
        public static final String COUPON_TEMPLATE = "imooc_coupon_template_code_";

        public static final String USER_COUPON_USABLE = "imooc_user_coupon_useable_";

        public static final String USER_COUPON_USED = "imooc_user_coupon_used_";

        public static final String USER_COUPON_EXPIRED = "imooc_user_coupon_expired_";
    }
}
