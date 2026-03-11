package com.kanade.mipaaudio.service;

import com.mybatisflex.core.service.IService;
import com.kanade.mipaaudio.entity.Audio;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 米帕音频表 - 驼峰 + 秒传 + 逻辑删除 服务层。
 *
 * @author Lenovo
 * @since 2026-03-11
 */
public interface AudioService extends IService<Audio> {
    /**
     * 同步上传音频文件
     * @param file 上传的文件
     * @param uploadUser 上传人
     * @param category 分类
     * @return 上传结果
     */
    Map<String, Object> uploadAudio(MultipartFile file, String uploadUser, String category);

    /**
     * 异步上传音频文件
     * @param file 上传的文件
     * @param uploadUser 上传人
     * @param category 分类
     * @return CompletableFuture 包含上传结果
     */
    CompletableFuture<Map<String, Object>> uploadAudioAsync(MultipartFile file, String uploadUser, String category);
}

