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

    /**
     * 分页查询音频文件列表
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页结果，包含列表、总数、总页数等信息
     */
    Map<String, Object> getAudioListWithPagination(Integer pageNum, Integer pageSize);

    /**
     * 下载音频文件
     * @param id 音频 ID
     * @return 下载结果，包含文件路径、文件名等信息
     */
    Map<String, Object> downloadAudio(Long id);

    /**
     * 按分类分页查询音频文件列表
     * @param category 分类名称
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页结果，包含列表、总数、总页数等信息
     */
    Map<String, Object> getAudioListByCategory(String category, Integer pageNum, Integer pageSize);

    /**
     * 获取所有分类（去重）
     * @return 分类列表
     */
    Map<String, Object> getAllCategories();
}

