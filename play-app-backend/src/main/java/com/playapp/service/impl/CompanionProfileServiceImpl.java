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
import java.util.ArrayList;
import java.util.List;
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
    public Page<CompanionVO> getCompanionPage(Integer current, Integer size, Integer categoryId) {
        Page<CompanionProfile> page = new Page<>(current, size);
        LambdaQueryWrapper<CompanionProfile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CompanionProfile::getAuditStatus, 1);

        // 按分类筛选：通过关联技能表
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

        wrapper.orderByDesc(CompanionProfile::getIsRecommend, CompanionProfile::getSortWeight, CompanionProfile::getRating);
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
        vo.setPricePerHour(getPricePerHour(profile.getUserId()));
        vo.setWorkStatus(profile.getWorkStatus());

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
}
