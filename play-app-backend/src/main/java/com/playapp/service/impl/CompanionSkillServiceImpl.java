package com.playapp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.playapp.common.BusinessException;
import com.playapp.common.ErrorCode;
import com.playapp.entity.CompanionSkill;
import com.playapp.mapper.CompanionSkillMapper;
import com.playapp.service.CompanionSkillService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompanionSkillServiceImpl extends ServiceImpl<CompanionSkillMapper, CompanionSkill> implements CompanionSkillService {

    @Override
    public List<CompanionSkill> getSkills(Long companionId) {
        return this.list(new LambdaQueryWrapper<CompanionSkill>()
                .eq(CompanionSkill::getCompanionId, companionId)
                .eq(CompanionSkill::getStatus, 1));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CompanionSkill addSkill(Long companionId, Integer categoryId, BigDecimal pricePerHour, String experienceDesc) {
        // 检查是否已有同品类技能
        long count = this.count(new LambdaQueryWrapper<CompanionSkill>()
                .eq(CompanionSkill::getCompanionId, companionId)
                .eq(CompanionSkill::getCategoryId, categoryId)
                .eq(CompanionSkill::getStatus, 1));
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "该品类已添加，请直接修改价格");
        }

        CompanionSkill skill = new CompanionSkill();
        skill.setCompanionId(companionId);
        skill.setCategoryId(categoryId);
        skill.setPricePerHour(pricePerHour);
        skill.setExperienceDesc(experienceDesc);
        skill.setStatus(1);
        this.save(skill);
        log.info("陪玩 {} 添加技能: categoryId={}, price={}", companionId, categoryId, pricePerHour);
        return skill;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSkill(Long companionId, Long skillId, BigDecimal pricePerHour, String experienceDesc) {
        CompanionSkill skill = this.getById(skillId);
        if (skill == null || !skill.getCompanionId().equals(companionId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "技能不存在");
        }
        if (pricePerHour != null) skill.setPricePerHour(pricePerHour);
        if (experienceDesc != null) skill.setExperienceDesc(experienceDesc);
        this.updateById(skill);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSkill(Long companionId, Long skillId) {
        CompanionSkill skill = this.getById(skillId);
        if (skill == null || !skill.getCompanionId().equals(companionId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "技能不存在");
        }
        long count = this.count(new LambdaQueryWrapper<CompanionSkill>()
                .eq(CompanionSkill::getCompanionId, companionId)
                .eq(CompanionSkill::getStatus, 1));
        if (count <= 1) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "至少保留一个技能");
        }
        skill.setStatus(2); // 软删除
        this.updateById(skill);
        log.info("陪玩 {} 删除技能: skillId={}", companionId, skillId);
    }
}
