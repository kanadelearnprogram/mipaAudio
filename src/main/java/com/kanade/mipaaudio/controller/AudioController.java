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

    // todo 增查

    // todo 分类查询

}
