package com.maxbot.management.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.maxbot.management.entity.ImageTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ImageTaskMapper extends BaseMapper<ImageTask> {
    
    /**
     * 查询待处理或处理中的图片任务
     */
    @Select("SELECT * FROM tb_image_task WHERE status IN (0, 1) AND deleted = 0")
    List<ImageTask> selectPendingTasks();
    
    /**
     * 查询项目的角色定妆照
     */
    @Select("SELECT * FROM tb_image_task WHERE project_id = #{projectId} AND is_character_photo = 1 AND deleted = 0")
    List<ImageTask> selectCharacterPhotos(Long projectId);
}
