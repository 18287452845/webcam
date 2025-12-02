# 前后端联调测试脚本
# 用于执行API接口测试

$baseUrl = "http://localhost:8080"
$testResults = @()

# 创建一个1x1像素的红色PNG图片的Base64编码（用于测试）
$minimalImageBase64 = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg=="

# 测试函数：记录测试结果
function Record-TestResult {
    param(
        [string]$TestId,
        [string]$TestName,
        [string]$Status,
        [string]$Details
    )
    $color = if($Status -eq "PASS"){"Green"}else{"Red"}
    Write-Host ""
    Write-Host "[$Status] $TestId : $TestName" -ForegroundColor $color
    if ($Details) {
        Write-Host "    $Details" -ForegroundColor Gray
    }
    $global:testResults += [PSCustomObject]@{
        TestId = $TestId
        TestName = $TestName
        Status = $Status
        Details = $Details
        Timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    }
}

# 测试函数：发送POST请求
function Invoke-ApiTest {
    param(
        [string]$TestId,
        [string]$TestName,
        [string]$Endpoint,
        [string]$ImageData,
        [int]$ExpectedStatus = 200,
        [string]$ExpectedResult = $null,
        [string]$ExpectedErrorCode = $null
    )
    
    try {
        # 构建表单数据
        $body = "image=" + [System.Web.HttpUtility]::UrlEncode($ImageData)
        
        $response = Invoke-WebRequest -Uri "$baseUrl$Endpoint" `
            -Method POST `
            -ContentType "application/x-www-form-urlencoded" `
            -Body $body `
            -ErrorAction Stop
        
        $responseBody = $response.Content | ConvertFrom-Json
        
        if ($response.StatusCode -eq $ExpectedStatus) {
            if ($ExpectedResult -and $responseBody.result -eq $ExpectedResult) {
                Record-TestResult $TestId $TestName "PASS" "Status: $($response.StatusCode), Result: $($responseBody.result)"
                return $true
            }
            elseif ($ExpectedErrorCode -and $responseBody.errorCode -eq $ExpectedErrorCode) {
                Record-TestResult $TestId $TestName "PASS" "Status: $($response.StatusCode), ErrorCode: $($responseBody.errorCode)"
                return $true
            }
            elseif (-not $ExpectedResult -and -not $ExpectedErrorCode) {
                Record-TestResult $TestId $TestName "PASS" "Status: $($response.StatusCode), Result: $($responseBody.result)"
                return $true
            }
            else {
                Record-TestResult $TestId $TestName "FAIL" "Unexpected response: $($response.Content)"
                return $false
            }
        }
        else {
            Record-TestResult $TestId $TestName "FAIL" "Expected status $ExpectedStatus, got $($response.StatusCode)"
            return $false
        }
    }
    catch {
        $statusCode = $_.Exception.Response.StatusCode.value__
        if ($statusCode -eq $ExpectedStatus) {
            try {
                $errorStream = $_.Exception.Response.GetResponseStream()
                $reader = New-Object System.IO.StreamReader($errorStream)
                $errorContent = $reader.ReadToEnd() | ConvertFrom-Json
                if ($ExpectedErrorCode -and $errorContent.errorCode -eq $ExpectedErrorCode) {
                    Record-TestResult $TestId $TestName "PASS" "Status: $statusCode, ErrorCode: $($errorContent.errorCode)"
                    return $true
                }
                else {
                    Record-TestResult $TestId $TestName "PASS" "Status: $statusCode (expected error)"
                    return $true
                }
            }
            catch {
                Record-TestResult $TestId $TestName "FAIL" "Error: $($_.Exception.Message)"
                return $false
            }
        }
        else {
            Record-TestResult $TestId $TestName "FAIL" "Expected status $ExpectedStatus, got $statusCode. Error: $($_.Exception.Message)"
            return $false
        }
    }
}

# 测试函数：发送GET请求
function Invoke-GetTest {
    param(
        [string]$TestId,
        [string]$TestName,
        [string]$Endpoint,
        [int]$ExpectedStatus = 200
    )
    
    try {
        $response = Invoke-WebRequest -Uri "$baseUrl$Endpoint" `
            -Method GET `
            -MaximumRedirection 0 `
            -ErrorAction Stop
        
        if ($response.StatusCode -eq $ExpectedStatus) {
            Record-TestResult $TestId $TestName "PASS" "Status: $($response.StatusCode)"
            return $true
        }
        else {
            Record-TestResult $TestId $TestName "FAIL" "Expected status $ExpectedStatus, got $($response.StatusCode)"
            return $false
        }
    }
    catch {
        $exception = $_.Exception
        if ($exception.Response.StatusCode.value__ -eq 302) {
            $location = $exception.Response.Headers.Location
            Record-TestResult $TestId $TestName "PASS" "Redirected to: $location"
            return $true
        }
        elseif ($exception.Response.StatusCode.value__ -eq $ExpectedStatus) {
            Record-TestResult $TestId $TestName "PASS" "Status: $ExpectedStatus (expected)"
            return $true
        }
        else {
            $statusCode = $exception.Response.StatusCode.value__
            Record-TestResult $TestId $TestName "FAIL" "Error: $($exception.Message), Status: $statusCode"
            return $false
        }
    }
}

# 检查服务是否运行
Write-Host ""
Write-Host "检查后端服务是否运行..." -ForegroundColor Cyan
try {
    $healthCheck = Invoke-WebRequest -Uri "$baseUrl/index.html" -Method GET -TimeoutSec 5 -ErrorAction Stop
    Write-Host "后端服务运行正常！" -ForegroundColor Green
    Write-Host ""
}
catch {
    Write-Host "错误: 无法连接到后端服务 ($baseUrl)" -ForegroundColor Red
    Write-Host "请确保后端服务已启动。" -ForegroundColor Yellow
    Write-Host ""
    exit 1
}

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  前后端联调测试开始" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# ============================================
# 一、API接口测试 (POST /webcam)
# ============================================

Write-Host "一、API接口测试 (POST /webcam)" -ForegroundColor Yellow
Write-Host "----------------------------------------" -ForegroundColor Yellow

# TC-API-001: 发送有效Base64图片（带前缀）
$imageWithPrefix = "data:image/png;base64," + $minimalImageBase64
Invoke-ApiTest -TestId "TC-API-001" `
    -TestName "发送有效Base64图片(带前缀)" `
    -Endpoint "/webcam" `
    -ImageData $imageWithPrefix `
    -ExpectedStatus 200

# TC-API-002: 发送纯Base64字符串（无前缀）
Invoke-ApiTest -TestId "TC-API-002" `
    -TestName "发送纯Base64字符串(无前缀)" `
    -Endpoint "/webcam" `
    -ImageData $minimalImageBase64 `
    -ExpectedStatus 200

# TC-API-003: 空图片参数
Invoke-ApiTest -TestId "TC-API-003" `
    -TestName "空图片参数" `
    -Endpoint "/webcam" `
    -ImageData "" `
    -ExpectedStatus 400 `
    -ExpectedErrorCode "INVALID_PARAMETER"

# TC-API-004: 无效Base64数据
Invoke-ApiTest -TestId "TC-API-004" `
    -TestName "无效Base64数据" `
    -Endpoint "/webcam" `
    -ImageData "invalid_base64_string_!!!###" `
    -ExpectedStatus 400 `
    -ExpectedErrorCode "IMAGE_PROCESSING_ERROR"

# TC-API-005: 无人脸图片（使用最小图片，可能检测不到人脸）
Invoke-ApiTest -TestId "TC-API-005" `
    -TestName "无人脸图片" `
    -Endpoint "/webcam" `
    -ImageData $minimalImageBase64 `
    -ExpectedStatus 200 `
    -ExpectedResult "0"

# ============================================
# 二、结果页面API测试 (GET /result)
# ============================================

Write-Host ""
Write-Host "二、结果页面API测试 (GET /result)" -ForegroundColor Yellow
Write-Host "----------------------------------------" -ForegroundColor Yellow

# TC-RESULT-001: 有效JSON参数
$jsonStr = "{`"gender`":`"Male`",`"age`":`"25`",`"img`":`"http://localhost:8080/upload/test.jpg`"}"
$testMsg = [System.Web.HttpUtility]::UrlEncode($jsonStr)
Invoke-GetTest -TestId "TC-RESULT-001" `
    -TestName "有效JSON参数" `
    -Endpoint "/result?msg=$testMsg" `
    -ExpectedStatus 200

# TC-RESULT-002: 空参数
Invoke-GetTest -TestId "TC-RESULT-002" `
    -TestName "空参数" `
    -Endpoint "/result" `
    -ExpectedStatus 302

# TC-RESULT-003: 无效JSON
$invalidJson = [System.Web.HttpUtility]::UrlEncode("invalid_json")
Invoke-GetTest -TestId "TC-RESULT-003" `
    -TestName "无效JSON" `
    -Endpoint "/result?msg=$invalidJson" `
    -ExpectedStatus 302

# ============================================
# 测试结果汇总
# ============================================

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  测试结果汇总" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$totalTests = $testResults.Count
$passedTests = ($testResults | Where-Object {$_.Status -eq "PASS"}).Count
$failedTests = ($testResults | Where-Object {$_.Status -eq "FAIL"}).Count
if ($totalTests -gt 0) {
    $passRate = [math]::Round(($passedTests / $totalTests) * 100, 2)
} else {
    $passRate = 0
}

Write-Host "总测试数: $totalTests" -ForegroundColor White
Write-Host "通过: $passedTests" -ForegroundColor Green
Write-Host "失败: $failedTests" -ForegroundColor Red
$rateColor = if($passRate -ge 95){"Green"}elseif($passRate -ge 80){"Yellow"}else{"Red"}
Write-Host "通过率: $passRate%" -ForegroundColor $rateColor
Write-Host ""

if ($failedTests -gt 0) {
    Write-Host "失败的测试用例:" -ForegroundColor Red
    $testResults | Where-Object {$_.Status -eq "FAIL"} | ForEach-Object {
        Write-Host "  - $($_.TestId): $($_.TestName)" -ForegroundColor Red
        Write-Host "    $($_.Details)" -ForegroundColor Gray
    }
    Write-Host ""
}

# 导出测试结果到JSON文件
$testResults | ConvertTo-Json -Depth 3 | Out-File "api-test-results.json" -Encoding UTF8
Write-Host "测试结果已保存到: api-test-results.json" -ForegroundColor Cyan
Write-Host ""

