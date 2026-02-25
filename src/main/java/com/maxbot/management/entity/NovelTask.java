package com.maxbot.management.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 小说整理任务实体
 */
@Data
@TableName("tb_novel_task")
public class NovelTask {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 关联项目ID
     */
    private Long projectId;
    
    /**
     * 任务名称
     */
    private String taskName;
    
    /**
     * 原始小说文本URL
     */
    private String originalTextUrl;
    
    /**
     * 整理要求/提示词
     */
    private String prompt;
    
    /**
     * 整理后的分镜JSON
     */
    private String resultJson;
    
    /**
     * 结果文件URL
     */
    private String resultUrl;
    
    /**
     * 任务状态: 0-待处理, 1-处理中, 2-已完成, 3-失败
     */
    private Integer status;
    
    /**
     * 火山引擎任务ID
     */
    private String volcengineTaskId;
    
    /**
     * 错误信息
     */
    private String errorMsg;
    
    /**
     * 重试次数
     */
    private Integer retryCount;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;
}
