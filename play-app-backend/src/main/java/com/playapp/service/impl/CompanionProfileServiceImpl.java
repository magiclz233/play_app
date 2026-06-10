package com.playapp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.playapp.common.BusinessException;
import com.playapp.common.ErrorCode;
import com.playapp.dto.CompanionApplyDTO;
import com.playapp.entity.CompanionAlbum;
import com.playapp.entity.CompanionProfile;
import com.playapp.entity.CompanionSkill;
import com.playapp.entity.CompanionTagRelation;
import com.playapp.entity.Tag;
import com.playapp.entity.User;
import com.playapp.mapper.CompanionAlbumMapper;
import com.playapp.mapper.CompanionProfileMapper;
import com.playapp.mapper.CompanionSkillMapper;
import com.playapp.mapper.CompanionTagRelationMapper;
import com.playapp.mapper.TagMapper;
import com.playapp.mapper.UserMapper;
import com.playapp.service.CompanionProfileService;
import com.playapp.vo.CompanionDetailVO;
import com.playapp.vo.CompanionVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompanionProfileServiceImpl extends ServiceImpl<CompanionProfileMapper, CompanionProfile> implements CompanionProfileService {

    private final CompanionAlbumMapper companionAlbumMapper;
    private final CompanionTagRelationMapper companionTagRelationMapper;
    private final CompanionSkillMapper companionSkillMapper;
    private final TagMapper tagMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitApplication(Long userId, CompanionApplyDTO dto) {
        CompanionProfile existingProfile = this.getById(userId);

        if (existingProfile != null) {
            if (existingProfile.getAuditStatus() == 0) {
                throw new BusinessException(ErrorCode.COMPANION_ALREADY_APPLIED, "入驻申请正在审核中，请勿重复提交");
            }
            if (existingProfile.getAuditStatus() == 1) {
                throw new BusinessException(ErrorCode.COMPANION_ALREADY_APPLIED, "您已经是认证助教了");
            }
            // 驳回后重新提交：清除旧相册、旧标签、旧技能
            companionAlbumMapper.delete(new LambdaQueryWrapper<CompanionAlbum>().eq(CompanionAlbum::getCompanionId, userId));
            companionTagRelationMapper.delete(new LambdaQueryWrapper<CompanionTagRelation>().eq(CompanionTagRelation::getCompanionId, userId));
            companionSkillMapper.delete(new LambdaQueryWrapper<CompanionSkill>().eq(CompanionSkill::getCompanionId, userId));
        }

        CompanionProfile profile = new CompanionProfile();
        profile.setUserId(userId);
        profile.setRealName(dto.getRealName());
        profile.setNickname(dto.getNickname());
        // 首次申请自动生成助教号码（驳回重申请保留原号）
        if (existingProfile == null || existingProfile.getCompanionCode() == null) {
            profile.setCompanionCode(generateCompanionCode());
        } else {
            profile.setCompanionCode(existingProfile.getCompanionCode());
        }
        profile.setWechatCode(dto.getWechatCode());
        profile.setGender(dto.getGender());
        profile.setAge(dto.getAge());
        profile.setHeight(dto.getHeight());
        profile.setSummary(dto.getSummary());
        profile.setVoiceUrl(dto.getVoiceUrl());
        profile.setVoiceDuration(dto.getVoiceDuration());

        profile.setAuditStatus(0);
        profile.setWorkStatus(3);
        profile.setRating(new BigDecimal("5.00"));

        this.saveOrUpdate(profile);

        // 保存技能价格（默认categoryId=1，后续可扩展多技能）
        CompanionSkill skill = new CompanionSkill();
        skill.setCompanionId(userId);
        skill.setCategoryId(1);
        skill.setPricePerHour(dto.getPricePerHour());
        skill.setStatus(1);
        companionSkillMapper.insert(skill);

        // 保存相册
        List<String> photos = dto.getPhotoUrls();
        if (photos != null && !photos.isEmpty()) {
            for (int i = 0; i < photos.size(); i++) {
                CompanionAlbum album = new CompanionAlbum();
                album.setCompanionId(userId);
                album.setImageUrl(photos.get(i));
                album.setSortOrder(i);
                album.setIsCover(i == 0);
                album.setAuditStatus(1);
                companionAlbumMapper.insert(album);
            }
        }

        // 保存标签
        List<Integer> tagIds = dto.getTagIds();
        if (tagIds != null && !tagIds.isEmpty()) {
            for (Integer tagId : tagIds) {
                CompanionTagRelation relation = new CompanionTagRelation();
                relation.setCompanionId(userId);
                relation.setTagId(tagId);
                companionTagRelationMapper.insert(relation);
            }
        }

        log.info("用户 {} 提交了入驻申请", userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditApplication(Long companionId, boolean isPass, String rejectReason) {
        CompanionProfile profile = this.getById(companionId);
        if (profile == null) {
            throw new BusinessException(ErrorCode.COMPANION_NOT_FOUND, "申请记录不存在");
        }

        if (profile.getAuditStatus() != 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "该申请已处理");
        }

        if (isPass) {
            profile.setAuditStatus(1);
            profile.setWorkStatus(1);
        } else {
            profile.setAuditStatus(2);
            profile.setRejectReason(rejectReason);
        }

        this.updateById(profile);
        log.info("助教申请审核结果: companionId={}, isPass={}", companionId, isPass);
    }

    @Override
    public Page<CompanionVO> getCompanionPage(Integer current, Integer size, Integer categoryId,
                                                String keyword, Integer gender, String sortBy) {
        Page<CompanionProfile> page = new Page<>(current, size);
        LambdaQueryWrapper<CompanionProfile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CompanionProfile::getAuditStatus, 1);

        // 关键词搜索（昵称或简介）
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(CompanionProfile::getNickname, keyword)
                    .or().like(CompanionProfile::getSummary, keyword));
        }

        // 性别筛选
        if (gender != null && gender > 0) {
            wrapper.eq(CompanionProfile::getGender, gender);
        }

        // 按分类筛选
        if (categoryId != null) {
            List<Long> companionIds = companionSkillMapper.selectList(
                    new LambdaQueryWrapper<CompanionSkill>()
                            .eq(CompanionSkill::getCategoryId, categoryId)
                            .eq(CompanionSkill::getStatus, 1)
            ).stream().map(CompanionSkill::getCompanionId).collect(Collectors.toList());
            if (companionIds.isEmpty()) {
                Page<CompanionVO> empty = new Page<>(current, size, 0);
                empty.setRecords(List.of());
                return empty;
            }
            wrapper.in(CompanionProfile::getUserId, companionIds);
        }

        // 排序
        if ("rating".equals(sortBy)) {
            wrapper.orderByDesc(CompanionProfile::getRating);
        } else if ("newest".equals(sortBy)) {
            wrapper.orderByDesc(CompanionProfile::getCreateTime);
        } else {
            wrapper.orderByDesc(CompanionProfile::getIsRecommend, CompanionProfile::getSortWeight, CompanionProfile::getRating);
        }

        this.page(page, wrapper);
        Page<CompanionVO> voPage = new Page<>(current, size, page.getTotal());
        voPage.setRecords(page.getRecords().stream().map(this::toCompanionVO).collect(Collectors.toList()));
        return voPage;
    }

    @Override
    public CompanionDetailVO getCompanionDetail(Long id) {
        CompanionProfile profile = this.getById(id);
        if (profile == null || profile.getAuditStatus() != 1) {
            throw new BusinessException(ErrorCode.COMPANION_NOT_FOUND, "该助教不存在或已下线");
        }

        CompanionDetailVO vo = new CompanionDetailVO();
        vo.setUserId(profile.getUserId());
        vo.setNickname(getNickname(profile));
        vo.setGender(profile.getGender());
        vo.setAge(profile.getAge());
        vo.setHeight(profile.getHeight());
        vo.setSummary(profile.getSummary());
        vo.setVoiceUrl(profile.getVoiceUrl());
        vo.setVoiceDuration(profile.getVoiceDuration());
        vo.setRating(profile.getRating());
        vo.setTotalRatingCount(profile.getTotalRatingCount());
        vo.setOrderCount(profile.getOrderCount());
        vo.setWorkStatus(profile.getWorkStatus());
        vo.setPricePerHour(getPricePerHour(id));

        // 相册
        List<CompanionAlbum> albums = companionAlbumMapper.selectList(
                new LambdaQueryWrapper<CompanionAlbum>()
                        .eq(CompanionAlbum::getCompanionId, id)
                        .orderByAsc(CompanionAlbum::getSortOrder));
        vo.setPhotoUrls(albums.stream().map(CompanionAlbum::getImageUrl).collect(Collectors.toList()));

        // 真实标签
        vo.setTags(getTagNames(id));

        return vo;
    }

    // ===== 私有辅助方法 =====

    private CompanionVO toCompanionVO(CompanionProfile profile) {
        CompanionVO vo = new CompanionVO();
        vo.setUserId(profile.getUserId());
        vo.setNickname(getNickname(profile));
        vo.setRating(profile.getRating());
        vo.setTotalRatingCount(profile.getTotalRatingCount());
        vo.setPricePerHour(getPricePerHour(profile.getUserId()));
        vo.setWorkStatus(profile.getWorkStatus());
        vo.setVoiceUrl(profile.getVoiceUrl());
        vo.setVoiceDuration(profile.getVoiceDuration());

        // 封面图
        CompanionAlbum cover = companionAlbumMapper.selectOne(
                new LambdaQueryWrapper<CompanionAlbum>()
                        .eq(CompanionAlbum::getCompanionId, profile.getUserId())
                        .eq(CompanionAlbum::getIsCover, true)
                        .last("limit 1"));
        if (cover != null) {
            vo.setCoverUrl(cover.getImageUrl());
        }

        // 真实标签
        vo.setTags(getTagNames(profile.getUserId()));

        // 距离（暂无LBS，留空）
        vo.setDistance("");

        return vo;
    }

    private String getNickname(CompanionProfile profile) {
        if (profile.getNickname() != null && !profile.getNickname().isEmpty()) {
            return profile.getNickname();
        }
        User user = userMapper.selectById(profile.getUserId());
        if (user != null && user.getNickname() != null) {
            return user.getNickname();
        }
        return "用户" + profile.getUserId();
    }

    private BigDecimal getPricePerHour(Long companionId) {
        CompanionSkill skill = companionSkillMapper.selectOne(
                new LambdaQueryWrapper<CompanionSkill>()
                        .eq(CompanionSkill::getCompanionId, companionId)
                        .eq(CompanionSkill::getStatus, 1)
                        .last("limit 1"));
        return skill != null ? skill.getPricePerHour() : BigDecimal.ZERO;
    }

    private List<String> getTagNames(Long companionId) {
        List<CompanionTagRelation> relations = companionTagRelationMapper.selectList(
                new LambdaQueryWrapper<CompanionTagRelation>()
                        .eq(CompanionTagRelation::getCompanionId, companionId));
        if (relations.isEmpty()) return List.of();

        List<Integer> tagIds = relations.stream().map(CompanionTagRelation::getTagId).collect(Collectors.toList());
        List<Tag> tags = tagMapper.selectBatchIds(tagIds);
        return tags.stream().map(Tag::getName).collect(Collectors.toList());
    }

    // ===== 助教号码自动生成 =====

    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * 生成 7 位助教号码，自动过滤靓号，检查唯一性
     */
    private String generateCompanionCode() {
        int maxRetries = 50;
        for (int i = 0; i < maxRetries; i++) {
            // 1000000 ~ 9999999 (7位)
            String code = String.valueOf(1_000_000 + RANDOM.nextInt(9_000_000));
            if (isPremiumNumber(code)) continue;
            if (existsCompanionCode(code)) continue;
            return code;
        }
        // 兜底：8位随机
        for (int i = 0; i < maxRetries; i++) {
            String code = String.valueOf(10_000_000 + RANDOM.nextInt(90_000_000));
            if (isPremiumNumber(code)) continue;
            if (existsCompanionCode(code)) continue;
            return code;
        }
        // 极端情况：时间戳兜底
        return String.valueOf(System.currentTimeMillis()).substring(5);
    }

    /**
     * 判断是否为靓号（不自动分配，留给后期售卖）
     */
    private boolean isPremiumNumber(String code) {
        char[] c = code.toCharArray();
        int len = c.length;

        // 全相同: 1111111, 8888888
        if (allSame(c)) return true;

        // 顺子（正序/倒序）: 1234567, 7654321
        if (isSequential(c)) return true;

        // 回文: 1234321
        if (isPalindrome(c)) return true;

        // AABB 模式: 1122334
        if (hasAabbPattern(c)) return true;

        // 含 3 个以上相同数字: 8881234, 1238888
        int maxConsecutive = 1, current = 1;
        for (int i = 1; i < len; i++) {
            if (c[i] == c[i - 1]) {
                current++;
                maxConsecutive = Math.max(maxConsecutive, current);
            } else {
                current = 1;
            }
        }
        if (maxConsecutive >= 3) return true;

        // 豹子号（含 4 个以上相同数字，不论位置）: 1818181
        int[] count = new int[10];
        for (char ch : c) count[ch - '0']++;
        for (int cnt : count) {
            if (cnt >= 4) return true;
        }

        // ABAB 模式: 1212121
        if (isAbabPattern(c)) return true;

        return false;
    }

    private boolean allSame(char[] c) {
        for (int i = 1; i < c.length; i++)
            if (c[i] != c[0]) return false;
        return true;
    }

    private boolean isSequential(char[] c) {
        int asc = 1, desc = 1;
        for (int i = 1; i < c.length; i++) {
            if (c[i] == c[i - 1] + 1) asc++;
            if (c[i] == c[i - 1] - 1) desc++;
        }
        return asc == c.length || desc == c.length;
    }

    private boolean isPalindrome(char[] c) {
        for (int i = 0; i < c.length / 2; i++)
            if (c[i] != c[c.length - 1 - i]) return false;
        return true;
    }

    private boolean hasAabbPattern(char[] c) {
        for (int i = 0; i < c.length - 3; i++) {
            if (c[i] == c[i + 1] && c[i + 2] == c[i + 3] && c[i] != c[i + 2]) return true;
        }
        return false;
    }

    private boolean isAbabPattern(char[] c) {
        for (int i = 0; i < c.length - 3; i++) {
            if (c[i] == c[i + 2] && c[i + 1] == c[i + 3] && c[i] != c[i + 1]) return true;
        }
        return false;
    }

    private boolean existsCompanionCode(String code) {
        return this.count(new LambdaQueryWrapper<CompanionProfile>()
                .eq(CompanionProfile::getCompanionCode, code)) > 0;
    }
}
