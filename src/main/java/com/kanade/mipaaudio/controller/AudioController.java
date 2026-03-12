package com.kanade.mipaaudio.controller;


import com.kanade.mipaaudio.entity.Audio;
import com.kanade.mipaaudio.service.AudioService;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.servlet.http.HttpServletResponse;
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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
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

        log.info("Controller 接收到同步上传请求，上传人：{}", uploadUser);
        Map<String, Object> result = audioService.uploadAudio(file, uploadUser, category);

        int status = Boolean.TRUE.equals((Boolean) result.get("success")) ? 200 : 500;
        return ResponseEntity.status(status).body(result);
    }

    /**
     * 获取音频文件列表（支持分页）
     * @param pageNum 页码，默认第 1 页
     * @param pageSize 每页大小，默认 10 条
     * @return 文件列表和分页信息
     */
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getAudioList(
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        log.info("获取音频文件列表，页码：{}，每页大小：{}", pageNum, pageSize);
        
        Map<String, Object> result = new HashMap<>();
        try {
            Map<String, Object> pageResult = audioService.getAudioListWithPagination(pageNum, pageSize);
            result.put("success", true);
            result.putAll(pageResult);
        } catch (Exception e) {
            log.error("获取文件列表失败", e);
            result.put("success", false);
            result.put("message", "获取文件列表失败：" + e.getMessage());
        }
        
        return ResponseEntity.ok(result);
    }

    /**
     * 下载音频文件
     * @param id 音频 ID
     * @param response HTTP 响应对象
     */
    @GetMapping("/download")
    public void downloadAudio(@RequestParam("id") Long id, jakarta.servlet.http.HttpServletResponse response) {
        log.info("下载音频文件，ID: {}", id);
        
        try {
            Map<String, Object> result = audioService.downloadAudio(id);
            
            if (!Boolean.TRUE.equals((Boolean) result.get("success"))) {
                log.error("下载失败：{}", result.get("message"));
                response.sendError(400, result.get("message").toString());
                return;
            }
            
            String filePath = (String) result.get("filePath");
            String fileName = (String) result.get("fileName");
            Long fileSize = (Long) result.get("fileSize");
            
            File file = new File(filePath);
            if (!file.exists()) {
                log.error("文件不存在：{}", filePath);
                response.sendError(404, "文件不存在");
                return;
            }
            
            // 设置响应头
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Length", String.valueOf(file.length()));
            
            // 处理中文文件名编码问题
            String encodedFileName = java.net.URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFileName);
            response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
            
            // 写入文件流到响应输出流
            try (FileInputStream inputStream = new FileInputStream(file);
                 OutputStream outputStream = response.getOutputStream()) {
                
                byte[] buffer = new byte[4096];
                int bytesRead;
                
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                
                outputStream.flush();
                log.info("文件下载成功：{}", fileName);
            }
            
        } catch (Exception e) {
            log.error("下载文件失败", e);
            try {
                response.sendError(500, "下载失败：" + e.getMessage());
            } catch (IOException ex) {
                log.error("发送错误响应失败", ex);
            }
        }
    }

    /**
     * 在线播放音频文件
     * @param id 音频 ID
     * @param response HTTP 响应对象
     */
    @GetMapping("/play")
    public void playAudio(@RequestParam("id") Long id, jakarta.servlet.http.HttpServletResponse response) {
        log.info("在线播放音频文件，ID: {}", id);
        
        try {
            // 调用 service 层获取文件信息（不更新下载次数）
            Map<String, Object> result = audioService.downloadAudio(id);
            
            if (!Boolean.TRUE.equals((Boolean) result.get("success"))) {
                log.error("播放失败：{}", result.get("message"));
                response.sendError(400, result.get("message").toString());
                return;
            }
            
            String filePath = (String) result.get("filePath");
            String fileName = (String) result.get("fileName");
            
            File file = new File(filePath);
            if (!file.exists()) {
                log.error("文件不存在：{}", filePath);
                response.sendError(404, "文件不存在");
                return;
            }
            
            // 根据文件扩展名判断 MIME 类型
            String mimeType = getAudioMimeType(fileName);
            response.setContentType(mimeType);
            response.setHeader("Content-Length", String.valueOf(file.length()));
            
            // 使用 inline 让浏览器直接播放而不是下载
            String encodedFileName = java.net.URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-Disposition", "inline; filename*=UTF-8''" + encodedFileName);
            response.setHeader("Accept-Ranges", "bytes");
            
            // 写入文件流到响应输出流
            try (FileInputStream inputStream = new FileInputStream(file);
                 OutputStream outputStream = response.getOutputStream()) {
                
                byte[] buffer = new byte[4096];
                int bytesRead;
                
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                
                outputStream.flush();
                log.info("音频播放成功：{}", fileName);
            }
            
        } catch (Exception e) {
            log.error("播放音频失败", e);
            try {
                response.sendError(500, "播放失败：" + e.getMessage());
            } catch (IOException ex) {
                log.error("发送错误响应失败", ex);
            }
        }
    }

    /**
     * 按分类查询音频文件列表（支持分页）
     * @param category 分类名称
     * @param pageNum 页码，默认第 1 页
     * @param pageSize 每页大小，默认 10 条
     * @return 文件列表和分页信息
     */
    @GetMapping("/list/by-category")
    public ResponseEntity<Map<String, Object>> getAudioListByCategory(
            @RequestParam("category") String category,
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        log.info("按分类查询音频文件列表，分类：{}，页码：{}，每页大小：{}", category, pageNum, pageSize);
        
        Map<String, Object> result = new HashMap<>();
        try {
            Map<String, Object> pageResult = audioService.getAudioListByCategory(category, pageNum, pageSize);
            result.put("success", true);
            result.putAll(pageResult);
        } catch (Exception e) {
            log.error("按分类查询失败", e);
            result.put("success", false);
            result.put("message", "按分类查询失败：" + e.getMessage());
        }
        
        return ResponseEntity.ok(result);
    }

    /**
     * 获取所有分类列表
     * @return 分类列表
     */
    @GetMapping("/categories")
    public ResponseEntity<Map<String, Object>> getAllCategories() {
        log.info("获取所有分类列表");
        
        Map<String, Object> result = new HashMap<>();
        try {
            Map<String, Object> categoriesResult = audioService.getAllCategories();
            result.put("success", true);
            result.putAll(categoriesResult);
        } catch (Exception e) {
            log.error("获取分类列表失败", e);
            result.put("success", false);
            result.put("message", "获取分类列表失败：" + e.getMessage());
        }
        
        return ResponseEntity.ok(result);
    }

    /**
     * 多文件上传接口
     * @param files 上传的文件数组
     * @param uploadUser 上传人
     * @param category 分类
     * @return 上传结果
     */
    @PostMapping("/upload-multiple")
    public ResponseEntity<Map<String, Object>> uploadMultipleFiles(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam(value = "uploadUser", defaultValue = "anonymous") String uploadUser,
            @RequestParam(value = "category", defaultValue = "未分类") String category) {

        log.info("Controller 接收到多文件上传请求，上传人：{}，文件数量：{}", uploadUser, files.length);
        Map<String, Object> result = audioService.uploadMultipleAudio(files, uploadUser, category);

        int status = Boolean.TRUE.equals((Boolean) result.get("success")) ? 200 : 500;
        return ResponseEntity.status(status).body(result);
    }

    /**
     * 根据文件扩展名获取音频 MIME 类型
     * @param fileName 文件名
     * @return MIME 类型
     */
    private String getAudioMimeType(String fileName) {
        if (fileName == null) {
            return "application/octet-stream";
        }
        
        String lowerCase = fileName.toLowerCase();
        if (lowerCase.endsWith(".mp3")) {
            return "audio/mpeg";
        } else if (lowerCase.endsWith(".wav")) {
            return "audio/wav";
        } else if (lowerCase.endsWith(".ogg")) {
            return "audio/ogg";
        } else if (lowerCase.endsWith(".aac")) {
            return "audio/aac";
        } else if (lowerCase.endsWith(".flac")) {
            return "audio/flac";
        } else if (lowerCase.endsWith(".m4a")) {
            return "audio/mp4";
        } else if (lowerCase.endsWith(".webm")) {
            return "audio/webm";
        } else {
            return "audio/mpeg"; // 默认返回 MP3 类型
        }
    }
}
