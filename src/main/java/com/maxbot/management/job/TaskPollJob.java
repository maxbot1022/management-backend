package com.maxbot.management.job;

import com.maxbot.management.entity.ImageTask;
import com.maxbot.management.entity.NovelTask;
import com.maxbot.management.entity.VideoTask;
import com.maxbot.management.service.ImageTaskService;
import com.maxbot.management.service.NovelTaskService;
import com.maxbot.management.service.VideoTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 任务轮询Job
 */
@Slf4j
@Component
public class TaskPollJob {

    @Autowired
    private NovelTaskService novelTaskService;

    @Autowired
    private ImageTaskService imageTaskService;

    @Autowired
    private VideoTaskService videoTaskService;

    /**
     * 每10秒轮询一次图片任务
     */
    @Scheduled(fixedRate = 10000)
    public void pollImageTasks() {
        List<ImageTask> pendingTasks = imageTaskService.getPendingTasks();
        for (ImageTask task : pendingTasks) {
            if (task.getStatus() == 1 && task.getVolcengineTaskId() != null) {
                try {
                    imageTaskService.pollTaskResult(task.getId());
                } catch (Exception e) {
                    log.error("轮询图片任务失败: {}", task.getId(), e);
                }
            }
        }
    }

    /**
     * 每10秒轮询一次视频任务
     */
    @Scheduled(fixedRate = 10000)
    public void pollVideoTasks() {
        List<VideoTask> pendingTasks = videoTaskService.getPendingTasks();
        for (VideoTask task : pendingTasks) {
            if (task.getStatus() == 1 && task.getVolcengineTaskId() != null) {
                try {
                    videoTaskService.pollTaskResult(task.getId());
                } catch (Exception e) {
                    log.error("轮询视频任务失败: {}", task.getId(), e);
                }
            }
        }
    }
}
