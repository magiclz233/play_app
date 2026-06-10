package com.playapp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.playapp.common.BusinessException;
import com.playapp.common.ErrorCode;
import com.playapp.entity.CompanionAlbum;
import com.playapp.mapper.CompanionAlbumMapper;
import com.playapp.service.CompanionAlbumService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompanionAlbumServiceImpl extends ServiceImpl<CompanionAlbumMapper, CompanionAlbum> implements CompanionAlbumService {

    private static final int MAX_PHOTOS = 9;

    @Override
    public List<CompanionAlbum> getAlbums(Long companionId) {
        return this.list(new LambdaQueryWrapper<CompanionAlbum>()
                .eq(CompanionAlbum::getCompanionId, companionId)
                .eq(CompanionAlbum::getAuditStatus, 1)
                .orderByDesc(CompanionAlbum::getSortOrder));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CompanionAlbum addPhoto(Long companionId, String imageUrl, String thumbnailUrl) {
        long count = this.count(new LambdaQueryWrapper<CompanionAlbum>()
                .eq(CompanionAlbum::getCompanionId, companionId)
                .eq(CompanionAlbum::getAuditStatus, 1));
        if (count >= MAX_PHOTOS) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "相册最多" + MAX_PHOTOS + "张照片");
        }

        CompanionAlbum album = new CompanionAlbum();
        album.setCompanionId(companionId);
        album.setImageUrl(imageUrl);
        album.setThumbnailUrl(thumbnailUrl);
        album.setSortOrder((int) count);
        album.setIsCover(count == 0); // 第一张默认封面
        album.setAuditStatus(1);
        this.save(album);
        log.info("陪玩 {} 添加相册照片: {}", companionId, imageUrl);
        return album;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setCover(Long companionId, Long albumId) {
        CompanionAlbum album = this.getById(albumId);
        if (album == null || !album.getCompanionId().equals(companionId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "照片不存在");
        }
        // 取消其他封面
        this.update(new LambdaUpdateWrapper<CompanionAlbum>()
                .eq(CompanionAlbum::getCompanionId, companionId)
                .set(CompanionAlbum::getIsCover, false));
        // 设置新封面
        album.setIsCover(true);
        this.updateById(album);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePhoto(Long companionId, Long albumId) {
        CompanionAlbum album = this.getById(albumId);
        if (album == null || !album.getCompanionId().equals(companionId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "照片不存在");
        }
        long count = this.count(new LambdaQueryWrapper<CompanionAlbum>()
                .eq(CompanionAlbum::getCompanionId, companionId)
                .eq(CompanionAlbum::getAuditStatus, 1));
        if (count <= 1) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "至少保留一张照片");
        }
        this.removeById(albumId);
        // 如果删除的是封面，将第一张设为封面
        if (album.getIsCover()) {
            CompanionAlbum first = this.getOne(new LambdaQueryWrapper<CompanionAlbum>()
                    .eq(CompanionAlbum::getCompanionId, companionId)
                    .eq(CompanionAlbum::getAuditStatus, 1)
                    .orderByDesc(CompanionAlbum::getSortOrder)
                    .last("limit 1"));
            if (first != null) {
                first.setIsCover(true);
                this.updateById(first);
            }
        }
        log.info("陪玩 {} 删除相册照片: {}", companionId, albumId);
    }
}
