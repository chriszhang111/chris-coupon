package com.chris.coupon.dao;

import com.chris.coupon.constant.CouponStatus;
import com.chris.coupon.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CouponDao extends JpaRepository<Coupon, Integer> {

    List<Coupon> findAllByUserIdAndStatus(Long userId, CouponStatus status);
}
