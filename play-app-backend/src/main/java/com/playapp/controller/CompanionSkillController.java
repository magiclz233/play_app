package com.playapp.controller;

import com.playapp.common.Result;
import com.playapp.entity.CompanionSkill;
import com.playapp.service.CompanionSkillService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/companion/skills")
@RequiredArgsConstructor
public class CompanionSkillController {

    private final CompanionSkillService skillService;

    /** 获取我的技能列表 */
    @GetMapping
    public Result<List<CompanionSkill>> getSkills(@AuthenticationPrincipal Long userId) {
        return Result.success(skillService.getSkills(userId));
    }

    /** 添加技能 */
    @PostMapping
    public Result<CompanionSkill> addSkill(@AuthenticationPrincipal Long userId, @RequestBody Map<String, Object> body) {
        Integer categoryId = (Integer) body.get("categoryId");
        BigDecimal pricePerHour = new BigDecimal(body.get("pricePerHour").toString());
        String experienceDesc = (String) body.get("experienceDesc");
        return Result.success(skillService.addSkill(userId, categoryId, pricePerHour, experienceDesc));
    }

    /** 修改技能 */
    @PutMapping("/{id}")
    public Result<?> updateSkill(@AuthenticationPrincipal Long userId, @PathVariable Long id, @RequestBody Map<String, Object> body) {
        BigDecimal pricePerHour = body.containsKey("pricePerHour") ? new BigDecimal(body.get("pricePerHour").toString()) : null;
        String experienceDesc = (String) body.get("experienceDesc");
        skillService.updateSkill(userId, id, pricePerHour, experienceDesc);
        return Result.success("修改成功");
    }

    /** 删除技能 */
    @DeleteMapping("/{id}")
    public Result<?> deleteSkill(@AuthenticationPrincipal Long userId, @PathVariable Long id) {
        skillService.deleteSkill(userId, id);
        return Result.success("已删除");
    }
}
