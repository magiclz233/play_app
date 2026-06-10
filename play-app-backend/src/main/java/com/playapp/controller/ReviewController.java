package com.playapp.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.playapp.common.Result;
import com.playapp.entity.Review;
import com.playapp.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    /** 获取某陪玩的评价列表（公开） */
    @GetMapping("/api/companions/{companionId}/reviews")
    public Result<Page<Review>> getCompanionReviews(
            @PathVariable Long companionId,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "20") Integer size) {
        return Result.success(reviewService.getReviewsByCompanion(companionId, current, size));
    }

    /** 陪玩回复评价 */
    @PostMapping("/api/reviews/{reviewId}/reply")
    public Result<?> replyReview(@AuthenticationPrincipal Long userId,
                                 @PathVariable Long reviewId,
                                 @RequestBody Map<String, String> body) {
        reviewService.replyReview(userId, reviewId, body.get("content"));
        return Result.success("回复成功");
    }
}
