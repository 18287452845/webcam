<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!doctype html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta http-equiv="refresh" content="60;url=${pageContext.request.contextPath}/main.html">
    <title>NXNS云匹配认别系统 - 匹配结果</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/variables.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/animations.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/main.css">
</head>
<body>
    <div class="container">
        <div class="result-content">
            <div class="back-btn">
                <a href="${pageContext.request.contextPath}/main.html" title="返回首页"></a>
            </div>
            
            <video class="result-video" src="2.wav" id="video" autoplay loop muted>您所用的浏览器不支持视频标签</video>
            
            <div class="user-image left">
                <img src="${img}" alt="用户照片" width="205" height="205" onerror="this.style.display='none';">
            </div>
            
            <div class="user-image right">
                <img src="${ppei}" alt="匹配照片" width="205" height="205" data-match-gender="${gender == '男性' ? 'male' : 'female'}" onerror="handleImageError(this);">
            </div>
            
            <div class="main-content">
                <div class="info-section">
                    <h3 class="section-title">基本信息</h3>
                    <div class="info-grid">
                        <p>性别：<span class="p1 hidden-text">${gender}</span><span class="p11"></span></p>
                        <p>年龄：<span class="p4 hidden-text">${age}</span><span class="p44"></span></p>
                        <p>眼镜：<span class="p2 hidden-text">${eyestatus}</span><span class="p22"></span></p>
                        <p>笑容：<span class="p3 hidden-text">${smile}</span><span class="p33"></span></p>
                    </div>
                </div>
                
                <div class="praise-section">
                    <h3 class="section-title">AI评价</h3>
                    <p class="praise-text"><span class="p6 hidden-text">${praise}</span><span class="p66"></span></p>
                </div>
                
                <c:if test="${not empty healthAnalysis}">
                <div class="health-section">
                    <h3 class="section-title health-title">
                        <span class="health-icon">⚕</span>健康分析
                    </h3>
                    <div class="health-content">
                        <p class="health-text"><span class="p7 hidden-text">${healthAnalysis}</span><span class="p77"></span></p>
                    </div>
                </div>
                </c:if>
            </div>
            
            <div class="result-bg">
                <img src="${pageContext.request.contextPath}/images/third.png" alt="背景">
            </div>
            
            <div class="success-message">
                <h1>匹配成功</h1>
            </div>
        </div>
    </div>
    
    <script>
        (function() {
            'use strict';
            
            const contextPath = '${pageContext.request.contextPath}';
            
            /**
             * 将本地相对路径转换为带上下文路径的完整URL
             */
            function buildLocalImagePath(relativePath) {
                if (!relativePath) {
                    return contextPath || '/';
                }

                let normalizedPath = relativePath.trim();
                if (!normalizedPath.startsWith('/')) {
                    normalizedPath = '/' + normalizedPath;
                }

                if (!contextPath || contextPath === '/') {
                    return normalizedPath;
                }
                
                return contextPath + normalizedPath;
            }
            
            /**
             * 处理明星照片加载错误，使用本地备用图片
             */
            window.handleImageError = function(img) {
                console.error('Failed to load celebrity photo:', img.src);
                
                // 如果已经尝试过本地图片，不再重试，避免循环
                if (img.dataset.retried) {
                    console.error('Local fallback image also failed to load');
                    img.style.display = 'none';
                    return;
                }
                
                const gender = img.dataset.matchGender || 'male';
                const randomNum = Math.floor(Math.random() * 10) + 1;
                const fallbackSrc = buildLocalImagePath(`${gender}/${randomNum}.png`);
                
                console.log('Falling back to local image:', fallbackSrc);
                img.dataset.retried = 'true';
                img.src = fallbackSrc;
            };
            
            /**
             * 逐字显示文本动画
             */
            function animateText(sourceSelector, targetSelector, speed) {
                const source = document.querySelector(sourceSelector);
                const target = document.querySelector(targetSelector);
                
                if (!source || !target) return;
                
                const text = source.textContent || '';
                const chars = text.split('');
                let index = 0;
                
                target.textContent = '';
                
                function showNextChar() {
                    if (index < chars.length) {
                        target.textContent += chars[index];
                        index++;
                        setTimeout(showNextChar, speed);
                    }
                }
                
                showNextChar();
            }
            
            // 页面加载完成后启动动画
            document.addEventListener('DOMContentLoaded', function() {
                // 依次显示各项信息
                animateText('.p1', '.p11', 150);
                setTimeout(() => animateText('.p2', '.p22', 150), 500);
                setTimeout(() => animateText('.p3', '.p33', 150), 1600);
                setTimeout(() => animateText('.p4', '.p44', 150), 2000);
                setTimeout(() => animateText('.p6', '.p66', 80), 3000);
                setTimeout(() => animateText('.p7', '.p77', 60), 4500);
                
                const pauseTimes = [];
                
                // 根据描述长度计算暂停时间
                const descElement = document.querySelector('.p5');
                if (descElement) {
                    const descLength = descElement.textContent.length;
                    pauseTimes.push((descLength * 0.1 + 2.6) * 1000);
                }
                
                // 根据夸奖长度计算暂停时间
                const praiseElement = document.querySelector('.p6');
                if (praiseElement) {
                    const praiseLength = praiseElement.textContent.length;
                    pauseTimes.push((praiseLength * 0.08 + 3.0) * 1000);
                }
                
                // 根据健康分析长度计算暂停时间
                const healthElement = document.querySelector('.p7');
                if (healthElement) {
                    const healthLength = healthElement.textContent.length;
                    pauseTimes.push((healthLength * 0.06 + 4.5) * 1000);
                }
                
                if (pauseTimes.length > 0) {
                    const pauseTime = Math.max(...pauseTimes);
                    setTimeout(() => {
                        const video = document.getElementById('video');
                        if (video) {
                            video.pause();
                        }
                    }, pauseTime);
                }
            });
        })();
    </script>
</body>
</html>

