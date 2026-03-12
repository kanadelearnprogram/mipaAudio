package com.kanade.mipaaudio.service.impl;

import cn.hutool.crypto.digest.DigestUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.kanade.mipaaudio.entity.Audio;
import com.kanade.mipaaudio.mapper.AudioMapper;
import com.kanade.mipaaudio.service.AudioService;
import com.mybatisflex.core.query.QueryWrapper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class AudioServiceImpl extends ServiceImpl<AudioMapper, Audio> implements AudioService {

    @Autowired
    private AudioMapper audioMapper;

    private static final String UPLOAD_DIR = "E:/codelab/mipaAudio/files/";


    // TODO: 存储空间控制已通过 AOP 切面实现
    // 实现思路：
    // 1. 创建 StorageSpaceAspect 切面类，定义切点匹配所有上传方法
    // 2. 使用环绕通知在上传前检查存储空间
    // 3. 计算已使用空间和剩余空间
    // 4. 检查文件大小是否超过剩余空间
    // 5. 如果空间不足，返回错误信息；否则继续执行上传
    // 原因：使用 AOP 实现可以将空间检查逻辑与业务逻辑分离，提高代码可维护性
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> uploadAudio(MultipartFile file, String uploadUser, String category) {
        return handleFileUpload(file, uploadUser, category, false);
    }

    @Override
    @Async("audioUploadExecutor")
    @Transactional(rollbackFor = Exception.class)
    public CompletableFuture<Map<String, Object>> uploadAudioAsync(MultipartFile file, String uploadUser, String category) {
        Map<String, Object> result = handleFileUpload(file, uploadUser, category, true);
        return CompletableFuture.completedFuture(result);
    }


    private Map<String, Object> handleFileUpload(MultipartFile file, String uploadUser, String category, boolean isAsync) {
        Map<String, Object> result = new HashMap<>();
        String filePath = null; // 记录文件路径，用于失败时回滚

        if (file.isEmpty()) {
            log.warn("{}错误：上传文件为空", isAsync ? "[ASYNC] " : "");
            result.put("success", false);
            result.put("message", "上传文件不能为空");
            return result;
        }

        // 计算MD5 查看是否上传
        String fileMd5 = null;
        try {
            fileMd5 = calculateMD5(file);
            log.info("{}文件 MD5: {}", isAsync ? "[ASYNC] " : "", fileMd5);

            Audio existingAudio = checkExistingFile(fileMd5);
            log.info("{}检查秒传：{}", isAsync ? "[ASYNC] " : "", existingAudio != null ? "文件已存在" : "新文件");

            if (existingAudio != null) {
                log.info("{}触发秒传功能，删除重复文件", isAsync ? "[ASYNC] " : "");
                result.put("success", true);
                result.put("message", "文件已存在，使用秒传功能");
                result.put("data", existingAudio);
                result.put("isSecondTransfer", true);
                return result;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            // 生成路径
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String fileName = UUID.randomUUID().toString() + extension;
            log.info("{}生成的新文件名：{}", isAsync ? "[ASYNC] " : "", fileName);

            File uploadPath = new File(UPLOAD_DIR);
            if (!uploadPath.exists()) {
                boolean created = uploadPath.mkdirs();
                log.info("{}创建上传目录：{}, 结果：{}", isAsync ? "[ASYNC] " : "", UPLOAD_DIR, created);
                if (!created) {
                    log.error("{}错误：无法创建上传目录", isAsync ? "[ASYNC] " : "");
                    result.put("success", false);
                    result.put("message", "无法创建上传目录：" + UPLOAD_DIR);
                    return result;
                }
            }

            filePath = UPLOAD_DIR + fileName;
            File destFile = new File(filePath);
            log.info("{}保存文件路径：{}", isAsync ? "[ASYNC] " : "", filePath);
            
            // 保存文件到磁盘
            try (var inputStream = file.getInputStream()) {
                java.nio.file.Files.copy(
                    inputStream, 
                    destFile.toPath(), 
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING
                );
                log.info("{}文件保存成功！", isAsync ? "[ASYNC] " : "");
            }

            // 2. 保存数据库（在事务中）
            Audio audio = Audio.builder()
                    .uploadUser(uploadUser)
                    .audioName(originalFilename != null ? originalFilename : fileName)
                    .fileSize(file.getSize())
                    .category(category)
                    .savePath(filePath)
                    .downloadCount(0)
                    .uploadTime(LocalDateTime.now())
                    .fileMd5(fileMd5)
                    .isDelete(0)
                    .build();
            
            boolean saved = save(audio);
            log.info("{}数据库保存结果：{}", isAsync ? "[ASYNC] " : "", saved);
            
            if (saved) {
                log.info("{}========== 上传成功 ==========", isAsync ? "[ASYNC] " : "");
                result.put("success", true);
                result.put("message", "上传成功");
                result.put("data", audio);
                result.put("isSecondTransfer", false);
            } else {
                log.error("{}错误：保存到数据库失败", isAsync ? "[ASYNC] " : "");
                result.put("success", false);
                result.put("message", "保存到数据库失败");
                // 3. 数据库保存失败，删除已保存的文件
                if (filePath != null) {
                    File failedFile = new File(filePath);
                    if (failedFile.exists()) {
                        failedFile.delete();
                        log.info("{}已删除失败文件：{}", isAsync ? "[ASYNC] " : "", filePath);
                    }
                }
            }

        } catch (IOException e) {
            log.error("{}========== 上传失败 ==========", isAsync ? "[ASYNC] " : "", e);
            result.put("success", false);
            result.put("message", "文件上传失败：" + e.getMessage());
            // 4. 发生异常时，删除已保存的文件
            if (filePath != null) {
                File failedFile = new File(filePath);
                if (failedFile.exists()) {
                    failedFile.delete();
                    log.info("{}异常回滚：已删除文件 {}", isAsync ? "[ASYNC] " : "", filePath);
                }
            }
        }
        
        return result;
    }

    private Audio checkExistingFile(String fileMd5) {
        com.mybatisflex.core.query.QueryWrapper query = new com.mybatisflex.core.query.QueryWrapper();
        query.eq("fileMd5", fileMd5);
        List<Audio> existingList = list(query);
        return (existingList != null && !existingList.isEmpty()) ? existingList.get(0) : null;
    }

    private String calculateMD5(MultipartFile file) throws IOException {
        return DigestUtil.md5Hex(file.getInputStream());
    }

    /**
     * 分页查询音频文件列表
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    @Override
    @Transactional(readOnly = true) // 只读事务，提升性能
    public Map<String, Object> getAudioListWithPagination(Integer pageNum, Integer pageSize) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 创建分页对象
            Page page = new Page(pageNum, pageSize);
            
            // 构建查询条件（只查询未删除的）
            QueryWrapper query = new QueryWrapper();
            query.eq("isDelete", 0);
            // 按上传时间倒序排序
            query.orderBy("uploadTime", false);
            
            // 使用 Mapper 进行分页查询
            Page pageResult = audioMapper.paginate(page, query);
            
            // 封装返回结果
            result.put("data", pageResult.getRecords());
            result.put("total", pageResult.getTotalRow());
            result.put("pageNum", pageNum);
            result.put("pageSize", pageSize);
            result.put("totalPages", pageResult.getTotalPage());
            result.put("hasNext", pageNum < pageResult.getTotalPage());
            result.put("hasPrevious", pageNum > 1);
            
            log.info("分页查询成功，页码：{}，总数：{}，总页数：{}", 
                    pageNum, pageResult.getTotalRow(), pageResult.getTotalPage());
            
        } catch (Exception e) {
            log.error("分页查询失败", e);
            result.put("success", false);
            result.put("message", "分页查询失败：" + e.getMessage());
        }
        
        return result;
    }

    /**
     * 下载音频文件
     * @param id 音频 ID
     * @return 下载结果
     */
    @Override
    @Transactional(readOnly = true) // 只读事务
    public Map<String, Object> downloadAudio(Long id) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 1. 根据 ID 查询音频信息
            Audio audio = getById(id);
            if (audio == null || audio.getIsDelete() != null && audio.getIsDelete() == 1) {
                log.warn("音频文件不存在或已被删除，ID: {}", id);
                result.put("success", false);
                result.put("message", "音频文件不存在或已被删除");
                return result;
            }
            
            // 2. 检查文件是否存在
            File file = new File(audio.getSavePath());
            if (!file.exists()) {
                log.error("文件不存在：{}", audio.getSavePath());
                result.put("success", false);
                result.put("message", "文件已丢失：" + audio.getAudioName());
                return result;
            }
            
            // 3. 返回下载信息（下载次数由 AOP 切面更新）
            result.put("success", true);
            result.put("message", "准备下载");
            result.put("data", audio);
            result.put("filePath", audio.getSavePath());
            result.put("fileName", audio.getAudioName());
            result.put("fileSize", audio.getFileSize());
            result.put("downloadCount", audio.getDownloadCount() != null ? audio.getDownloadCount() : 0);
            
            log.info("下载信息准备完成，音频 ID: {}, 文件名：{}", id, audio.getAudioName());
            
        } catch (Exception e) {
            log.error("下载音频失败", e);
            result.put("success", false);
            result.put("message", "下载失败：" + e.getMessage());
        }
        
        return result;
    }

    /**
     * 按分类分页查询音频文件列表
     * @param category 分类名称
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    @Override
    @Transactional(readOnly = true) // 只读事务
    public Map<String, Object> getAudioListByCategory(String category, Integer pageNum, Integer pageSize) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 创建分页对象
            Page page = new Page(pageNum, pageSize);
            
            // 构建查询条件（只查询未删除的且分类匹配的）
            QueryWrapper query = new QueryWrapper();
            query.eq("isDelete", 0);
            query.eq("category", category);
            // 按上传时间倒序排序
            query.orderBy("uploadTime", false);
            
            // 使用 Mapper 进行分页查询
            Page pageResult = audioMapper.paginate(page, query);
            
            // 封装返回结果
            result.put("data", pageResult.getRecords());
            result.put("total", pageResult.getTotalRow());
            result.put("pageNum", pageNum);
            result.put("pageSize", pageSize);
            result.put("totalPages", pageResult.getTotalPage());
            result.put("hasNext", pageNum < pageResult.getTotalPage());
            result.put("hasPrevious", pageNum > 1);
            result.put("success", true);
            
            log.info("按分类查询成功，分类：{}，页码：{}，总数：{}，总页数：{}", 
                    category, pageNum, pageResult.getTotalRow(), pageResult.getTotalPage());
            
        } catch (Exception e) {
            log.error("按分类查询失败，分类：{}", category, e);
            result.put("success", false);
            result.put("message", "按分类查询失败：" + e.getMessage());
        }
        
        return result;
    }

    /**
     * 获取所有分类（去重）
     * @return 分类列表
     */
    @Override
    @Transactional(readOnly = true) // 只读事务
    public Map<String, Object> getAllCategories() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 构建查询条件（只查询未删除的）
            QueryWrapper query = new QueryWrapper();
            query.eq("isDelete", 0);
            // 使用 select 方法指定查询 category 字段
            query.select("DISTINCT category");
            
            List<Audio> audioList = list(query);
            
            // 提取分类名称列表
            List<String> categories = audioList.stream()
                    .map(Audio::getCategory)
                    .filter(cat -> cat != null && !cat.trim().isEmpty())
                    .distinct()
                    .sorted()
                    .toList();
            
            result.put("success", true);
            result.put("data", categories);
            result.put("count", categories.size());
            
            log.info("获取所有分类成功，分类数量：{}", categories.size());
            
        } catch (Exception e) {
            log.error("获取所有分类失败", e);
            result.put("success", false);
            result.put("message", "获取分类失败：" + e.getMessage());
        }
        
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> uploadMultipleAudio(MultipartFile[] files, String uploadUser, String category) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> fileResults = new ArrayList<>();
        boolean allSuccess = true;

        log.info("开始处理多文件上传，文件数量：{}", files.length);

        for (MultipartFile file : files) {
            Map<String, Object> fileResult = new HashMap<>();
            String fileName = file.getOriginalFilename();
            fileResult.put("fileName", fileName);

            try {
                Map<String, Object> uploadResult = handleFileUpload(file, uploadUser, category, false);
                boolean success = Boolean.TRUE.equals(uploadResult.get("success"));
                fileResult.put("status", success ? "success" : "error");
                fileResult.put("message", uploadResult.get("message"));
                fileResult.put("audio", success ? uploadResult.get("data") : null);
                
                if (!success) {
                    allSuccess = false;
                }
            } catch (Exception e) {
                log.error("文件上传失败：{}", fileName, e);
                fileResult.put("status", "error");
                fileResult.put("message", "上传失败：" + e.getMessage());
                fileResult.put("audio", null);
                allSuccess = false;
            }

            fileResults.add(fileResult);
        }

        result.put("success", allSuccess);
        result.put("message", allSuccess ? "所有文件上传成功" : "部分文件上传失败");
        result.put("data", fileResults);
        
        log.info("多文件上传处理完成，成功：{}", allSuccess);
        return result;
    }
}
