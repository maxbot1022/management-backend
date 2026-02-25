package com.maxbot.management.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.maxbot.management.entity.Project;
import com.maxbot.management.mapper.ProjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 项目管理服务
 */
@Slf4j
@Service
public class ProjectService {

    @Autowired
    private ProjectMapper projectMapper;

    /**
     * 创建项目
     */
    public Project createProject(String name, String description) {
        Project project = new Project();
        project.setName(name);
        project.setDescription(description);
        project.setStatus(0); // 草稿状态
        projectMapper.insert(project);
        log.info("创建项目成功: {}", project.getId());
        return project;
    }

    /**
     * 更新项目
     */
    public void updateProject(Long id, String name, String description, Integer status) {
        Project project = projectMapper.selectById(id);
        if (project == null) {
            throw new RuntimeException("项目不存在");
        }
        if (name != null) {
            project.setName(name);
        }
        if (description != null) {
            project.setDescription(description);
        }
        if (status != null) {
            project.setStatus(status);
        }
        projectMapper.updateById(project);
        log.info("更新项目成功: {}", id);
    }

    /**
     * 删除项目
     */
    public void deleteProject(Long id) {
        projectMapper.deleteById(id);
        log.info("删除项目成功: {}", id);
    }

    /**
     * 获取项目详情
     */
    public Project getProject(Long id) {
        return projectMapper.selectById(id);
    }

    /**
     * 分页查询项目列表
     */
    public Page<Project> listProjects(int page, int size, Integer status) {
        Page<Project> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Project> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(Project::getStatus, status);
        }
        wrapper.orderByDesc(Project::getCreateTime);
        return projectMapper.selectPage(pageParam, wrapper);
    }

    /**
     * 获取所有项目
     */
    public List<Project> getAllProjects() {
        LambdaQueryWrapper<Project> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Project::getCreateTime);
        return projectMapper.selectList(wrapper);
    }
}
