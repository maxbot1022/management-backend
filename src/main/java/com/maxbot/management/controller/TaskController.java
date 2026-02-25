package com.maxbot.management.controller;

import com.maxbot.management.entity.*;
import com.maxbot.management.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 任务管理控制器
 */
@RestController
@RequestMapping("/task")
@CrossOrigin(origins = "*")
public class TaskController {

    @Autowired
    private NovelTaskService novelTaskService;

    @Autowired
    private ImageTaskService imageTaskService;

    @Autowired
    private VideoTaskService videoTaskService;

    // ==================== 小说整理任务 ====================

    @PostMapping("/novel/create")
    public Map<String, Object> createNovelTask(@RequestBody Map<String, Object> params) {
        NovelTask task = novelTaskService.createTask(
            Long.valueOf(params.get("projectId").toString()),
            (String) params.get("taskName"),
            (String) params.get("originalTextUrl"),
            (String) params.get("prompt")
        );
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", task);
        result.put("msg", "创建成功");
        return result;
    }

    @PostMapping("/novel/submit/{id}")
    public Map<String, Object> submitNovelTask(@PathVariable Long id) {
        novelTaskService.submitToVolcengine(id);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("msg", "提交处理成功");
        return result;
    }

    @GetMapping("/novel/list/{projectId}")
    public Map<String, Object> listNovelTasks(@PathVariable Long projectId) {
        List<NovelTask> list = novelTaskService.getTasksByProject(projectId);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", list);
        return result;
    }

    // ==================== 图片制作任务 ====================

    @PostMapping("/image/create")
    public Map<String, Object> createImageTask(@RequestBody Map<String, Object> params) {
        Integer width = params.get("width") != null ? (Integer) params.get("width") : 1024;
        Integer height = params.get("height") != null ? (Integer) params.get("height") : 1024;
        
        ImageTask task = imageTaskService.createTask(
            Long.valueOf(params.get("projectId").toString()),
            params.get("novelTaskId") != null ? Long.valueOf(params.get("novelTaskId").toString()) : null,
            (String) params.get("taskName"),
            (String) params.get("referenceImageUrl"),
            (String) params.get("prompt"),
            width,
            height
        );
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", task);
        result.put("msg", "创建成功");
        return result;
    }

    @PostMapping("/image/submit/{id}")
    public Map<String, Object> submitImageTask(@PathVariable Long id) {
        imageTaskService.submitToVolcengine(id);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("msg", "提交处理成功");
        return result;
    }

    @PostMapping("/image/markCharacter/{id}")
    public Map<String, Object> markCharacterPhoto(@PathVariable Long id, @RequestBody Map<String, String> params) {
        imageTaskService.markAsCharacterPhoto(id, params.get("characterName"));
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("msg", "标记成功");
        return result;
    }

    @GetMapping("/image/characters/{projectId}")
    public Map<String, Object> getCharacterPhotos(@PathVariable Long projectId) {
        List<ImageTask> list = imageTaskService.getCharacterPhotos(projectId);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", list);
        return result;
    }

    @GetMapping("/image/list/{projectId}")
    public Map<String, Object> listImageTasks(@PathVariable Long projectId) {
        List<ImageTask> list = imageTaskService.getTasksByProject(projectId);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", list);
        return result;
    }

    // ==================== 视频制作任务 ====================

    @PostMapping("/video/create")
    public Map<String, Object> createVideoTask(@RequestBody Map<String, Object> params) {
        Integer duration = params.get("duration") != null ? (Integer) params.get("duration") : 5;
        
        VideoTask task = videoTaskService.createTask(
            Long.valueOf(params.get("projectId").toString()),
            params.get("novelTaskId") != null ? Long.valueOf(params.get("novelTaskId").toString()) : null,
            params.get("imageTaskId") != null ? Long.valueOf(params.get("imageTaskId").toString()) : null,
            (String) params.get("taskName"),
            (String) params.get("referenceImageUrl"),
            (String) params.get("referenceVideoUrl"),
            (String) params.get("prompt"),
            duration
        );
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", task);
        result.put("msg", "创建成功");
        return result;
    }

    @PostMapping("/video/submit/{id}")
    public Map<String, Object> submitVideoTask(@PathVariable Long id) {
        videoTaskService.submitToVolcengine(id);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("msg", "提交处理成功");
        return result;
    }

    @GetMapping("/video/list/{projectId}")
    public Map<String, Object> listVideoTasks(@PathVariable Long projectId) {
        List<VideoTask> list = videoTaskService.getTasksByProject(projectId);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", list);
        return result;
    }
}
