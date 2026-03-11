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

        .btn-play {
            padding: 8px 16px;
            background: linear-gradient(135deg, #007bff 0%, #0056b3 100%);
            color: white;
            border: none;
            border-radius: 5px;
            cursor: pointer;
            font-size: 14px;
            font-weight: bold;
            transition: all 0.2s;
            white-space: nowrap;
        }

        .btn-play:hover {
            background: linear-gradient(135deg, #0056b3 0%, #004085 100%);
            transform: translateY(-2px);
            box-shadow: 0 4px 12px rgba(0, 123, 255, 0.3);
        }

        .btn-play:active {
            transform: translateY(0);
        }

        /* 音频播放器弹窗 */
        .player-modal {
            display: none;
            position: fixed;
            z-index: 1000;
            left: 0;
            top: 0;
            width: 100%;
            height: 100%;
            background-color: rgba(0, 0, 0, 0.7);
            animation: fadeIn 0.3s;
        }

        @keyframes fadeIn {
            from { opacity: 0; }
            to { opacity: 1; }
        }

        .player-content {
            background: white;
            margin: 10% auto;
            padding: 30px;
            border-radius: 10px;
            width: 90%;
            max-width: 600px;
            box-shadow: 0 10px 40px rgba(0, 0, 0, 0.5);
            animation: slideDown 0.3s;
        }

        @keyframes slideDown {
            from {
                transform: translateY(-50px);
                opacity: 0;
            }
            to {
                transform: translateY(0);
                opacity: 1;
            }
        }

        .player-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 20px;
            padding-bottom: 15px;
            border-bottom: 2px solid #e0e0e0;
        }

        .player-title {
            font-size: 20px;
            font-weight: bold;
            color: #333;
        }

        .close-player {
            font-size: 32px;
            font-weight: bold;
            color: #999;
            cursor: pointer;
            transition: color 0.2s;
            line-height: 1;
        }

        .close-player:hover {
            color: #333;
        }

        .player-info {
            margin-bottom: 20px;
            padding: 15px;
            background: #f8f9fa;
            border-radius: 5px;
        }

        .player-info p {
            margin: 8px 0;
            color: #555;
        }

        .audio-player-wrapper {
            width: 100%;
            margin: 20px 0;
        }

        audio {
            width: 100%;
            height: 50px;
            border-radius: 5px;
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
        <div style="display: flex; gap: 10px; align-items: center;">
            <!-- 分类筛选下拉框 -->
            <select id="categoryFilter" onchange="loadAudioList(1)" style="padding: 8px 12px; border-radius: 5px; border: 2px solid #667eea; font-size: 14px; cursor: pointer;">
                <option value="">全部分类</option>
            </select>
            <button class="refresh-btn" onclick="loadAudioList(1)">🔄 刷新列表</button>
        </div>
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

    <!-- 音频播放器弹窗 -->
    <div id="playerModal" class="player-modal">
        <div class="player-content">
            <div class="player-header">
                <div class="player-title">🎵 在线播放</div>
                <span class="close-player" onclick="closePlayer()">&times;</span>
            </div>
            <div class="player-info">
                <p><strong>文件名：</strong><span id="playerFileName"></span></p>
                <p><strong>上传人：</strong><span id="playerUploadUser"></span></p>
                <p><strong>文件大小：</strong><span id="playerFileSize"></span></p>
            </div>
            <div class="audio-player-wrapper">
                <audio id="audioPlayer" controls autoplay>
                    <source src="" type="audio/mpeg">
                    您的浏览器不支持音频播放。
                </audio>
            </div>
        </div>
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
    let currentCategory = ''; // 当前选中的分类
    
    // 页面加载时获取音频列表和分类列表
    document.addEventListener('DOMContentLoaded', function() {
        loadCategories(); // 先加载分类列表
        loadAudioList(1); // 再加载音频列表
    });
    
    // 加载分类列表
    async function loadCategories() {
        try {
            const url = contextPath + '/api/audio/categories';
            console.log('加载分类列表:', url);
            
            const response = await fetch(url);
            if (!response.ok) {
                throw new Error(`HTTP 错误：${response.status}`);
            }
            
            const result = await response.json();
            console.log('分类数据:', result);
            
            if (result.success && result.data) {
                const categoryFilter = document.getElementById('categoryFilter');
                
                // 清空现有选项（保留“全部分类”）
                categoryFilter.innerHTML = '<option value="">全部分类</option>';
                
                // 添加分类选项
                result.data.forEach(category => {
                    const option = document.createElement('option');
                    option.value = category;
                    option.textContent = category;
                    categoryFilter.appendChild(option);
                });
                
                console.log('分类列表加载完成，共' + result.data.length + '个分类');
            }
        } catch (error) {
            console.error('加载分类列表失败:', error);
        }
    }
    
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
    
    // 在线播放音频文件
    function playAudio(id) {
        console.log('播放音频，ID:', id);
        
        // 获取音频信息（从已加载的数据中查找）
        const audioData = findAudioById(id);
        
        if (!audioData) {
            console.error('未找到音频数据，ID:', id);
            alert('未找到音频信息');
            return;
        }
        
        // 设置播放器信息
        const playerModal = document.getElementById('playerModal');
        const playerFileName = document.getElementById('playerFileName');
        const playerUploadUser = document.getElementById('playerUploadUser');
        const playerFileSize = document.getElementById('playerFileSize');
        const audioPlayer = document.getElementById('audioPlayer');
        
        playerFileName.textContent = audioData.audioName || '未知';
        playerUploadUser.textContent = audioData.uploadUser || '匿名';
        playerFileSize.textContent = formatFileSize(audioData.fileSize || 0);
        
        // 设置音频源
        const playUrl = contextPath + '/api/audio/play?id=' + id;
        console.log('播放 URL:', playUrl);
        
        audioPlayer.src = playUrl;
        audioPlayer.load();
        
        // 显示播放器弹窗
        playerModal.style.display = 'block';
        
        // 自动播放（浏览器可能阻止）
        audioPlayer.play().catch(error => {
            console.warn('自动播放被阻止，需要用户手动点击播放:', error);
        });
    }
    
    // 关闭播放器
    function closePlayer() {
        const playerModal = document.getElementById('playerModal');
        const audioPlayer = document.getElementById('audioPlayer');
        
        // 暂停播放
        audioPlayer.pause();
        audioPlayer.src = '';
        audioPlayer.load();
        
        // 隐藏弹窗
        playerModal.style.display = 'none';
    }
    
    // 根据 ID 查找音频数据
    function findAudioById(id) {
        // 从当前页面缓存的数据中查找（需要在 renderTable 时保存数据）
        if (window.currentAudioData && window.currentAudioData.length > 0) {
            return window.currentAudioData.find(audio => audio.id === id);
        }
        return null;
    }
    
    // 点击弹窗外部关闭播放器
    window.onclick = function(event) {
        const playerModal = document.getElementById('playerModal');
        if (event.target === playerModal) {
            closePlayer();
        }
    };
    
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
            // 根据是否有分类筛选条件，选择不同的 API
            let url;
            if (currentCategory && currentCategory.trim() !== '') {
                // 按分类查询
                url = contextPath + '/api/audio/list/by-category?category=' + encodeURIComponent(currentCategory) + '&pageNum=' + page + '&pageSize=' + pageSize;
            } else {
                // 查询全部
                url = contextPath + '/api/audio/list?pageNum=' + page + '&pageSize=' + pageSize;
            }
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
                
                // 保存当前数据，方便播放时查找
                window.currentAudioData = result.data;
                
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
            
            // 操作列（播放和下载按钮）
            const actionCell = document.createElement('td');
            const buttonContainer = document.createElement('div');
            buttonContainer.style.display = 'flex';
            buttonContainer.style.gap = '8px';
            
            // 播放按钮
            const playBtn = document.createElement('button');
            playBtn.className = 'btn-play';
            playBtn.textContent = '▶ 播放';
            playBtn.onclick = function() { playAudio(audio.id); };
            
            // 下载按钮
            const downloadBtn = document.createElement('button');
            downloadBtn.className = 'btn-download';
            downloadBtn.textContent = '📥 下载';
            downloadBtn.onclick = function() { downloadAudio(audio.id); };
            
            buttonContainer.appendChild(playBtn);
            buttonContainer.appendChild(downloadBtn);
            actionCell.appendChild(buttonContainer);
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
    
    // 分类筛选事件处理
    document.getElementById('categoryFilter').addEventListener('change', function(e) {
        currentCategory = e.target.value;
        console.log('分类筛选:', currentCategory ? currentCategory : '全部');
        // 切换分类时重置到第一页
        loadAudioList(1);
    });
    
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
