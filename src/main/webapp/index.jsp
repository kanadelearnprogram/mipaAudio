<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <style>
        /* 导航栏 */
        .navbar {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 30px;
            padding-bottom: 20px;
            border-bottom: 2px solid #e0e0e0;
        }

        .navbar h1 {
            margin: 0;
        }

        .btn-list {
            padding: 10px 20px;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            border: none;
            border-radius: 5px;
            cursor: pointer;
            font-size: 14px;
            transition: transform 0.2s, box-shadow 0.2s;
            text-decoration: none;
            display: inline-block;
        }

        .btn-list:hover {
            transform: translateY(-2px);
            box-shadow: 0 5px 20px rgba(102, 126, 234, 0.4);
        }

        /* 文件列表样式 */
        .file-list {
            margin-top: 15px;
        }

        .file-item {
            display: flex;
            align-items: center;
            padding: 10px;
            background: #f8f9fa;
            border-radius: 5px;
            margin-bottom: 8px;
            border-left: 4px solid #667eea;
        }

        .file-info {
            flex: 1;
        }

        .file-item-name {
            font-weight: bold;
            margin-bottom: 3px;
        }

        .file-item-size {
            font-size: 12px;
            color: #666;
        }

        .file-item-progress {
            width: 100%;
            height: 4px;
            background: #e0e0e0;
            border-radius: 2px;
            margin-top: 5px;
            overflow: hidden;
        }

        .file-item-progress-fill {
            height: 100%;
            background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
            width: 0%;
            transition: width 0.3s;
        }
    </style>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>音频文件上传 - 米帕音频</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/upload.css">

</head>
<body>
<div class="container">
    <div class="navbar">
        <h1>🎵 米帕音频</h1>
        <a href="${pageContext.request.contextPath}/audio-list.jsp" class="btn-list">📋 文件列表</a>
    </div>
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
                    <div class="file-upload-hint">支持 MP3、WAV、FLAC 等格式，最大 100MB，可选择多个文件</div>
                </label>
                <input type="file" id="file" name="files" accept="audio/*,.mp3,.wav,.flac,.aac,.ogg,.wma" multiple required>
            </div>
            <div class="file-name" id="fileName"></div>
            <div id="fileList" class="file-list"></div>
        </div>

        <button type="submit" class="btn-submit">🚀 开始上传</button>
        
        <div class="progress-bar" id="progressBar">
            <div class="progress-fill" id="progressFill"></div>
        </div>
    </form>

    <div class="result-message" id="resultMessage"></div>
</div>

<script>
    // 获取项目上下文路径 - 使用 JSP EL 表达式（更可靠）
    const contextPath = '${pageContext.request.contextPath}';
    console.log('ContextPath:', contextPath);
    
    // 显示选中的文件列表
    document.getElementById('file').addEventListener('change', function(e) {
        const files = e.target.files;
        const fileList = document.getElementById('fileList');
        const fileNameDiv = document.getElementById('fileName');
        
        // 清空文件列表
        fileList.innerHTML = '';
        
        if (files.length > 0) {
            fileNameDiv.textContent = '已选择 ' + files.length + ' 个文件';
            fileNameDiv.style.display = 'block';
            
            // 显示每个文件的信息
            for (let i = 0; i < files.length; i++) {
                const file = files[i];
                const fileItem = document.createElement('div');
                fileItem.className = 'file-item';
                fileItem.innerHTML = '<div class="file-info">' +
                    '<div class="file-item-name">' + file.name + '</div>' +
                    '<div class="file-item-size">' + formatFileSize(file.size) + '</div>' +
                    '<div class="file-item-progress">' +
                        '<div class="file-item-progress-fill" data-index="' + i + '"></div>' +
                    '</div>' +
                '</div>';
                fileList.appendChild(fileItem);
            }
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
        if (fileInput.files.length === 0) {
            showResult('请选择要上传的文件！', false);
            return;
        }
        
        console.log('开始上传...');
        console.log('请求 URL:', contextPath + '/api/audio/upload-multiple');
        console.log('文件数量:', fileInput.files.length);
        
        // 显示总进度条
        progressBar.style.display = 'block';
        progressFill.style.width = '0%';
        
        // 重置文件进度条
        const fileProgressFills = document.querySelectorAll('.file-item-progress-fill');
        fileProgressFills.forEach(fill => fill.style.width = '0%');
        
        try {
            // 模拟上传进度
            let progress = 0;
            const progressInterval = setInterval(() => {
                progress += Math.random() * 10;
                if (progress > 90) progress = 90;
                progressFill.style.width = progress + '%';
                
                // 随机更新每个文件的进度
                fileProgressFills.forEach(fill => {
                    const currentWidth = parseFloat(fill.style.width) || 0;
                    if (currentWidth < 90) {
                        fill.style.width = (currentWidth + Math.random() * 5) + '%';
                    }
                });
            }, 200);
            
            console.log('发送请求...');
            const response = await fetch(contextPath + '/api/audio/upload-multiple', {
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
                    // 显示成功消息
                    let successMessage = '✅ ' + result.message + '<br><br>';
                    result.data.forEach(item => {
                        if (item.status === 'success') {
                            successMessage += `📄 ${item.fileName} - ${item.message}<br>`;
                        }
                    });
                    showResult(successMessage, true);
                } else {
                    // 显示部分失败消息
                    let errorMessage = '❌ ' + result.message + '<br><br>';
                    result.data.forEach(item => {
                        if (item.status === 'error') {
                            errorMessage += `📄 ${item.fileName} - ${item.message}<br>`;
                        } else {
                            errorMessage += `✅ ${item.fileName} - ${item.message}<br>`;
                        }
                    });
                    showResult(errorMessage, false);
                }
                
                // 重置表单
                document.getElementById('uploadForm').reset();
                document.getElementById('fileName').style.display = 'none';
                document.getElementById('fileList').innerHTML = '';
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
        
        // 5 秒后自动隐藏（成功时）
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