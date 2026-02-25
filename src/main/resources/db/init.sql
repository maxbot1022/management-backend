-- 小说转视频管理平台数据库初始化脚本

-- 创建数据库
CREATE DATABASE IF NOT EXISTS management_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE management_db;

-- 项目表
CREATE TABLE IF NOT EXISTS tb_project (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '项目ID',
    name VARCHAR(200) NOT NULL COMMENT '项目名称',
    description TEXT COMMENT '项目描述',
    status TINYINT DEFAULT 0 COMMENT '状态: 0-草稿, 1-进行中, 2-已完成, 3-已归档',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '是否删除: 0-否, 1-是',
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目表';

-- 小说整理任务表
CREATE TABLE IF NOT EXISTS tb_novel_task (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '任务ID',
    project_id BIGINT NOT NULL COMMENT '关联项目ID',
    task_name VARCHAR(200) NOT NULL COMMENT '任务名称',
    original_text_url VARCHAR(500) COMMENT '原始小说文本URL',
    prompt TEXT COMMENT '整理要求/提示词',
    result_json LONGTEXT COMMENT '整理后的分镜JSON',
    result_url VARCHAR(500) COMMENT '结果文件URL',
    status TINYINT DEFAULT 0 COMMENT '状态: 0-待处理, 1-处理中, 2-已完成, 3-失败',
    volcengine_task_id VARCHAR(100) COMMENT '火山引擎任务ID',
    error_msg TEXT COMMENT '错误信息',
    retry_count INT DEFAULT 0 COMMENT '重试次数',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_project_id (project_id),
    INDEX idx_status (status),
    INDEX idx_volcengine_task_id (volcengine_task_id),
    FOREIGN KEY (project_id) REFERENCES tb_project(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='小说整理任务表';

-- 图片制作任务表
CREATE TABLE IF NOT EXISTS tb_image_task (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '任务ID',
    project_id BIGINT NOT NULL COMMENT '关联项目ID',
    novel_task_id BIGINT COMMENT '关联小说任务ID',
    task_name VARCHAR(200) NOT NULL COMMENT '任务名称',
    reference_image_url VARCHAR(500) COMMENT '参考图片URL',
    prompt TEXT COMMENT '制作要求/提示词',
    result_url VARCHAR(500) COMMENT '生成图片URL',
    width INT DEFAULT 1024 COMMENT '图片宽度',
    height INT DEFAULT 1024 COMMENT '图片高度',
    status TINYINT DEFAULT 0 COMMENT '状态: 0-待处理, 1-处理中, 2-已完成, 3-失败',
    volcengine_task_id VARCHAR(100) COMMENT '火山引擎任务ID',
    error_msg TEXT COMMENT '错误信息',
    retry_count INT DEFAULT 0 COMMENT '重试次数',
    is_character_photo TINYINT DEFAULT 0 COMMENT '是否为角色定妆照: 0-否, 1-是',
    character_name VARCHAR(100) COMMENT '角色名称',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_project_id (project_id),
    INDEX idx_novel_task_id (novel_task_id),
    INDEX idx_status (status),
    INDEX idx_is_character_photo (is_character_photo),
    FOREIGN KEY (project_id) REFERENCES tb_project(id),
    FOREIGN KEY (novel_task_id) REFERENCES tb_novel_task(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图片制作任务表';

-- 视频制作任务表
CREATE TABLE IF NOT EXISTS tb_video_task (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '任务ID',
    project_id BIGINT NOT NULL COMMENT '关联项目ID',
    novel_task_id BIGINT COMMENT '关联小说任务ID',
    image_task_id BIGINT COMMENT '关联图片任务ID',
    task_name VARCHAR(200) NOT NULL COMMENT '任务名称',
    reference_image_url VARCHAR(500) COMMENT '参考图片URL',
    reference_video_url VARCHAR(500) COMMENT '参考视频URL',
    prompt TEXT COMMENT '制作要求/提示词',
    result_url VARCHAR(500) COMMENT '生成视频URL',
    duration INT DEFAULT 5 COMMENT '视频时长(秒)',
    status TINYINT DEFAULT 0 COMMENT '状态: 0-待处理, 1-处理中, 2-已完成, 3-失败',
    volcengine_task_id VARCHAR(100) COMMENT '火山引擎任务ID',
    error_msg TEXT COMMENT '错误信息',
    retry_count INT DEFAULT 0 COMMENT '重试次数',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_project_id (project_id),
    INDEX idx_novel_task_id (novel_task_id),
    INDEX idx_image_task_id (image_task_id),
    INDEX idx_status (status),
    FOREIGN KEY (project_id) REFERENCES tb_project(id),
    FOREIGN KEY (novel_task_id) REFERENCES tb_novel_task(id),
    FOREIGN KEY (image_task_id) REFERENCES tb_image_task(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='视频制作任务表';
