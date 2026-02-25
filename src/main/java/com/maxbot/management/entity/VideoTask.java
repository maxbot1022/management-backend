package com.maxbot.management.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 视频制作任务实体
 */
@Data
@TableName("tb_video_task")
public class VideoTask {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 关联项目ID
     */
    private Long projectId;
    
    /**
     * 关联小说任务ID
     */
    private Long novelTaskId;
    
    /**
     * 关联图片任务ID
     */
    private Long imageTaskId;
    
    /**
     * 任务名称
     */
    private String taskName;
    
    /**
     * 参考图片URL
     */
    private String referenceImageUrl;
    
    /**
     * 参考视频URL
     */
    private String referenceVideoUrl;
    
    /**
     * 制作要求/提示词
     */
    private String prompt;
    
    /**
     * 生成的视频URL
     */
    private String resultUrl;
    
    /**
     * 视频时长(秒)
     */
    private Integer duration;
    
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
