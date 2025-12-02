/**
 * Index Page - Home Page Interactions
 * 首页交互功能
 * 
 * @version 2.0.0
 */

(function () {
    'use strict';

    // ========== DOM 元素引用 ==========
    const elements = {
        video: document.querySelector('.home-video'),
        genderButtons: document.querySelectorAll('.gender-btn'),
        container: document.querySelector('.home-content')
    };

    // ========== 工具函数 ==========

    /**
     * 添加页面加载动画
     */
    function addLoadingAnimation() {
        if (elements.container) {
            elements.container.classList.add('animate-fade-in');
        }

        // 依次为元素添加淡入动画
        const animatedElements = [
            elements.video,
            document.querySelector('.home-bg'),
            ...elements.genderButtons
        ].filter(Boolean);

        animatedElements.forEach((el, index) => {
            setTimeout(() => {
                el.classList.add('animate-fade-in-up');
            }, index * 100);
        });
    }

    /**
     * 优化视频播放
     */
    function optimizeVideoPlayback() {
        if (!elements.video) return;

        // 确保视频自动播放（某些浏览器需要用户交互）
        const playVideo = () => {
            elements.video.play().catch(error => {
                console.log('Video autoplay was prevented:', error);
                // 如果自动播放被阻止，添加点击事件
                document.addEventListener('click', () => {
                    elements.video.play();
                }, { once: true });
            });
        };

        // 当视频可以播放时
        elements.video.addEventListener('canplay', playVideo, { once: true });

        // 视频加载失败处理
        elements.video.addEventListener('error', (e) => {
            console.error('Video failed to load:', e);
        });
    }

    /**
     * 添加按钮交互效果
     */
    function enhanceButtonInteractions() {
        elements.genderButtons.forEach(button => {
            // 悬停音效（可选）
            button.addEventListener('mouseenter', () => {
                button.style.transform = 'scale(1.05)';
            });

            button.addEventListener('mouseleave', () => {
                button.style.transform = 'scale(1)';
            });

            // 点击反馈
            button.addEventListener('click', (e) => {
                button.classList.add('animate-pulse');

                // 添加点击动画，让用户看到反馈
                // 跳转通过HTML中的href属性自然发生
                setTimeout(() => {
                    // 按钮已配置href属性，会自动跳转
                }, 200);
            });

            // 添加键盘支持
            button.addEventListener('keypress', (e) => {
                if (e.key === 'Enter' || e.key === ' ') {
                    e.preventDefault();
                    button.click();
                }
            });
        });
    }

    /**
     * 添加提示信息
     */
    function addTooltips() {
        elements.genderButtons.forEach(button => {
            const title = button.getAttribute('title');
            if (!title) return;

            // 为无障碍访问添加 aria-label
            button.setAttribute('aria-label', `选择${title}进行人脸识别`);
            button.setAttribute('role', 'button');
            button.setAttribute('tabindex', '0');
        });
    }

    /**
     * 预加载重要资源
     */
    function preloadResources() {
        // 预加载拍照页面的图片资源
        const preloadImages = [
            'images/sec.png',
            'saomiao.gif'
        ];

        preloadImages.forEach(src => {
            const img = new Image();
            img.src = src;
        });
    }

    /**
     * 性能监控（开发模式）
     */
    function monitorPerformance() {
        if (typeof performance === 'undefined') return;

        window.addEventListener('load', () => {
            // 页面加载时间
            const loadTime = performance.timing.loadEventEnd - performance.timing.navigationStart;
            console.log(`Page load time: ${loadTime}ms`);

            // 如果有 Performance Observer API
            if (typeof PerformanceObserver !== 'undefined') {
                const observer = new PerformanceObserver((list) => {
                    for (const entry of list.getEntries()) {
                        console.log('Performance entry:', entry);
                    }
                });

                observer.observe({ entryTypes: ['paint', 'navigation'] });
            }
        });
    }

    /**
     * 检查浏览器兼容性
     */
    function checkBrowserCompatibility() {
        const features = {
            mediaDevices: navigator.mediaDevices && navigator.mediaDevices.getUserMedia,
            fetch: typeof fetch !== 'undefined',
            Promise: typeof Promise !== 'undefined',
            localStorage: typeof Storage !== 'undefined'
        };

        const unsupported = Object.entries(features)
            .filter(([, supported]) => !supported)
            .map(([feature]) => feature);

        if (unsupported.length > 0) {
            console.warn('Unsupported features:', unsupported.join(', '));
            console.warn('Please use a modern browser for the best experience.');
        }

        return unsupported.length === 0;
    }

    // ========== 事件处理 ==========

    /**
     * 页面加载完成处理
     */
    function handlePageLoad() {
        // 检查浏览器兼容性
        checkBrowserCompatibility();

        // 添加加载动画
        addLoadingAnimation();

        // 优化视频播放
        optimizeVideoPlayback();

        // 增强按钮交互
        enhanceButtonInteractions();

        // 添加提示信息
        addTooltips();

        // 预加载资源
        preloadResources();

        // 性能监控（仅开发环境）
        if (window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1') {
            monitorPerformance();
        }
    }

    // ========== 初始化 ==========

    // 页面加载完成后初始化
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', handlePageLoad);
    } else {
        handlePageLoad();
    }

    // 暴露公共API（可选，用于调试）
    if (typeof window !== 'undefined') {
        window.IndexApp = {
            elements,
            preloadResources,
            checkBrowserCompatibility
        };
    }

})();
