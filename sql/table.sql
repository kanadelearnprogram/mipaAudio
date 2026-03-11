CREATE TABLE mipaAudio (
                           id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
                           uploadUser VARCHAR(50) NOT NULL COMMENT '上传人',
                           audioName VARCHAR(255) NOT NULL COMMENT '音频文件名',
                           fileSize BIGINT NOT NULL COMMENT '文件大小(字节)',
                           category VARCHAR(50) DEFAULT '未分类' COMMENT '音频分类',
                           savePath VARCHAR(512) NOT NULL COMMENT '文件存储路径',
                           downloadCount INT DEFAULT 0 COMMENT '下载次数',
                           uploadTime DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
                           fileMd5 VARCHAR(32) NOT NULL COMMENT '文件唯一标识',
                           isDelete TINYINT DEFAULT 0 COMMENT '逻辑删除 0=未删除 1=已删除',

                           INDEX idx_fileMd5 (fileMd5),
                           INDEX idx_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='米帕音频表-驼峰+秒传+逻辑删除';