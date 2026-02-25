package com.maxbot.management.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 图片制作任务实体
 */
@Data
@TableName("tb_image_task")
public class ImageTask {
    
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
     * 任务名称
     */
    private String taskName;
    
    /**
     * 参考图片URL
     */
    private String referenceImageUrl;
    
    /**
     * 制作要求/提示词
     */
    private String prompt;
    
    /**
     * 生成的图片URL
     */
    private String resultUrl;
    
    /**
     * 图片宽度
     */
    private Integer width;
    
    /**
     * 图片高度
     */
    private Integer height;
    
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
    
    /**
     * 是否为角色定妆照
     */
    private Integer isCharacterPhoto;
    
    /**
     * 角色名称
     */
    private String characterName;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;
}
