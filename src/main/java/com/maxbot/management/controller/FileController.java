package com.maxbot.management.controller;

import com.maxbot.management.service.OssService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 文件上传控制器
 */
@Slf4j
@RestController
@RequestMapping("/file")
@CrossOrigin(origins = "*")
public class FileController {

    @Autowired
    private OssService ossService;

    private static final String TEMP_DIR = "/tmp/management/upload/";

    /**
     * 上传文件到OSS
     */
    @PostMapping("/upload")
    public Map<String, Object> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "common") String folder) {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 创建临时目录
            Path tempPath = Paths.get(TEMP_DIR);
            if (!Files.exists(tempPath)) {
                Files.createDirectories(tempPath);
            }

            // 保存临时文件
            String originalFilename = file.getOriginalFilename();
            String tempFileName = UUID.randomUUID().toString() + "_" + originalFilename;
            File tempFile = new File(TEMP_DIR + tempFileName);
            file.transferTo(tempFile);

            // 上传到OSS
            String objectName = ossService.generateObjectName(folder, originalFilename);
            String url = ossService.uploadFile(objectName, tempFile);

            // 删除临时文件
            tempFile.delete();

            result.put("code", 200);
            Map<String, Object> data = new HashMap<>();
            data.put("url", url);
            data.put("objectName", objectName);
            data.put("originalName", originalFilename);
            result.put("data", data);
            result.put("msg", "上传成功");
            
            log.info("文件上传成功: {}", originalFilename);
        } catch (Exception e) {
            log.error("文件上传失败", e);
            result.put("code", 500);
            result.put("msg", "上传失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 上传图片（专门用于图片上传）
     */
    @PostMapping("/upload/image")
    public Map<String, Object> uploadImage(@RequestParam("file") MultipartFile file) {
        return upload(file, "images");
    }

    /**
     * 上传视频（专门用于视频上传）
     */
    @PostMapping("/upload/video")
    public Map<String, Object> uploadVideo(@RequestParam("file") MultipartFile file) {
        return upload(file, "videos");
    }

    /**
     * 上传文本文件
     */
    @PostMapping("/upload/text")
    public Map<String, Object> uploadText(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = upload(file, "texts");
        
        // 读取文本内容
        if (result.get("code").equals(200)) {
            try {
                String content = new String(file.getBytes());
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) result.get("data");
                data.put("content", content);
            } catch (Exception e) {
                log.error("读取文本内容失败", e);
            }
        }
        
        return result;
    }
}
