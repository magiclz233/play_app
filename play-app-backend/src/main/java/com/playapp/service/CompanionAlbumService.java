package com.playapp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.playapp.entity.CompanionAlbum;

import java.util.List;

public interface CompanionAlbumService extends IService<CompanionAlbum> {

    /** 获取陪玩相册 */
    List<CompanionAlbum> getAlbums(Long companionId);

    /** 添加照片 */
    CompanionAlbum addPhoto(Long companionId, String imageUrl, String thumbnailUrl);

    /** 设为封面 */
    void setCover(Long companionId, Long albumId);

    /** 删除照片 */
    void deletePhoto(Long companionId, Long albumId);
}
