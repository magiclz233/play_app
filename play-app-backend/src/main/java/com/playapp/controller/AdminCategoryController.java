package com.playapp.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.playapp.common.OpLog;
import com.playapp.common.Result;
import com.playapp.entity.Category;
import com.playapp.mapper.CategoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理员分类管理控制器
 */
@RestController
@RequestMapping("/api/admin/categories")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminCategoryController {

    private final CategoryMapper categoryMapper;

    /** 全量分类列表 */
    @GetMapping
    public Result<List<Category>> list() {
        return Result.success(categoryMapper.selectList(
                new LambdaQueryWrapper<Category>().orderByAsc(Category::getSortOrder)));
    }

    /** 创建分类 */
    @PostMapping
    @OpLog(module = "CONTENT", action = "CREATE", detail = "创建分类")
    public Result<Category> create(@RequestBody Category category) {
        categoryMapper.insert(category);
        return Result.success(category);
    }

    /** 更新分类 */
    @PutMapping("/{id}")
    @OpLog(module = "CONTENT", action = "UPDATE", detail = "更新分类: #id")
    public Result<?> update(@PathVariable Integer id, @RequestBody Category category) {
        category.setId(id);
        categoryMapper.updateById(category);
        return Result.success("修改成功");
    }

    /** 删除分类（软删除） */
    @DeleteMapping("/{id}")
    @OpLog(module = "CONTENT", action = "DELETE", detail = "删除分类: #id")
    public Result<?> delete(@PathVariable Integer id) {
        Category category = categoryMapper.selectById(id);
        if (category != null) {
            category.setStatus(0);
            categoryMapper.updateById(category);
        }
        return Result.success("已删除");
    }
}
