package com.kanade.mipaaudio.service.impl;

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
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
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

    @Override
    public Map<String, Object> uploadAudio(MultipartFile file, String uploadUser, String category) {
        return handleFileUpload(file, uploadUser, category, false);
    }

    @Override
    @Async("audioUploadExecutor")
    public CompletableFuture<Map<String, Object>> uploadAudioAsync(MultipartFile file, String uploadUser, String category) {
        Map<String, Object> result = handleFileUpload(file, uploadUser, category, true);
        return CompletableFuture.completedFuture(result);
    }

    private Map<String, Object> handleFileUpload(MultipartFile file, String uploadUser, String category, boolean isAsync) {
        Map<String, Object> result = new HashMap<>();

        try {
            if (file.isEmpty()) {
                log.warn("{}错误：上传文件为空", isAsync ? "[ASYNC] " : "");
                result.put("success", false);
                result.put("message", "上传文件不能为空");
                return result;
            }

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

            String filePath = UPLOAD_DIR + fileName;
            File destFile = new File(filePath);
            log.info("{}保存文件路径：{}", isAsync ? "[ASYNC] " : "", filePath);
            
            try (var inputStream = file.getInputStream()) {
                java.nio.file.Files.copy(
                    inputStream, 
                    destFile.toPath(), 
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING
                );
                log.info("{}文件保存成功！", isAsync ? "[ASYNC] " : "");
            }

            String fileMd5 = calculateMD5(file);
            log.info("{}文件 MD5: {}", isAsync ? "[ASYNC] " : "", fileMd5);

            Audio existingAudio = checkExistingFile(fileMd5);
            log.info("{}检查秒传：{}", isAsync ? "[ASYNC] " : "", existingAudio != null ? "文件已存在" : "新文件");
            
            if (existingAudio != null) {
                destFile.delete();
                log.info("{}触发秒传功能，删除重复文件", isAsync ? "[ASYNC] " : "");
                result.put("success", true);
                result.put("message", "文件已存在，使用秒传功能");
                result.put("data", existingAudio);
                result.put("isSecondTransfer", true);
                return result;
            }

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
            }

        } catch (IOException e) {
            log.error("{}========== 上传失败 ==========", isAsync ? "[ASYNC] " : "", e);
            result.put("success", false);
            result.put("message", "文件上传失败：" + e.getMessage());
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
        return cn.hutool.crypto.digest.DigestUtil.md5Hex(file.getInputStream());
    }

    /**
     * 分页查询音频文件列表
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    @Override
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
}
