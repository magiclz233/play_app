package com.playapp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.playapp.entity.CompanionSkill;

import java.util.List;

public interface CompanionSkillService extends IService<CompanionSkill> {

    List<CompanionSkill> getSkills(Long companionId);

    CompanionSkill addSkill(Long companionId, Integer categoryId, java.math.BigDecimal pricePerHour, String experienceDesc);

    void updateSkill(Long companionId, Long skillId, java.math.BigDecimal pricePerHour, String experienceDesc);

    void deleteSkill(Long companionId, Long skillId);
}
