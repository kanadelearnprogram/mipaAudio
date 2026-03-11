<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>音频文件列表 - 米帕音频</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        body {
            font-family: 'Microsoft YaHei', Arial, sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
            padding: 20px;
        }

        .container {
            background: white;
            border-radius: 10px;
            box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
            padding: 40px;
            max-width: 1200px;
            width: 100%;
            margin: 0 auto;
        }

        h1 {
            color: #333;
            text-align: center;
            margin-bottom: 30px;
            font-size: 28px;
        }

        .navbar {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 30px;
            padding-bottom: 20px;
            border-bottom: 2px solid #e0e0e0;
        }

        .btn-back {
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

        .btn-back:hover {
            transform: translateY(-2px);
            box-shadow: 0 5px 20px rgba(102, 126, 234, 0.4);
        }

        .refresh-btn {
            padding: 10px 20px;
            background: #28a745;
            color: white;
            border: none;
            border-radius: 5px;
            cursor: pointer;
            font-size: 14px;
            transition: all 0.2s;
        }

        .refresh-btn:hover {
            background: #218838;
        }

        .table-container {
            overflow-x: auto;
        }

        table {
            width: 100%;
            border-collapse: collapse;
        }

        thead {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
        }

        th {
            padding: 15px;
            text-align: left;
            font-weight: bold;
        }

        tbody tr {
            border-bottom: 1px solid #e0e0e0;
        }

        tbody tr:hover {
            background-color: #f8f9fa;
        }

        td {
            padding: 15px;
        }

        .upload-user-badge {
            display: inline-block;
            padding: 6px 12px;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            border-radius: 20px;
            font-size: 13px;
            font-weight: bold;
        }

        .file-size {
            font-family: 'Courier New', monospace;
            background: #f0f0f0;
            padding: 4px 8px;
            border-radius: 3px;
            font-size: 13px;
        }

        .download-count-badge {
            display: inline-block;
            padding: 4px 10px;
            background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
            color: white;
            border-radius: 15px;
            font-size: 12px;
            font-weight: bold;
            min-width: 60px;
            text-align: center;
        }

        .loading, .empty-data, .error-message {
            text-align: center;
            padding: 60px 20px;
        }

        .loading-spinner {
            border: 4px solid #f3f3f3;
            border-top: 4px solid #667eea;
            border-radius: 50%;
            width: 40px;
            height: 40px;
            animation: spin 1s linear infinite;
            margin: 0 auto 20px;
        }

        @keyframes spin {
            0% { transform: rotate(0deg); }
            100% { transform: rotate(360deg); }
        }

        .error-message {
            background: #f8d7da;
            color: #721c24;
            border: 1px solid #f5c6cb;
            border-radius: 5px;
            margin: 20px 0;
        }

        .pagination {
            display: flex;
            justify-content: center;
            align-items: center;
            margin-top: 30px;
            gap: 10px;
            flex-wrap: wrap;
        }

        .pagination button {
            padding: 8px 16px;
            background: white;
            border: 2px solid #667eea;
            color: #667eea;
            border-radius: 5px;
            cursor: pointer;
            font-size: 14px;
            transition: all 0.2s;
        }

        .pagination button:hover:not(:disabled) {
            background: #667eea;
            color: white;
            transform: translateY(-2px);
        }

        .pagination button:disabled {
            opacity: 0.5;
            cursor: not-allowed;
        }

        .pagination-info {
            color: #666;
            font-size: 14px;
            padding: 0 10px;
        }

        .btn-download {
            padding: 8px 16px;
            background: linear-gradient(135deg, #28a745 0%, #20c997 100%);
            color: white;
            border: none;
            border-radius: 5px;
            cursor: pointer;
            font-size: 14px;
            font-weight: bold;
            transition: all 0.2s;
            white-space: nowrap;
        }

        .btn-download:hover {
            background: linear-gradient(135deg, #218838 0%, #1aa179 100%);
            transform: translateY(-2px);
            box-shadow: 0 4px 12px rgba(40, 167, 69, 0.3);
        }

        .btn-download:active {
            transform: translateY(0);
        }
    </style>
</head>
<body>
<div class="container">
    <h1>📋 音频文件列表</h1>
    
    <div class="navbar">
        <div style="display: flex; gap: 10px; align-items: center;">
            <a href="${pageContext.request.contextPath}/index.jsp" class="btn-back">⬆ 上传音频</a>
            <a href="${pageContext.request.contextPath}/" class="btn-back">🏠 首页</a>
        </div>
        <button class="refresh-btn" onclick="loadAudioList(1)">🔄 刷新列表</button>
    </div>

    <div class="table-container">
        <table id="audioTable">
            <thead>
                <tr>
                    <th>序号</th>
                    <th>上传人</th>
                    <th>音频文件名</th>
                    <th>文件大小</th>
                    <th>下载量</th>
                    <th>上传时间</th>
                    <th>操作</th>
                </tr>
            </thead>
            <tbody id="audioTableBody">
                <!-- 数据将通过 JavaScript 动态加载 -->
            </tbody>
        </table>
        
        <div id="loading" class="loading">
            <div class="loading-spinner"></div>
            <div>正在加载文件列表...</div>
        </div>
        
        <div id="emptyData" class="empty-data" style="display: none;">
            <div style="font-size: 64px; margin-bottom: 20px;">📭</div>
            <div>暂无音频文件</div>
        </div>
        
        <div id="errorMessage" class="error-message" style="display: none;"></div>
    </div>

    <!-- 分页控件 -->
    <div class="pagination" id="pagination" style="display: none;">
        <button onclick="changePage(1)" id="btnFirst">⏮ 首页</button>
        <button onclick="changePage(currentPage - 1)" id="btnPrev">← 上一页</button>
        <span class="pagination-info" id="pageInfo"></span>
        <button onclick="changePage(currentPage + 1)" id="btnNext">下一页 →</button>
        <button onclick="changePage(totalPages)" id="btnLast">末页 ⏭</button>
    </div>
</div>

<script>
    // 获取项目上下文路径 - 使用更可靠的方式
    const contextPath = '${pageContext.request.contextPath}';
    console.log('ContextPath:', contextPath);
    
    // 全局变量
    let currentPage = 1;
    let totalPages = 1;
    let totalRecords = 0;
    let pageSize = 10;
    
    // 页面加载时获取音频列表
    document.addEventListener('DOMContentLoaded', function() {
        loadAudioList(1);
    });
    
    // 下载音频文件
    function downloadAudio(id) {
        console.log('下载音频，ID:', id);
        
        const downloadUrl = contextPath + '/api/audio/download?id=' + id;
        console.log('下载 URL:', downloadUrl);
        
        // 创建隐藏的 iframe 来触发下载
        const iframe = document.createElement('iframe');
        iframe.style.display = 'none';
        iframe.src = downloadUrl;
        document.body.appendChild(iframe);
        
        // 下载完成后移除 iframe
        setTimeout(() => {
            document.body.removeChild(iframe);
        }, 5000);
    }
    
    // 加载音频文件列表
    async function loadAudioList(page = 1) {
        // 确保 page 是有效的数字
        if (!page || page < 1) {
            page = 1;
        }
        
        const loadingDiv = document.getElementById('loading');
        const emptyDataDiv = document.getElementById('emptyData');
        const errorDiv = document.getElementById('errorMessage');
        const tableBody = document.getElementById('audioTableBody');
        const paginationDiv = document.getElementById('pagination');
        
        // 显示加载状态
        loadingDiv.style.display = 'block';
        emptyDataDiv.style.display = 'none';
        errorDiv.style.display = 'none';
        tableBody.innerHTML = '';
        paginationDiv.style.display = 'none';
        
        try {
            const url = contextPath + '/api/audio/list?pageNum=' + page + '&pageSize=' + pageSize;
            console.log('请求 API:', url);
            
            const response = await fetch(url);
            
            if (!response.ok) {
                throw new Error(`HTTP 错误：${response.status}`);
            }
            
            const result = await response.json();
            console.log('获取到的数据:', result);
            
            loadingDiv.style.display = 'none';
            
            if (result.success && result.data && result.data.length > 0) {
                // 更新分页信息
                currentPage = parseInt(result.pageNum) || page;
                totalPages = parseInt(result.totalPages) || 1;
                totalRecords = parseInt(result.total) || result.data.length;
                pageSize = parseInt(result.pageSize) || 10;
                
                console.log('分页信息 - 当前页:', currentPage, '总页数:', totalPages, '总记录数:', totalRecords);
                
                renderTable(result.data);
                renderPagination();
                paginationDiv.style.display = 'flex';
            } else if (result.success && (!result.data || result.data.length === 0)) {
                // 空数据
                currentPage = 1;
                totalPages = 1;
                totalRecords = 0;
                emptyDataDiv.style.display = 'block';
            } else {
                // 错误
                errorDiv.textContent = '❌ ' + (result.message || '获取数据失败');
                errorDiv.style.display = 'block';
            }
            
        } catch (error) {
            console.error('获取数据失败:', error);
            loadingDiv.style.display = 'none';
            errorDiv.textContent = '❌ 加载失败：' + error.message;
            errorDiv.style.display = 'block';
        }
    }
    
    // 渲染表格
    function renderTable(data) {
        const tableBody = document.getElementById('audioTableBody');
        
        data.forEach((audio, index) => {
            const row = document.createElement('tr');
            
            // 序号 = (当前页 - 1) * 每页大小 + 索引 + 1
            const indexCell = document.createElement('td');
            indexCell.textContent = (currentPage - 1) * pageSize + index + 1;
            row.appendChild(indexCell);
            
            // 上传人
            const userCell = document.createElement('td');
            const userBadge = document.createElement('span');
            userBadge.className = 'upload-user-badge';
            userBadge.textContent = audio.uploadUser || '匿名用户';
            userCell.appendChild(userBadge);
            row.appendChild(userCell);
            
            // 音频文件名
            const nameCell = document.createElement('td');
            const nameSpan = document.createElement('span');
            nameSpan.className = 'audio-name';
            nameSpan.textContent = audio.audioName || '未命名';
            nameCell.appendChild(nameSpan);
            row.appendChild(nameCell);
            
            // 文件大小
            const sizeCell = document.createElement('td');
            const sizeSpan = document.createElement('span');
            sizeSpan.className = 'file-size';
            sizeSpan.textContent = formatFileSize(audio.fileSize || 0);
            sizeCell.appendChild(sizeSpan);
            row.appendChild(sizeCell);
            
            // 下载量
            const downloadCell = document.createElement('td');
            const downloadBadge = document.createElement('span');
            downloadBadge.className = 'download-count-badge';
            downloadBadge.textContent = audio.downloadCount || 0;
            downloadCell.appendChild(downloadBadge);
            row.appendChild(downloadCell);
            
            // 上传时间
            const timeCell = document.createElement('td');
            timeCell.textContent = formatDateTime(audio.uploadTime);
            row.appendChild(timeCell);
            
            // 操作列（下载按钮）
            const actionCell = document.createElement('td');
            const downloadBtn = document.createElement('button');
            downloadBtn.className = 'btn-download';
            downloadBtn.textContent = '📥 下载';
            downloadBtn.onclick = function() { downloadAudio(audio.id); };
            actionCell.appendChild(downloadBtn);
            row.appendChild(actionCell);
            
            tableBody.appendChild(row);
        });
    }
    
    // 渲染分页控件
    function renderPagination() {
        const pageInfo = document.getElementById('pageInfo');
        const btnFirst = document.getElementById('btnFirst');
        const btnPrev = document.getElementById('btnPrev');
        const btnNext = document.getElementById('btnNext');
        const btnLast = document.getElementById('btnLast');
        
        // 确保值是数字
        const current = parseInt(currentPage) || 1;
        const total = parseInt(totalPages) || 1;
        const records = parseInt(totalRecords) || 0;
        
        console.log('分页控件 - 当前页:', current, '总页数:', total, '总记录数:', records);
        
        // 更新页码信息
        pageInfo.textContent = '第 ' + current + ' / ' + total + ' 页，共 ' + records + ' 条记录';
        
        // 更新按钮状态
        if (btnFirst) btnFirst.disabled = (current === 1);
        if (btnPrev) btnPrev.disabled = (current === 1);
        if (btnNext) btnNext.disabled = (current === total);
        if (btnLast) btnLast.disabled = (current === total);
    }
    
    // 切换页码
    function changePage(page) {
        const pageNum = parseInt(page);
        const total = parseInt(totalPages) || 1;
        
        if (pageNum < 1 || pageNum > total) {
            return;
        }
        
        loadAudioList(pageNum);
    }
    
    // 格式化文件大小
    function formatFileSize(bytes) {
        if (bytes < 1024) return bytes + ' B';
        if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(2) + ' KB';
        if (bytes < 1024 * 1024 * 1024) return (bytes / (1024 * 1024)).toFixed(2) + ' MB';
        return (bytes / (1024 * 1024 * 1024)).toFixed(2) + ' GB';
    }
    
    // 格式化日期时间
    function formatDateTime(dateTimeStr) {
        if (!dateTimeStr) return '未知';
        
        try {
            // 处理 LocalDateTime 的数组格式 [2026, 3, 11, 21, 47, 46]
            if (Array.isArray(dateTimeStr)) {
                const year = dateTimeStr[0];
                const month = String(dateTimeStr[1]).padStart(2, '0');
                const day = String(dateTimeStr[2]).padStart(2, '0');
                const hours = String(dateTimeStr[3]).padStart(2, '0');
                const minutes = String(dateTimeStr[4]).padStart(2, '0');
                const seconds = String(dateTimeStr[5]).padStart(2, '0');
                
                return year + '-' + month + '-' + day + ' ' + hours + ':' + minutes + ':' + seconds;
            }
            
            // 处理字符串格式
            const date = new Date(dateTimeStr);
            if (isNaN(date.getTime())) {
                return dateTimeStr;
            }
            
            const year = date.getFullYear();
            const month = String(date.getMonth() + 1).padStart(2, '0');
            const day = String(date.getDate()).padStart(2, '0');
            const hours = String(date.getHours()).padStart(2, '0');
            const minutes = String(date.getMinutes()).padStart(2, '0');
            const seconds = String(date.getSeconds()).padStart(2, '0');
            
            return year + '-' + month + '-' + day + ' ' + hours + ':' + minutes + ':' + seconds;
        } catch (e) {
            return dateTimeStr;
        }
    }
</script>
</body>
</html>
