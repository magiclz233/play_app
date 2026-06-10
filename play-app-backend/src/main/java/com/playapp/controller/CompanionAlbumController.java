package com.playapp.controller;

import com.playapp.common.Result;
import com.playapp.entity.CompanionAlbum;
import com.playapp.service.CompanionAlbumService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/companion/albums")
@RequiredArgsConstructor
public class CompanionAlbumController {

    private final CompanionAlbumService albumService;

    /** 获取我的相册 */
    @GetMapping
    public Result<List<CompanionAlbum>> getAlbums(@AuthenticationPrincipal Long userId) {
        return Result.success(albumService.getAlbums(userId));
    }

    /** 添加照片 */
    @PostMapping
    public Result<CompanionAlbum> addPhoto(@AuthenticationPrincipal Long userId, @RequestBody Map<String, String> body) {
        String imageUrl = body.get("imageUrl");
        String thumbnailUrl = body.get("thumbnailUrl");
        return Result.success(albumService.addPhoto(userId, imageUrl, thumbnailUrl));
    }

    /** 设为封面 */
    @PutMapping("/{id}/cover")
    public Result<?> setCover(@AuthenticationPrincipal Long userId, @PathVariable Long id) {
        albumService.setCover(userId, id);
        return Result.success("已设为封面");
    }

    /** 删除照片 */
    @DeleteMapping("/{id}")
    public Result<?> deletePhoto(@AuthenticationPrincipal Long userId, @PathVariable Long id) {
        albumService.deletePhoto(userId, id);
        return Result.success("已删除");
    }
}
