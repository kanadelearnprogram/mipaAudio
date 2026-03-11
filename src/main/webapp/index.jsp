<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>音频文件上传 - 米帕音频</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/upload.css">
</head>
<body>
<div class="container">
    <h1>🎵 音频文件上传</h1>
    
    <form id="uploadForm" action="<%= request.getContextPath() %>/api/audio/upload" method="post" enctype="multipart/form-data">
        <div class="form-group">
            <label for="uploadUser">上传人</label>
            <input type="text" id="uploadUser" name="uploadUser" value="anonymous" placeholder="请输入您的姓名或用户名">
        </div>

        <div class="form-group">
            <label for="category">音频分类</label>
            <select id="category" name="category">
                <option value="未分类">未分类</option>
                <option value="音乐">音乐</option>
                <option value="播客">播客</option>
                <option value="有声书">有声书</option>
                <option value="音效">音效</option>
                <option value="其他">其他</option>
            </select>
        </div>

        <div class="form-group">
            <label>选择音频文件</label>
            <div class="file-upload-wrapper">
                <label for="file" class="file-upload-label">
                    <div class="file-upload-icon">📁</div>
                    <div class="file-upload-text">点击选择文件或拖拽文件到此处</div>
                    <div class="file-upload-hint">支持 MP3、WAV、FLAC 等格式，最大 100MB</div>
                </label>
                <input type="file" id="file" name="file" accept="audio/*,.mp3,.wav,.flac,.aac,.ogg,.wma" required>
            </div>
            <div class="file-name" id="fileName"></div>
        </div>

        <button type="submit" class="btn-submit">🚀 开始上传</button>
        
        <div class="progress-bar" id="progressBar">
            <div class="progress-fill" id="progressFill"></div>
        </div>
    </form>

    <div class="result-message" id="resultMessage"></div>
</div>

<script>
    // 获取项目上下文路径 - 修复版本
    const contextPath = window.location.pathname === '/' ? '' : window.location.pathname.substring(0, window.location.pathname.indexOf('/', 2));
    
    console.log('计算的 ContextPath:', contextPath);
    
    // 显示选中的文件名
    document.getElementById('file').addEventListener('change', function(e) {
        const fileName = e.target.files[0] ? e.target.files[0].name : '';
        const fileNameDiv = document.getElementById('fileName');
        if (fileName) {
            fileNameDiv.textContent = '已选择：' + fileName;
            fileNameDiv.style.display = 'block';
        } else {
            fileNameDiv.style.display = 'none';
        }
    });

    // 表单提交处理
    document.getElementById('uploadForm').addEventListener('submit', async function(e) {
        e.preventDefault();
        
        const formData = new FormData(this);
        const resultMessage = document.getElementById('resultMessage');
        const progressBar = document.getElementById('progressBar');
        const progressFill = document.getElementById('progressFill');
        
        // 验证文件
        const fileInput = document.getElementById('file');
        if (!fileInput.files[0]) {
            showResult('请选择要上传的文件！', false);
            return;
        }
        
        console.log('开始上传...');
        console.log('请求 URL:', contextPath + '/api/audio/upload');
        console.log('文件大小:', fileInput.files[0].size, 'bytes');
        
        // 显示进度条
        progressBar.style.display = 'block';
        progressFill.style.width = '0%';
        
        try {
            // 模拟上传进度
            let progress = 0;
            const progressInterval = setInterval(() => {
                progress += Math.random() * 20;
                if (progress > 90) progress = 90;
                progressFill.style.width = progress + '%';
            }, 200);
            
            console.log('发送请求...');
            const response = await fetch(contextPath + '/api/audio/upload', {
                method: 'POST',
                body: formData
            });
            
            console.log('响应状态:', response.status);
            console.log('响应头:', response.headers);
            
            clearInterval(progressInterval);
            progressFill.style.width = '100%';
            
            // 检查响应是否为 JSON
            const contentType = response.headers.get('content-type');
            console.log('响应类型:', contentType);
            
            if (!contentType || !contentType.includes('application/json')) {
                const text = await response.text();
                console.error('非 JSON 响应:', text);
                throw new Error('服务器返回了非 JSON 响应：<br>' + text.substring(0, 200));
            }
            
            const result = await response.json();
            console.log('上传结果:', result);
            
            setTimeout(() => {
                progressBar.style.display = 'none';
                
                if (result.success) {
                    showResult('✅ ' + result.message + '<br><br>文件名：' + result.data.audioName + '<br>大小：' + formatFileSize(result.data.fileSize) + '<br>分类：' + result.data.category, true);
                    // 重置表单
                    document.getElementById('uploadForm').reset();
                    document.getElementById('fileName').style.display = 'none';
                } else {
                    showResult('❌ ' + result.message, false);
                }
            }, 500);
            
        } catch (error) {
            console.error('上传异常:', error);
            console.error('错误堆栈:', error.stack);
            progressBar.style.display = 'none';
            showResult('❌ 上传失败：' + error.message + '<br><br>请查看控制台获取详细信息。', false);
        }
    });
    
    // 显示结果消息
    function showResult(message, isSuccess) {
        const resultMessage = document.getElementById('resultMessage');
        resultMessage.innerHTML = message;
        resultMessage.className = 'result-message ' + (isSuccess ? 'result-success' : 'result-error');
        resultMessage.style.display = 'block';
        
        // 3 秒后自动隐藏（成功时）
        if (isSuccess) {
            setTimeout(() => {
                resultMessage.style.display = 'none';
            }, 5000);
        }
    }
    
    // 格式化文件大小
    function formatFileSize(bytes) {
        if (bytes < 1024) return bytes + ' B';
        if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(2) + ' KB';
        if (bytes < 1024 * 1024 * 1024) return (bytes / (1024 * 1024)).toFixed(2) + ' MB';
        return (bytes / (1024 * 1024 * 1024)).toFixed(2) + ' GB';
    }
</script>
</body>
</html>