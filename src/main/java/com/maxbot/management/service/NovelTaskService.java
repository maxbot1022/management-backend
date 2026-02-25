package com.maxbot.management.service;

import com.maxbot.management.entity.NovelTask;
import com.maxbot.management.mapper.NovelTaskMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 小说整理任务服务
 */
@Slf4j
@Service
public class NovelTaskService {

    @Autowired
    private NovelTaskMapper novelTaskMapper;

    @Autowired
    private VolcengineService volcengineService;

    @Autowired
    private OssService ossService;

    /**
     * 创建小说整理任务
     */
    public NovelTask createTask(Long projectId, String taskName, String originalTextUrl, String prompt) {
        NovelTask task = new NovelTask();
        task.setProjectId(projectId);
        task.setTaskName(taskName);
        task.setOriginalTextUrl(originalTextUrl);
        task.setPrompt(prompt);
        task.setStatus(0); // 待处理
        task.setRetryCount(0);
        novelTaskMapper.insert(task);
        log.info("创建小说整理任务成功: {}", task.getId());
        return task;
    }

    /**
     * 提交任务到火山引擎处理
     */
    public void submitToVolcengine(Long taskId) {
        NovelTask task = novelTaskMapper.selectById(taskId);
        if (task == null || task.getStatus() != 0) {
            throw new RuntimeException("任务不存在或状态不正确");
        }

        try {
            // 从OSS下载原始文本内容
            String textContent = downloadTextFromOss(task.getOriginalTextUrl());
            
            // 调用火山引擎处理
            String result = volcengineService.processNovelText(textContent, task.getPrompt());
            
            // 更新任务状态
            task.setResultJson(result);
            task.setStatus(2); // 已完成
            novelTaskMapper.updateById(task);
            
            log.info("小说整理任务处理完成: {}", taskId);
        } catch (Exception e) {
            log.error("小说整理任务处理失败: {}", taskId, e);
            task.setStatus(3); // 失败
            task.setErrorMsg(e.getMessage());
            task.setRetryCount(task.getRetryCount() + 1);
            novelTaskMapper.updateById(task);
        }
    }

    /**
     * 从OSS下载文本内容
     */
    private String downloadTextFromOss(String fileUrl) {
        try {
            // 从URL中提取objectName
            // URL格式: https://bucket.oss-region.aliyuncs.com/objectName?Expires=xxx&OSSAccessKeyId=xxx&Signature=xxx
            String objectName = extractObjectNameFromUrl(fileUrl);
            
            // 使用OSS客户端下载文件
            InputStream inputStream = ossService.downloadFile(objectName);
            
            // 读取文本内容
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                return reader.lines().collect(Collectors.joining("\n"));
            }
        } catch (Exception e) {
            log.error("从OSS下载文本失败: {}", fileUrl, e);
            throw new RuntimeException("下载文本失败", e);
        }
    }

    /**
     * 从URL中提取objectName
     */
    private String extractObjectNameFromUrl(String fileUrl) {
        try {
            // 去掉查询参数
            String urlWithoutParams = fileUrl.split("\\?")[0];
            // 提取path部分
            java.net.URL url = new java.net.URL(urlWithoutParams);
            String path = url.getPath();
            // 去掉开头的 /
            if (path.startsWith("/")) {
                path = path.substring(1);
            }
            return path;
        } catch (Exception e) {
            log.error("解析URL失败: {}", fileUrl, e);
            throw new RuntimeException("解析URL失败", e);
        }
    }

    /**
     * 获取任务详情
     */
    public NovelTask getTask(Long id) {
        return novelTaskMapper.selectById(id);
    }

    /**
     * 获取项目的所有任务
     */
    public List<NovelTask> getTasksByProject(Long projectId) {
        return novelTaskMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<NovelTask>()
                .eq(NovelTask::getProjectId, projectId)
                .orderByDesc(NovelTask::getCreateTime)
        );
    }

    /**
     * 删除任务
     */
    public void deleteTask(Long id) {
        novelTaskMapper.deleteById(id);
    }

    /**
     * 获取待处理的任务列表
     */
    public List<NovelTask> getPendingTasks() {
        return novelTaskMapper.selectPendingTasks();
    }
}
