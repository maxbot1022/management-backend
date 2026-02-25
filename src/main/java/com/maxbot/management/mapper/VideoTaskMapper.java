package com.maxbot.management.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.maxbot.management.entity.VideoTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface VideoTaskMapper extends BaseMapper<VideoTask> {
    
    /**
     * 查询待处理或处理中的视频任务
     */
    @Select("SELECT * FROM tb_video_task WHERE status IN (0, 1) AND deleted = 0")
    List<VideoTask> selectPendingTasks();
}
