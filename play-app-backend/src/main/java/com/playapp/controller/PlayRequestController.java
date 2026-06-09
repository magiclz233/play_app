package com.playapp.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.playapp.common.Result;
import com.playapp.dto.PlayRequestCreateDTO;
import com.playapp.entity.PlayRequest;
import com.playapp.service.PlayRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/requests")
@RequiredArgsConstructor
public class PlayRequestController {

    private final PlayRequestService playRequestService;

    @GetMapping
    public Result<Page<PlayRequest>> list(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "20") Integer size) {
        return Result.success(playRequestService.listPublic(current, size));
    }

    @GetMapping("/mine")
    public Result<Page<PlayRequest>> mine(
            @AuthenticationPrincipal Long userId,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "20") Integer size) {
        return Result.success(playRequestService.listMine(userId, current, size));
    }

    @PostMapping
    public Result<PlayRequest> publish(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody PlayRequestCreateDTO dto) {
        return Result.success("需求已发布", playRequestService.publish(userId, dto));
    }
}
