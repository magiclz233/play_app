package com.playapp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.playapp.common.BusinessException;
import com.playapp.common.ErrorCode;
import com.playapp.dto.ReviewDTO;
import com.playapp.entity.CompanionProfile;
import com.playapp.entity.Order;
import com.playapp.entity.Review;
import com.playapp.mapper.CompanionProfileMapper;
import com.playapp.mapper.ReviewMapper;
import com.playapp.service.OrderService;
import com.playapp.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl extends ServiceImpl<ReviewMapper, Review> implements ReviewService {

    private final OrderService orderService;
    private final CompanionProfileMapper companionProfileMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitReview(Long userId, String orderNo, ReviewDTO dto) {
        Order order = orderService.getOrderDetail(userId, orderNo);

        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "只能评价自己的订单");
        }
        if (order.getStatus() != Order.STATUS_USER_CONFIRMED && order.getStatus() != Order.STATUS_SETTLED) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "该订单尚未完成，暂不能评价");
        }

        // 检查是否已评价
        long count = this.count(new LambdaQueryWrapper<Review>().eq(Review::getOrderId, order.getOrderId()));
        if (count > 0) {
            throw new BusinessException(ErrorCode.REVIEW_ALREADY_EXISTS, "该订单已评价");
        }

        Review review = new Review();
        review.setOrderId(order.getOrderId());
        review.setUserId(userId);
        review.setCompanionId(order.getCompanionId());
        review.setRating(dto.getRating());
        review.setContent(dto.getContent());
        review.setImages(dto.getImages());
        review.setStatus(1);

        this.save(review);

        // 更新助教平均评分
        updateCompanionRating(order.getCompanionId(), dto.getRating());

        log.info("用户提交了评价, orderNo: {}, rating: {}", orderNo, dto.getRating());
    }

    /**
     * 更新助教评分：累加总分和评分次数，重新计算平均分
     */
    private void updateCompanionRating(Long companionId, Integer newRating) {
        CompanionProfile profile = companionProfileMapper.selectById(companionId);
        if (profile == null) return;

        int currentCount = profile.getTotalRatingCount() != null ? profile.getTotalRatingCount() : 0;
        int currentScore = profile.getTotalRatingScore() != null ? profile.getTotalRatingScore() : 0;

        int newCount = currentCount + 1;
        int newScore = currentScore + newRating;
        BigDecimal avgRating = new BigDecimal(newScore).divide(new BigDecimal(newCount), 1, RoundingMode.HALF_UP);

        profile.setTotalRatingCount(newCount);
        profile.setTotalRatingScore(newScore);
        profile.setRating(avgRating);
        companionProfileMapper.updateById(profile);

        log.info("助教 {} 评分更新: 次数={}, 均分={}", companionId, newCount, avgRating);
    }
}
