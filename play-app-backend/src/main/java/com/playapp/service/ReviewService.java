package com.playapp.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.playapp.dto.ReviewDTO;
import com.playapp.entity.Review;

public interface ReviewService extends IService<Review> {

    /** 提交评价 */
    void submitReview(Long userId, String orderNo, ReviewDTO dto);

    /** 获取陪玩的评价列表 */
    Page<Review> getReviewsByCompanion(Long companionId, Integer current, Integer size);

    /** 陪玩回复评价 */
    void replyReview(Long companionId, Long reviewId, String content);
}
