package com.playapp.controller;

import com.playapp.common.BusinessException;
import com.playapp.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/file")
public class FileController {

    @Value("${file.upload.path:D:/code/project/play_app/uploads/}")
    private String uploadPath;

    @Value("${app.base-url:http://127.0.0.1:8080}")
    private String baseUrl;

    /**
     * MVP阶段：简单的本地文件上传，返回可访问的相对URL
     */
    @PostMapping("/upload")
    public Result<String> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }

        try {
            // 创建上传目录
            File dir = new File(uploadPath);
            if (!dir.exists() && !dir.mkdirs()) {
                throw new BusinessException("上传目录创建失败");
            }

            // 获取原文件名
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null) {
                originalFilename = "unknown";
            }
            String extension = StringUtils.getFilenameExtension(originalFilename);
            if (!StringUtils.hasText(extension)) {
                extension = "tmp";
            }

            // 生成唯一文件名
            String newFilename = UUID.randomUUID().toString().replace("-", "") + "." + extension;
            Path targetPath = Paths.get(uploadPath, newFilename);

            // 保存文件
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            String accessUrl = baseUrl + "/uploads/" + newFilename;

            return Result.success("上传成功", accessUrl);

        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new BusinessException("文件上传失败");
        }
    }
}
