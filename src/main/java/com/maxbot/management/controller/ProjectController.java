package com.maxbot.management.controller;

import com.maxbot.management.entity.Project;
import com.maxbot.management.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 项目管理控制器
 */
@RestController
@RequestMapping("/project")
@CrossOrigin(origins = "*")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    /**
     * 创建项目
     */
    @PostMapping("/create")
    public Map<String, Object> create(@RequestBody Map<String, String> params) {
        Project project = projectService.createProject(
            params.get("name"), 
            params.get("description")
        );
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", project);
        result.put("msg", "创建成功");
        return result;
    }

    /**
     * 更新项目
     */
    @PostMapping("/update/{id}")
    public Map<String, Object> update(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        projectService.updateProject(
            id,
            (String) params.get("name"),
            (String) params.get("description"),
            params.get("status") != null ? (Integer) params.get("status") : null
        );
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("msg", "更新成功");
        return result;
    }

    /**
     * 删除项目
     */
    @PostMapping("/delete/{id}")
    public Map<String, Object> delete(@PathVariable Long id) {
        projectService.deleteProject(id);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("msg", "删除成功");
        return result;
    }

    /**
     * 获取项目详情
     */
    @GetMapping("/detail/{id}")
    public Map<String, Object> detail(@PathVariable Long id) {
        Project project = projectService.getProject(id);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", project);
        return result;
    }

    /**
     * 获取项目列表
     */
    @GetMapping("/list")
    public Map<String, Object> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", projectService.listProjects(page, size, status));
        return result;
    }

    /**
     * 获取所有项目
     */
    @GetMapping("/all")
    public Map<String, Object> all() {
        List<Project> list = projectService.getAllProjects();
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", list);
        return result;
    }
}
