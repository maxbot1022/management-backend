package com.maxbot.management.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.maxbot.management.entity.NovelTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface NovelTaskMapper extends BaseMapper<NovelTask> {
    
    /**
     * 查询待处理或处理中的任务
     */
    @Select("SELECT * FROM tb_novel_task WHERE status IN (0, 1) AND deleted = 0")
    List<NovelTask> selectPendingTasks();
}
