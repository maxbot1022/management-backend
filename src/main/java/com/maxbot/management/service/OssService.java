package com.maxbot.management.service;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.PutObjectRequest;
import com.maxbot.management.config.OssProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;

/**
 * 阿里云OSS服务
 */
@Slf4j
@Service
public class OssService {

    @Autowired
    private OssProperties ossProperties;

    private OSS ossClient;

    @PostConstruct
    public void init() {
        ossClient = new OSSClientBuilder().build(
                ossProperties.getEndpoint(),
                ossProperties.getAccessKeyId(),
                ossProperties.getAccessKeySecret()
        );
        log.info("OSS客户端初始化成功");
    }

    @PreDestroy
    public void destroy() {
        if (ossClient != null) {
            ossClient.shutdown();
            log.info("OSS客户端已关闭");
        }
    }

    /**
     * 上传文件
     */
    public String uploadFile(String objectName, File file) {
        try {
            PutObjectRequest putObjectRequest = new PutObjectRequest(
                    ossProperties.getBucketName(), objectName, file
            );
            ossClient.putObject(putObjectRequest);
            return getFileUrl(objectName);
        } catch (Exception e) {
            log.error("上传文件失败: {}", objectName, e);
            throw new RuntimeException("上传文件失败", e);
        }
    }

    /**
     * 上传输入流
     */
    public String uploadInputStream(String objectName, InputStream inputStream) {
        try {
            ossClient.putObject(ossProperties.getBucketName(), objectName, inputStream);
            return getFileUrl(objectName);
        } catch (Exception e) {
            log.error("上传输入流失败: {}", objectName, e);
            throw new RuntimeException("上传输入流失败", e);
        }
    }

    /**
     * 获取文件访问URL
     */
    public String getFileUrl(String objectName) {
        // 生成临时URL，有效期1小时
        Date expiration = new Date(System.currentTimeMillis() + 3600 * 1000);
        URL url = ossClient.generatePresignedUrl(ossProperties.getBucketName(), objectName, expiration);
        return url.toString();
    }

    /**
     * 生成唯一的对象名称
     */
    public String generateObjectName(String prefix, String originalFilename) {
        String extension = "";
        int dotIndex = originalFilename.lastIndexOf(".");
        if (dotIndex > 0) {
            extension = originalFilename.substring(dotIndex);
        }
        return prefix + "/" + System.currentTimeMillis() + "_" + (int)(Math.random() * 10000) + extension;
    }

    /**
     * 删除文件
     */
    public void deleteFile(String objectName) {
        try {
            ossClient.deleteObject(ossProperties.getBucketName(), objectName);
            log.info("删除文件成功: {}", objectName);
        } catch (Exception e) {
            log.error("删除文件失败: {}", objectName, e);
        }
    }
}
