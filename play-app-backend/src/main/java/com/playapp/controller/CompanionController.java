package com.playapp.controller;

import com.playapp.common.Result;
import com.playapp.dto.CompanionApplyDTO;
import com.playapp.entity.CompanionProfile;
import com.playapp.service.CompanionProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/companion")
@RequiredArgsConstructor
public class CompanionController {

    private final CompanionProfileService companionProfileService;

    /**
     * 提交助教入驻申请
     */
    @PostMapping("/apply")
    public Result<?> apply(@AuthenticationPrincipal Long userId, @Valid @RequestBody CompanionApplyDTO applyDTO) {
        companionProfileService.submitApplication(userId, applyDTO);
        return Result.success("申请提交成功，请等待管理员审核");
    }

    /**
     * 查询个人的入驻状态
     */
    @GetMapping("/apply/status")
    public Result<CompanionProfile> getApplyStatus(@AuthenticationPrincipal Long userId) {
        CompanionProfile profile = companionProfileService.getById(userId);
        return Result.success(profile);
    }
}
