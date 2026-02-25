package com.maxbot.management.service;

import com.maxbot.management.entity.NovelTask;
import com.maxbot.management.mapper.NovelTaskMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
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
     * 根据整本小说文本URL，自动分段并批量创建小说整理任务
     * 每一段都会生成一条 status=0 的 NovelTask，后续通过 /task/novel/submit/{id} 手动触发处理
     */
    public List<NovelTask> createTasksFromWholeNovel(Long projectId,
                                                     String taskNamePrefix,
                                                     String originalTextUrl,
                                                     String prompt) {
        // 1. 下载整本文本
        log.info("开始根据整本小说创建分段任务, projectId={}, originalTextUrl={}", projectId, originalTextUrl);
        String fullText = downloadTextFromOss(originalTextUrl);
        if (fullText == null) {
            fullText = "";
        }
        log.info("整本小说文本下载完成, length={}", fullText.length());

        // 2. 按长度规则切分为多段
        // 为了避免单段文本过长导致 LLM 超限，这里控制每段的最大字符数
        final int maxSegmentChars = 3000;
        List<String> segments = splitTextIntoSegments(fullText, maxSegmentChars);
        log.info("整本小说切分完成, 共生成 {} 段文本", segments.size());

        // 3. 为每一段上传独立的文本到 OSS，并创建对应的 NovelTask
        List<NovelTask> createdTasks = new ArrayList<>();
        int index = 1;
        for (String segment : segments) {
            if (segment == null || segment.trim().isEmpty()) {
                index++;
                continue;
            }

            try {
                String objectName = ossService.generateObjectName(
                        "texts",
                        "novel_segment_" + System.currentTimeMillis() + "_" + index + ".txt"
                );
                ByteArrayInputStream inputStream = new ByteArrayInputStream(segment.getBytes(StandardCharsets.UTF_8));
                String segmentUrl = ossService.uploadInputStream(objectName, inputStream);

                NovelTask task = new NovelTask();
                task.setProjectId(projectId);
                String taskName = (taskNamePrefix != null && !taskNamePrefix.isEmpty())
                        ? taskNamePrefix + " - 段" + index
                        : "小说分镜 - 段" + index;
                task.setTaskName(taskName);
                task.setOriginalTextUrl(segmentUrl);
                task.setPrompt(prompt);
                task.setStatus(0);
                task.setRetryCount(0);

                novelTaskMapper.insert(task);
                createdTasks.add(task);
                log.info("创建分段小说整理任务成功, taskId={}, index={}", task.getId(), index);
            } catch (Exception e) {
                log.error("创建分段小说整理任务失败, index={}", index, e);
            }

            index++;
        }

        log.info("整本小说分段任务创建完成, 成功创建 {} 条任务", createdTasks.size());
        return createdTasks;
    }

    /**
     * 提交任务到火山引擎处理
     */
    public void submitToVolcengine(Long taskId) {
        log.info("开始提交小说整理任务到火山引擎, taskId={}", taskId);
        NovelTask task = novelTaskMapper.selectById(taskId);
        if (task == null || task.getStatus() != 0) {
            log.warn("提交小说整理任务失败, 任务不存在或状态不正确, taskId={}, status={}", 
                    taskId, task == null ? null : task.getStatus());
            throw new RuntimeException("任务不存在或状态不正确");
        }

        try {
            // 从OSS下载原始文本内容
            log.info("开始从OSS下载原始文本内容, taskId={}, url={}", taskId, task.getOriginalTextUrl());
            String textContent = downloadTextFromOss(task.getOriginalTextUrl());
            log.info("从OSS下载原始文本完成, taskId={}, textLength={}", taskId, 
                    textContent == null ? 0 : textContent.length());
            
            // 调用火山引擎处理
            log.info("开始调用火山引擎小说整理接口, taskId={}, promptLength={}, textLength={}", 
                    taskId, 
                    task.getPrompt() == null ? 0 : task.getPrompt().length(),
                    textContent == null ? 0 : textContent.length());
            String result = volcengineService.processNovelText(textContent, task.getPrompt());
            log.info("火山引擎小说整理接口调用结束, taskId={}, resultLength={}", taskId, 
                    result == null ? 0 : result.length());
            
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
     * 将整本文本按最大字符数切分为多段
     * 优先按空行分段，其次在段内按句号/问号/感叹号尽量在句子边界切分
     */
    private List<String> splitTextIntoSegments(String fullText, int maxSegmentChars) {
        List<String> segments = new ArrayList<>();
        if (fullText == null || fullText.isEmpty()) {
            return segments;
        }

        // 先按空行粗略分段
        String[] paragraphs = fullText.split("\\n\\s*\\n");
        StringBuilder current = new StringBuilder();

        for (String paragraph : paragraphs) {
            if (paragraph == null) {
                continue;
            }
            String trimmedParagraph = paragraph.trim();
            if (trimmedParagraph.isEmpty()) {
                continue;
            }

            // 如果当前段落本身就比 maxSegmentChars 大，再细分
            if (trimmedParagraph.length() > maxSegmentChars) {
                // 先把已有的 current 收尾
                if (current.length() > 0) {
                    segments.add(current.toString());
                    current.setLength(0);
                }

                // 在段落内部按句号/问号/感叹号切分
                String[] sentences = trimmedParagraph.split("(?<=[。？！])");
                StringBuilder inner = new StringBuilder();
                for (String sentence : sentences) {
                    if (inner.length() + sentence.length() > maxSegmentChars) {
                        if (inner.length() > 0) {
                            segments.add(inner.toString());
                            inner.setLength(0);
                        }
                    }
                    inner.append(sentence);
                }
                if (inner.length() > 0) {
                    segments.add(inner.toString());
                }
                continue;
            }

            // 普通情况：尝试往 current 里追加本段
            if (current.length() + trimmedParagraph.length() + 2 <= maxSegmentChars) {
                if (current.length() > 0) {
                    current.append("\n\n");
                }
                current.append(trimmedParagraph);
            } else {
                // 当前已经接近上限，先收尾，再开启新段
                if (current.length() > 0) {
                    segments.add(current.toString());
                    current.setLength(0);
                }
                current.append(trimmedParagraph);
            }
        }

        if (current.length() > 0) {
            segments.add(current.toString());
        }

        return segments;
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
