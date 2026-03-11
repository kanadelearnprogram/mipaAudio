package com.kanade.mipaaudio.controller;


import com.kanade.mipaaudio.entity.Audio;
import com.kanade.mipaaudio.service.AudioService;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/audio")
public class AudioController {

    @Autowired
    AudioService audioService;
    
    // 静态初始化块，用于验证 Controller 是否被加载
    static {
        System.out.println("===========================================");
        System.out.println("AudioController 已加载！");
        System.out.println("===========================================");
    }

    // 上传目录配置
    private static final String UPLOAD_DIR = "E:/codelab/mipaAudio/files/";

    /**
     * 健康检查接口
     * @return 健康状态
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Controller 运行正常！");
        result.put("timestamp", LocalDateTime.now().toString());
        log.info("Health check endpoint called");
        return ResponseEntity.ok(result);
    }

    /**
     * 文件上传接口
     * @param file 上传的文件
     * @param uploadUser 上传人
     * @param category 分类
     * @return 上传结果
     */

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "uploadUser", defaultValue = "anonymous") String uploadUser,
            @RequestParam(value = "category", defaultValue = "未分类") String category) {
        
        Map<String, Object> result = new HashMap<>();
        
        // 日志：开始上传
        log.info("========== 文件上传开始 ==========");
        log.info("上传人：{}", uploadUser);
        log.info("分类：{}", category);
        log.info("原始文件名：{}", file.getOriginalFilename());
        log.info("文件大小：{} bytes", file.getSize());
        log.info("文件类型：{}", file.getContentType());
        
        // 验证文件是否为空
        if (file.isEmpty()) {
            log.warn("错误：上传文件为空");
            result.put("success", false);
            result.put("message", "上传文件不能为空");
            return ResponseEntity.badRequest().body(result);
        }
        
        try {
            // 生成唯一的文件名
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String fileName = UUID.randomUUID().toString() + extension;
            log.info("生成的新文件名：{}", fileName);
            
            // 创建上传目录
            File uploadPath = new File(UPLOAD_DIR);
            if (!uploadPath.exists()) {
                boolean created = uploadPath.mkdirs();
                log.info("创建上传目录：{}, 结果：{}", UPLOAD_DIR, created);
                if (!created) {
                    log.error("错误：无法创建上传目录");
                    result.put("success", false);
                    result.put("message", "无法创建上传目录：" + UPLOAD_DIR);
                    return ResponseEntity.internalServerError().body(result);
                }
            }
            
            // 保存文件
            String filePath = UPLOAD_DIR + fileName;
            File destFile = new File(filePath);
            log.info("保存文件路径：{}", filePath);
            log.info("目标文件绝对路径：{}", destFile.getAbsolutePath());
            
            // 使用文件复制方式保存（更可靠的方式）
            try (var inputStream = file.getInputStream()) {
                java.nio.file.Files.copy(
                    inputStream, 
                    destFile.toPath(), 
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING
                );
                log.info("文件保存成功！");
            }
            
            // 计算文件 MD5
            String fileMd5 = calculateMD5(file);
            log.info("文件 MD5: {}", fileMd5);
            
            // 检查是否已存在相同文件（秒传）
            QueryWrapper query = new QueryWrapper();
            query.eq("fileMd5", fileMd5);
            List<Audio> existingList = audioService.list(query);
            Audio existingAudio = null;
            if (existingList != null && !existingList.isEmpty()) {
                existingAudio = existingList.get(0);
            }
            log.info("检查秒传：{}", existingAudio != null ? "文件已存在" : "新文件");
            if (existingAudio != null) {
                // 删除刚上传的文件（因为已经存在）
                destFile.delete();
                result.put("success", true);
                result.put("message", "文件已存在，使用秒传功能");
                result.put("data", existingAudio);
                return ResponseEntity.ok(result);
            }
            
            // 创建音频记录
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
            
            // 保存到数据库
            boolean saved = audioService.save(audio);
            log.info("数据库保存结果：{}", saved);
            
            if (saved) {
                log.info("========== 上传成功 ==========");
                result.put("success", true);
                result.put("message", "上传成功");
                result.put("data", audio);
                return ResponseEntity.ok(result);
            } else {
                log.error("错误：保存到数据库失败");
                result.put("success", false);
                result.put("message", "保存到数据库失败");
                return ResponseEntity.internalServerError().body(result);
            }
            
        } catch (IOException e) {
            log.error("========== 上传失败 ==========", e);
            log.error("异常类型：{}", e.getClass().getName());
            log.error("异常信息：{}", e.getMessage());
            result.put("success", false);
            result.put("message", "文件上传失败：" + e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }
    
    /**
     * 计算文件的 MD5 值
     */
    private String calculateMD5(MultipartFile file) throws IOException {
        // 使用 cn.hutool 工具类计算 MD5
        return cn.hutool.crypto.digest.DigestUtil.md5Hex(file.getInputStream());
    }

    // todo 增查

    // todo 分类查询

    // todo list

}
