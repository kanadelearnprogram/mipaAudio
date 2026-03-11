package com.kanade.mipaaudio.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import java.io.Serializable;
import java.time.LocalDateTime;

import java.io.Serial;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 米帕音频表-驼峰+秒传+逻辑删除 实体类。
 *
 * @author Lenovo
 * @since 2026-03-11
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("mipaaudio")
public class Audio implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 上传人
     */
    @Column("uploadUser")
    private String uploadUser;

    /**
     * 音频文件名
     */
    @Column("audioName")
    private String audioName;

    /**
     * 文件大小(字节)
     */
    @Column("fileSize")
    private Long fileSize;

    /**
     * 音频分类
     */
    private String category;

    /**
     * 文件存储路径
     */
    @Column("savePath")
    private String savePath;

    /**
     * 下载次数
     */
    @Column("downloadCount")
    private Integer downloadCount;

    /**
     * 上传时间
     */
    @Column("uploadTime")
    private LocalDateTime uploadTime;

    /**
     * 文件唯一标识
     */
    @Column("fileMd5")
    private String fileMd5;

    /**
     * 逻辑删除 0=未删除 1=已删除
     */
    @Column("isDelete")
    private Integer isDelete;

}
