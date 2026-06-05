package com.playapp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.playapp.dto.ReviewDTO;
import com.playapp.entity.Review;

public interface ReviewService extends IService<Review> {
    
    /**
     * 提交评价
     */
    void submitReview(Long userId, String orderNo, ReviewDTO dto);
}
