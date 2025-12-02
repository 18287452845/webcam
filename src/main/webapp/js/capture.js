/**
 * Capture Page - Camera Control and Face Detection
 * 摄像头控制和人脸检测功能（支持手动拍照和图片上传）
 * 
 * @version 2.1.0
 */

(function () {
  'use strict';

  // ========== DOM 元素引用 ==========
  const elements = {
    video: document.getElementById('video'),
    canvas: document.getElementById('canvas'),
    countdownDisplay: document.getElementById('countdown'),
    errorMessage: document.getElementById('error-message'),
    captureBtn: document.getElementById('captureBtn'),
    uploadBtn: document.getElementById('uploadBtn'),
    fileInput: document.getElementById('fileInput')
  };

  // ========== 状态管理 ==========
  const state = {
    stream: null,
    countdownTimer: null,
    countdown: 30,
    isProcessing: false,
    mode: 'manual', // 'auto' 或 'manual'
    selectedGender: null // 用户选择的性别
  };

  // ========== 配置 ==========
  const config = {
    cameraConstraints: {
      video: {
        width: { ideal: 1280 },
        height: { ideal: 720 },
        facingMode: 'user' // 前置摄像头
      },
      audio: false
    },
    countdownSeconds: 30,
    imageQuality: 0.85,
    redirectDelay: 3000,
    maxFileSize: 10 * 1024 * 1024 // 10MB
  };

  // ========== 工具函数 ==========

  /**
   * 显示错误消息
   * @param {string} message - 错误消息文本
   */
  function showError(message) {
    elements.errorMessage.textContent = message;
    elements.errorMessage.style.display = 'block';
    elements.errorMessage.classList.add('animate-bounce-in');
    elements.countdownDisplay.textContent = '错误';
  }

  /**
   * 显示摄像头错误并提示使用上传功能
   * @param {Error} error - 错误对象
   */
  function showCameraError(error) {
    const errorMsg = parseCameraError(error);
    const uploadMessage = '您也可以点击"上传图片"按钮选择本地照片进行识别';
    const fullMessage = `${errorMsg}。${uploadMessage}`;
    showError(fullMessage);
    updateCountdown('摄像头不可用');
  }

  /**
   * 隐藏错误消息
   */
  function hideError() {
    elements.errorMessage.style.display = 'none';
    elements.errorMessage.classList.remove('animate-bounce-in');
  }

  /**
   * 更新倒计时显示
   * @param {string} text - 显示文本
   */
  function updateCountdown(text) {
    elements.countdownDisplay.textContent = text;
  }

  /**
   * 获取URL参数
   * @param {string} name - 参数名
   * @returns {string|null} 参数值
   */
  function getUrlParameter(name) {
    const urlParams = new URLSearchParams(window.location.search);
    return urlParams.get(name);
  }

  /**
   * 初始化性别选择
   */
  function initializeGenderSelection() {
    const gender = getUrlParameter('gender');
    if (gender === 'male' || gender === 'female') {
      state.selectedGender = gender;
      const genderText = gender === 'male' ? '👨 男性' : '👩 女性';
      const indicator = document.getElementById('gender-indicator');
      const textElement = document.getElementById('gender-text');

      if (indicator && textElement) {
        textElement.textContent = `当前选择：${genderText}`;
        indicator.style.display = 'block';
      }
    }
  }

  /**
   * 启用/禁用按钮
   * @param {boolean} enabled - 是否启用
   */
  function setButtonsEnabled(enabled) {
    if (elements.captureBtn) {
      elements.captureBtn.disabled = !enabled;
    }
    if (elements.uploadBtn) {
      elements.uploadBtn.disabled = !enabled;
    }
  }

  /**
   * 解析相机错误
   * @param {Error} error - 错误对象
   * @returns {string} 用户友好的错误消息
   */
  function parseCameraError(error) {
    const errorMessages = {
      'NotAllowedError': '请允许访问摄像头权限',
      'PermissionDeniedError': '请允许访问摄像头权限',
      'NotFoundError': '未找到摄像头设备',
      'DevicesNotFoundError': '未找到摄像头设备',
      'NotReadableError': '摄像头被其他应用占用',
      'TrackStartError': '摄像头被其他应用占用',
      'OverconstrainedError': '摄像头不支持所需的分辨率'
    };

    return errorMessages[error.name] || '无法访问摄像头';
  }

  // ========== 核心功能 ==========

  /**
   * 启动摄像头
   */
  async function startCamera() {
    try {
      // 请求摄像头权限
      state.stream = await navigator.mediaDevices.getUserMedia(config.cameraConstraints);

      // 将视频流绑定到video元素
      elements.video.srcObject = state.stream;
      await elements.video.play();

      // 添加淡入动画
      elements.video.classList.add('animate-fade-in');

      hideError();

      // 手动模式：不启动倒计时，显示准备就绪
      if (state.mode === 'manual') {
        updateCountdown('准备就绪');
        setButtonsEnabled(true);
      } else {
        // 自动模式：启动倒计时
        startCountdown();
      }

    } catch (error) {
      console.error('Error accessing camera:', error);
      showCameraError(error);

      // 在摄像头不可用时，仅禁用拍照按钮，保持上传按钮可用
      if (elements.captureBtn) {
        elements.captureBtn.disabled = true;
      }
      // 上传按钮保持可用
      if (elements.uploadBtn) {
        elements.uploadBtn.disabled = false;
      }
    }
  }

  /**
   * 开始倒计时（自动模式）
   */
  function startCountdown() {
    state.countdown = config.countdownSeconds;
    updateCountdown(`检测倒计时 ${state.countdown}`);

    state.countdownTimer = setInterval(() => {
      state.countdown--;

      if (state.countdown > 0) {
        updateCountdown(`检测倒计时 ${state.countdown}`);
      } else if (state.countdown === 0) {
        updateCountdown('拍摄中...');
        clearInterval(state.countdownTimer);
        captureImage();
      }
    }, 1000);
  }

  /**
   * 捕获图像
   */
  function captureImage() {
    if (state.isProcessing) return;
    state.isProcessing = true;
    setButtonsEnabled(false);

    try {
      // 添加拍照闪光效果
      addPhotoFlash();

      // 设置canvas尺寸与video相同
      elements.canvas.width = elements.video.videoWidth;
      elements.canvas.height = elements.video.videoHeight;

      // 将video当前帧绘制到canvas
      const ctx = elements.canvas.getContext('2d');
      ctx.drawImage(elements.video, 0, 0, elements.canvas.width, elements.canvas.height);

      // 转换为base64 JPEG
      const imageData = elements.canvas.toDataURL('image/jpeg', config.imageQuality);

      // 发送到服务器
      sendImageToServer(imageData);

    } catch (error) {
      console.error('Error capturing image:', error);
      showError('捕获图像失败');
      state.isProcessing = false;
      setButtonsEnabled(true);
      setTimeout(() => redirectToHome(), config.redirectDelay);
    }
  }

  /**
   * 处理图片上传
   * @param {File} file - 上传的文件
   */
  async function handleFileUpload(file) {
    if (state.isProcessing) return;

    // 验证文件类型
    if (!file.type.startsWith('image/')) {
      showError('请选择图片文件');
      return;
    }

    // 验证文件大小
    if (file.size > config.maxFileSize) {
      showError('图片大小不能超过10MB');
      return;
    }

    state.isProcessing = true;
    setButtonsEnabled(false);
    updateCountdown('处理中...');

    try {
      // 读取文件为Base64
      const reader = new FileReader();

      reader.onload = (e) => {
        const imageData = e.target.result;

        // 显示图片预览到视频区域
        elements.video.style.display = 'none';
        const img = new Image();
        img.src = imageData;
        img.style.width = '100%';
        img.style.height = '100%';
        img.style.objectFit = 'cover';
        elements.video.parentNode.insertBefore(img, elements.video);

        // 添加闪光效果
        addPhotoFlash();

        // 发送到服务器
        sendImageToServer(imageData);
      };

      reader.onerror = () => {
        showError('读取文件失败');
        state.isProcessing = false;
        setButtonsEnabled(true);
      };

      reader.readAsDataURL(file);

    } catch (error) {
      console.error('Error uploading file:', error);
      showError('上传失败，请重试');
      state.isProcessing = false;
      setButtonsEnabled(true);
    }
  }

  /**
   * 添加拍照闪光效果
   */
  function addPhotoFlash() {
    const flash = document.createElement('div');
    flash.style.cssText = `
      position: fixed;
      top: 0;
      left: 0;
      width: 100vw;
      height: 100vh;
      background: white;
      z-index: 9999;
      pointer-events: none;
      animation: photoFlash 0.3s ease-out;
    `;
    document.body.appendChild(flash);

    setTimeout(() => flash.remove(), 300);
  }

  /**
   * 停止摄像头
   */
  function stopCamera() {
    if (state.stream) {
      state.stream.getTracks().forEach(track => track.stop());
      state.stream = null;
    }
    if (state.countdownTimer) {
      clearInterval(state.countdownTimer);
      state.countdownTimer = null;
    }
  }

  /**
   * 发送图像到服务器
   * @param {string} imageData - Base64编码的图像数据
   */
  async function sendImageToServer(imageData) {
    try {
      updateCountdown('识别中...');

      // 使用Fetch API发送数据
      const formData = new URLSearchParams();
      formData.append('image', imageData);

      // 如果用户选择了性别，发送给后端
      if (state.selectedGender) {
        formData.append('gender', state.selectedGender);
      }

      const response = await fetch('/webcam', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: formData
      });

      // 记录响应头中的元数据
      const requestId = response.headers.get('X-Request-Id');
      const processingTime = response.headers.get('X-Processing-Time');
      if (requestId) {
        console.log('Request ID:', requestId);
      }
      if (processingTime) {
        console.log('Server processing time:', processingTime, 'ms');
      }

      if (!response.ok) {
        // 尝试解析错误响应
        try {
          const errorResult = await response.json();
          if (errorResult.errorDetail || errorResult.msg) {
            throw new Error(errorResult.errorDetail || errorResult.msg);
          }
        } catch (e) {
          // 如果无法解析错误响应，使用默认错误消息
        }
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const result = await response.json();
      console.log('Server response:', result);

      handleServerResponse(result);

    } catch (error) {
      console.error('Error sending image to server:', error);
      showError('上传失败，请重试');
      updateCountdown('上传失败');

      state.isProcessing = false;
      setButtonsEnabled(true);

      setTimeout(() => redirectToHome(), config.redirectDelay);
    }
  }

  /**
   * 处理服务器响应
   * @param {Object} result - 服务器返回的结果
   */
  function handleServerResponse(result) {
    // 记录响应元数据（如果存在）
    if (result.requestId) {
      console.log('Request ID:', result.requestId);
    }
    if (result.processingTime) {
      console.log('Processing time:', result.processingTime, 'ms');
    }

    if (result.result === "1") {
      updateCountdown('识别完成！');

      // 停止摄像头
      stopCamera();

      // 跳转到结果页面
      const msgData = encodeURIComponent(JSON.stringify(result.msg));
      window.location.href = `result?msg=${msgData}`;
    } else {
      // 显示更详细的错误信息
      let errorMessage = '未检测到面部';
      if (result.errorDetail) {
        errorMessage = result.errorDetail;
      } else if (result.msg && typeof result.msg === 'string') {
        errorMessage = result.msg;
      } else if (result.msg && result.msg.error) {
        errorMessage = result.msg.error;
      }

      updateCountdown(errorMessage);
      showError(errorMessage);

      state.isProcessing = false;
      setButtonsEnabled(true);

      // 3秒后返回首页
      setTimeout(() => redirectToHome(), config.redirectDelay);
    }
  }

  /**
   * 重定向到首页
   */
  function redirectToHome() {
    stopCamera();
    window.location.href = 'index.html';
  }

  // ========== 事件处理 ==========

  /**
   * 页面加载完成处理
   */
  function handlePageLoad() {
    // 检查浏览器支持
    if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
      showError('您的浏览器不支持摄像头访问功能，请使用现代浏览器（Chrome、Firefox、Edge等）或点击"上传图片"按钮选择本地照片进行识别');
      // 仅禁用拍照按钮，保持上传按钮可用
      if (elements.captureBtn) {
        elements.captureBtn.disabled = true;
      }
      if (elements.uploadBtn) {
        elements.uploadBtn.disabled = false;
      }
      return;
    }

    // 添加页面加载动画
    document.body.classList.add('animate-fade-in');

    // 初始化性别选择
    initializeGenderSelection();

    // 启动摄像头
    startCamera();

    // 绑定手动拍照按钮事件
    if (elements.captureBtn) {
      elements.captureBtn.addEventListener('click', () => {
        if (!state.isProcessing) {
          updateCountdown('拍摄中...');
          captureImage();
        }
      });
    }

    // 绑定上传按钮事件
    if (elements.uploadBtn) {
      elements.uploadBtn.addEventListener('click', () => {
        elements.fileInput.click();
      });
    }

    // 绑定文件选择事件
    if (elements.fileInput) {
      elements.fileInput.addEventListener('change', (e) => {
        const file = e.target.files[0];
        if (file) {
          handleFileUpload(file);
        }
      });
    }
  }

  /**
   * 页面卸载处理
   */
  function handlePageUnload() {
    stopCamera();
  }

  // ========== 初始化 ==========

  // 页面加载完成后启动摄像头
  window.addEventListener('DOMContentLoaded', handlePageLoad);

  // 页面卸载时清理资源
  window.addEventListener('beforeunload', handlePageUnload);

  // 暴露公共API（可选，用于调试）
  if (typeof window !== 'undefined') {
    window.CaptureApp = {
      state,
      startCamera,
      stopCamera,
      captureImage,
      handleFileUpload
    };
  }

})();
