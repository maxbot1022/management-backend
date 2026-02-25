package com.maxbot.management.service;

import com.maxbot.management.entity.VideoTask;
import com.maxbot.management.mapper.VideoTaskMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 视频制作任务服务
 */
@Slf4j
@Service
public class VideoTaskService {

    @Autowired
    private VideoTaskMapper videoTaskMapper;

    @Autowired
    private VolcengineService volcengineService;

    @Autowired
    private OssService ossService;

    /**
     * 创建视频生成任务
     */
    public VideoTask createTask(Long projectId, Long novelTaskId, Long imageTaskId,
                                 String taskName, String referenceImageUrl, 
                                 String referenceVideoUrl, String prompt, Integer duration) {
        VideoTask task = new VideoTask();
        task.setProjectId(projectId);
        task.setNovelTaskId(novelTaskId);
        task.setImageTaskId(imageTaskId);
        task.setTaskName(taskName);
        task.setReferenceImageUrl(referenceImageUrl);
        task.setReferenceVideoUrl(referenceVideoUrl);
        task.setPrompt(prompt);
        task.setDuration(duration != null ? duration : 5);
        task.setStatus(0); // 待处理
        task.setRetryCount(0);
        videoTaskMapper.insert(task);
        log.info("创建视频生成任务成功: {}", task.getId());
        return task;
    }

    /**
     * 提交任务到火山引擎
     */
    public void submitToVolcengine(Long taskId) {
        VideoTask task = videoTaskMapper.selectById(taskId);
        if (task == null || task.getStatus() != 0) {
            throw new RuntimeException("任务不存在或状态不正确");
        }

        try {
            task.setStatus(1); // 处理中
            videoTaskMapper.updateById(task);

            // 提交到火山引擎
            String volcengineTaskId = volcengineService.submitVideoTask(
                task.getPrompt(),
                task.getReferenceImageUrl(),
                task.getReferenceVideoUrl()
            );
            
            task.setVolcengineTaskId(volcengineTaskId);
            videoTaskMapper.updateById(task);
            
            log.info("视频任务提交到火山引擎成功: {}, taskId: {}", taskId, volcengineTaskId);
        } catch (Exception e) {
            log.error("视频任务提交到火山引擎失败: {}", taskId, e);
            task.setStatus(3); // 失败
            task.setErrorMsg(e.getMessage());
            task.setRetryCount(task.getRetryCount() + 1);
            videoTaskMapper.updateById(task);
        }
    }

    /**
     * 轮询并更新任务状态
     */
    public void pollTaskResult(Long taskId) {
        VideoTask task = videoTaskMapper.selectById(taskId);
        if (task == null || task.getStatus() != 1 || task.getVolcengineTaskId() == null) {
            return;
        }

        try {
            String result = volcengineService.queryVideoTaskResult(task.getVolcengineTaskId());
            // TODO: 解析结果，下载视频上传到OSS
            log.info("视频任务轮询结果: {}", result);
        } catch (Exception e) {
            log.error("轮询视频任务结果失败: {}", taskId, e);
        }
    }

    /**
     * 获取任务详情
     */
    public VideoTask getTask(Long id) {
        return videoTaskMapper.selectById(id);
    }

    /**
     * 获取项目的所有视频任务
     */
    public List<VideoTask> getTasksByProject(Long projectId) {
        return videoTaskMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<VideoTask>()
                .eq(VideoTask::getProjectId, projectId)
                .orderByDesc(VideoTask::getCreateTime)
        );
    }

    /**
     * 获取待处理的任务
     */
    public List<VideoTask> getPendingTasks() {
        return videoTaskMapper.selectPendingTasks();
    }

    /**
     * 删除任务
     */
    public void deleteTask(Long id) {
        videoTaskMapper.deleteById(id);
    }
}
