package com.playapp.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.playapp.common.Result;
import com.playapp.entity.Category;
import com.playapp.mapper.CategoryMapper;
import com.playapp.service.CompanionProfileService;
import com.playapp.vo.CompanionDetailVO;
import com.playapp.vo.CompanionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/companions")
@RequiredArgsConstructor
public class PublicCompanionController {

    private final CompanionProfileService companionProfileService;
    private final CategoryMapper categoryMapper;

    /**
     * 首页推荐助教
     */
    @GetMapping("/recommended")
    public Result<List<CompanionVO>> getRecommended() {
        Page<CompanionVO> page = companionProfileService.getCompanionPage(1, 10, null, null, null, null);
        return Result.success(page.getRecords());
    }

    /**
     * 分页查询助教大厅列表（支持搜索/筛选/排序）
     */
    @GetMapping
    public Result<Page<CompanionVO>> getList(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer gender,
            @RequestParam(required = false) String sortBy) {

        return Result.success(companionProfileService.getCompanionPage(current, size, categoryId, keyword, gender, sortBy));
    }

    /**
     * 查询助教公开详情
     */
    @GetMapping("/{id}")
    public Result<CompanionDetailVO> getDetail(@PathVariable Long id) {
        return Result.success(companionProfileService.getCompanionDetail(id));
    }
}
