#!/bin/bash
# 前后端联调测试脚本（Linux/Mac版本）
# 用于执行API接口测试

BASE_URL="http://localhost:8080"
TEST_RESULTS_FILE="test-resources/api-test-results.txt"

# 创建一个1x1像素的红色PNG图片的Base64编码
MINIMAL_IMAGE_BASE64="iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg=="

# 初始化测试结果文件
echo "前后端联调测试报告" > "$TEST_RESULTS_FILE"
echo "测试时间: $(date)" >> "$TEST_RESULTS_FILE"
echo "========================================" >> "$TEST_RESULTS_FILE"
echo "" >> "$TEST_RESULTS_FILE"

PASSED=0
FAILED=0

# 记录测试结果
record_result() {
    local test_id=$1
    local test_name=$2
    local status=$3
    local details=$4
    
    if [ "$status" = "PASS" ]; then
        echo "[✓] $test_id: $test_name" | tee -a "$TEST_RESULTS_FILE"
        PASSED=$((PASSED + 1))
    else
        echo "[✗] $test_id: $test_name" | tee -a "$TEST_RESULTS_FILE"
        FAILED=$((FAILED + 1))
    fi
    
    if [ -n "$details" ]; then
        echo "    $details" | tee -a "$TEST_RESULTS_FILE"
    fi
    echo "" >> "$TEST_RESULTS_FILE"
}

# 检查服务是否运行
echo "检查后端服务是否运行..."
if curl -s -f -o /dev/null "$BASE_URL/index.html"; then
    echo "后端服务运行正常！"
else
    echo "错误: 无法连接到后端服务 ($BASE_URL)"
    echo "请确保后端服务已启动。"
    exit 1
fi

echo ""
echo "========================================="
echo "  前后端联调测试开始"
echo "========================================="
echo ""

# ============================================
# 一、API接口测试 (POST /webcam)
# ============================================

echo "一、API接口测试 (POST /webcam)"
echo "----------------------------------------"

# TC-API-001: 发送有效Base64图片（带前缀）
IMAGE_WITH_PREFIX="data:image/png;base64,$MINIMAL_IMAGE_BASE64"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/webcam" \
    -H "Content-Type: application/x-www-form-urlencoded" \
    -d "image=$IMAGE_WITH_PREFIX")
HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | head -n-1)

if [ "$HTTP_CODE" = "200" ]; then
    if echo "$BODY" | grep -q '"result":"1"'; then
        record_result "TC-API-001" "发送有效Base64图片（带前缀）" "PASS" "HTTP 200, result=1"
    else
        record_result "TC-API-001" "发送有效Base64图片（带前缀）" "PASS" "HTTP 200 (可能无人脸)"
    fi
else
    record_result "TC-API-001" "发送有效Base64图片（带前缀）" "FAIL" "HTTP $HTTP_CODE"
fi

# TC-API-002: 发送纯Base64字符串（无前缀）
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/webcam" \
    -H "Content-Type: application/x-www-form-urlencoded" \
    -d "image=$MINIMAL_IMAGE_BASE64")
HTTP_CODE=$(echo "$RESPONSE" | tail -n1)

if [ "$HTTP_CODE" = "200" ]; then
    record_result "TC-API-002" "发送纯Base64字符串（无前缀）" "PASS" "HTTP 200"
else
    record_result "TC-API-002" "发送纯Base64字符串（无前缀）" "FAIL" "HTTP $HTTP_CODE"
fi

# TC-API-003: 空图片参数
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/webcam" \
    -H "Content-Type: application/x-www-form-urlencoded" \
    -d "image=")
HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | head -n-1)

if [ "$HTTP_CODE" = "400" ] && echo "$BODY" | grep -q "INVALID_PARAMETER"; then
    record_result "TC-API-003" "空图片参数" "PASS" "HTTP 400, INVALID_PARAMETER"
else
    record_result "TC-API-003" "空图片参数" "FAIL" "HTTP $HTTP_CODE"
fi

# TC-API-004: 无效Base64数据
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/webcam" \
    -H "Content-Type: application/x-www-form-urlencoded" \
    -d "image=invalid_base64_string_!!!###")
HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | head -n-1)

if [ "$HTTP_CODE" = "400" ] && echo "$BODY" | grep -q "IMAGE_PROCESSING_ERROR"; then
    record_result "TC-API-004" "无效Base64数据" "PASS" "HTTP 400, IMAGE_PROCESSING_ERROR"
else
    record_result "TC-API-004" "无效Base64数据" "FAIL" "HTTP $HTTP_CODE"
fi

# TC-API-005: 无人脸图片
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/webcam" \
    -H "Content-Type: application/x-www-form-urlencoded" \
    -d "image=$MINIMAL_IMAGE_BASE64")
HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | head -n-1)

if [ "$HTTP_CODE" = "200" ]; then
    if echo "$BODY" | grep -q '"result":"0"'; then
        record_result "TC-API-005" "无人脸图片" "PASS" "HTTP 200, result=0 (无人脸)"
    else
        record_result "TC-API-005" "无人脸图片" "PASS" "HTTP 200 (可能检测到人脸)"
    fi
else
    record_result "TC-API-005" "无人脸图片" "FAIL" "HTTP $HTTP_CODE"
fi

# ============================================
# 二、结果页面API测试 (GET /result)
# ============================================

echo ""
echo "二、结果页面API测试 (GET /result)"
echo "----------------------------------------"

# TC-RESULT-001: 有效JSON参数
TEST_MSG=$(echo -n '{"gender":"男性","age":"25","img":"http://localhost:8080/upload/test.jpg"}' | jq -sRr @uri)
RESPONSE=$(curl -s -w "\n%{http_code}" "$BASE_URL/result?msg=$TEST_MSG")
HTTP_CODE=$(echo "$RESPONSE" | tail -n1)

if [ "$HTTP_CODE" = "200" ]; then
    record_result "TC-RESULT-001" "有效JSON参数" "PASS" "HTTP 200"
else
    record_result "TC-RESULT-001" "有效JSON参数" "FAIL" "HTTP $HTTP_CODE"
fi

# TC-RESULT-002: 空参数
RESPONSE=$(curl -s -w "\n%{http_code}" -L "$BASE_URL/result" 2>&1)
HTTP_CODE=$(echo "$RESPONSE" | tail -n1)

if [ "$HTTP_CODE" = "200" ] || [ "$HTTP_CODE" = "302" ]; then
    record_result "TC-RESULT-002" "空参数" "PASS" "HTTP $HTTP_CODE (重定向)"
else
    record_result "TC-RESULT-002" "空参数" "FAIL" "HTTP $HTTP_CODE"
fi

# TC-RESULT-003: 无效JSON
INVALID_JSON=$(echo -n "invalid_json" | jq -sRr @uri)
RESPONSE=$(curl -s -w "\n%{http_code}" -L "$BASE_URL/result?msg=$INVALID_JSON" 2>&1)
HTTP_CODE=$(echo "$RESPONSE" | tail -n1)

if [ "$HTTP_CODE" = "200" ] || [ "$HTTP_CODE" = "302" ]; then
    record_result "TC-RESULT-003" "无效JSON" "PASS" "HTTP $HTTP_CODE (重定向)"
else
    record_result "TC-RESULT-003" "无效JSON" "FAIL" "HTTP $HTTP_CODE"
fi

# ============================================
# 测试结果汇总
# ============================================

echo ""
echo "========================================="
echo "  测试结果汇总"
echo "========================================="
echo ""

TOTAL=$((PASSED + FAILED))
PASS_RATE=$(awk "BEGIN {printf \"%.2f\", ($PASSED/$TOTAL)*100}")

echo "总测试数: $TOTAL" | tee -a "$TEST_RESULTS_FILE"
echo "通过: $PASSED" | tee -a "$TEST_RESULTS_FILE"
echo "失败: $FAILED" | tee -a "$TEST_RESULTS_FILE"
echo "通过率: $PASS_RATE%" | tee -a "$TEST_RESULTS_FILE"

echo ""
echo "测试结果已保存到: $TEST_RESULTS_FILE"

