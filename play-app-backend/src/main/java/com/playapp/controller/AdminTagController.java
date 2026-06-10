package com.playapp.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.playapp.common.Result;
import com.playapp.entity.Tag;
import com.playapp.mapper.TagMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/tags")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminTagController {

    private final TagMapper tagMapper;

    /** 标签列表 */
    @GetMapping
    public Result<List<Tag>> getTags() {
        return Result.success(tagMapper.selectList(
                new LambdaQueryWrapper<Tag>().orderByAsc(Tag::getSortOrder)));
    }

    /** 创建标签 */
    @PostMapping
    public Result<Tag> createTag(@RequestBody Tag tag) {
        tagMapper.insert(tag);
        return Result.success(tag);
    }

    /** 修改标签 */
    @PutMapping("/{id}")
    public Result<?> updateTag(@PathVariable Integer id, @RequestBody Tag tag) {
        tag.setId(id);
        tagMapper.updateById(tag);
        return Result.success("修改成功");
    }

    /** 删除标签（软删除） */
    @DeleteMapping("/{id}")
    public Result<?> deleteTag(@PathVariable Integer id) {
        Tag tag = tagMapper.selectById(id);
        if (tag != null) {
            tag.setStatus(0);
            tagMapper.updateById(tag);
        }
        return Result.success("已删除");
    }
}
