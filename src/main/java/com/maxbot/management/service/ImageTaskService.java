package com.maxbot.management.service;

import com.maxbot.management.entity.ImageTask;
import com.maxbot.management.mapper.ImageTaskMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 图片制作任务服务
 */
@Slf4j
@Service
public class ImageTaskService {

    @Autowired
    private ImageTaskMapper imageTaskMapper;

    @Autowired
    private VolcengineService volcengineService;

    @Autowired
    private OssService ossService;

    /**
     * 创建图片生成任务
     */
    public ImageTask createTask(Long projectId, Long novelTaskId, String taskName, 
                                 String referenceImageUrl, String prompt,
                                 Integer width, Integer height) {
        ImageTask task = new ImageTask();
        task.setProjectId(projectId);
        task.setNovelTaskId(novelTaskId);
        task.setTaskName(taskName);
        task.setReferenceImageUrl(referenceImageUrl);
        task.setPrompt(prompt);
        task.setWidth(width != null ? width : 1024);
        task.setHeight(height != null ? height : 1024);
        task.setStatus(0); // 待处理
        task.setRetryCount(0);
        task.setIsCharacterPhoto(0);
        imageTaskMapper.insert(task);
        log.info("创建图片生成任务成功: {}", task.getId());
        return task;
    }

    /**
     * 提交任务到火山引擎
     */
    public void submitToVolcengine(Long taskId) {
        ImageTask task = imageTaskMapper.selectById(taskId);
        if (task == null || task.getStatus() != 0) {
            throw new RuntimeException("任务不存在或状态不正确");
        }

        try {
            task.setStatus(1); // 处理中
            imageTaskMapper.updateById(task);

            // 提交到火山引擎
            String volcengineTaskId = volcengineService.submitImageTask(
                task.getPrompt(), 
                task.getReferenceImageUrl()
            );
            
            task.setVolcengineTaskId(volcengineTaskId);
            imageTaskMapper.updateById(task);
            
            log.info("图片任务提交到火山引擎成功: {}, taskId: {}", taskId, volcengineTaskId);
        } catch (Exception e) {
            log.error("图片任务提交到火山引擎失败: {}", taskId, e);
            task.setStatus(3); // 失败
            task.setErrorMsg(e.getMessage());
            task.setRetryCount(task.getRetryCount() + 1);
            imageTaskMapper.updateById(task);
        }
    }

    /**
     * 轮询并更新任务状态
     */
    public void pollTaskResult(Long taskId) {
        ImageTask task = imageTaskMapper.selectById(taskId);
        if (task == null || task.getStatus() != 1 || task.getVolcengineTaskId() == null) {
            return;
        }

        try {
            String result = volcengineService.queryImageTaskResult(task.getVolcengineTaskId());
            // TODO: 解析结果，下载图片上传到OSS
            log.info("图片任务轮询结果: {}", result);
        } catch (Exception e) {
            log.error("轮询图片任务结果失败: {}", taskId, e);
        }
    }

    /**
     * 标记为角色定妆照
     */
    public void markAsCharacterPhoto(Long taskId, String characterName) {
        ImageTask task = imageTaskMapper.selectById(taskId);
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }
        task.setIsCharacterPhoto(1);
        task.setCharacterName(characterName);
        imageTaskMapper.updateById(task);
    }

    /**
     * 获取项目的角色定妆照
     */
    public List<ImageTask> getCharacterPhotos(Long projectId) {
        return imageTaskMapper.selectCharacterPhotos(projectId);
    }

    /**
     * 获取任务详情
     */
    public ImageTask getTask(Long id) {
        return imageTaskMapper.selectById(id);
    }

    /**
     * 获取项目的所有图片任务
     */
    public List<ImageTask> getTasksByProject(Long projectId) {
        return imageTaskMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ImageTask>()
                .eq(ImageTask::getProjectId, projectId)
                .orderByDesc(ImageTask::getCreateTime)
        );
    }

    /**
     * 获取待处理的任务
     */
    public List<ImageTask> getPendingTasks() {
        return imageTaskMapper.selectPendingTasks();
    }

    /**
     * 删除任务
     */
    public void deleteTask(Long id) {
        imageTaskMapper.deleteById(id);
    }
}
