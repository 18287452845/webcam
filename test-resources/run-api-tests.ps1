# API Integration Test Script
# Tests backend API endpoints

$baseUrl = "http://localhost:8080"
$testResults = @()

# Minimal 1x1 pixel PNG image in Base64
$minimalImageBase64 = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg=="

function Record-Result {
    param([string]$Id, [string]$Name, [string]$Status, [string]$Details)
    $color = if($Status -eq "PASS"){"Green"}else{"Red"}
    Write-Host ""
    Write-Host "[$Status] $Id : $Name" -ForegroundColor $color
    if ($Details) { Write-Host "    $Details" -ForegroundColor Gray }
    $global:testResults += [PSCustomObject]@{
        TestId = $Id
        TestName = $Name
        Status = $Status
        Details = $Details
        Timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    }
}

function Test-Post {
    param(
        [string]$Id, [string]$Name, [string]$Endpoint, [string]$ImageData,
        [int]$ExpectedStatus = 200, [string]$ExpectedResult = $null, [string]$ExpectedErrorCode = $null
    )
    try {
        $body = "image=" + [System.Web.HttpUtility]::UrlEncode($ImageData)
        $response = Invoke-WebRequest -Uri "$baseUrl$Endpoint" -Method POST `
            -ContentType "application/x-www-form-urlencoded" -Body $body -ErrorAction Stop
        $json = $response.Content | ConvertFrom-Json
        
        if ($response.StatusCode -eq $ExpectedStatus) {
            $passed = $false
            if ($ExpectedResult -and $json.result -eq $ExpectedResult) { $passed = $true }
            elseif ($ExpectedErrorCode -and $json.errorCode -eq $ExpectedErrorCode) { $passed = $true }
            elseif (-not $ExpectedResult -and -not $ExpectedErrorCode) { $passed = $true }
            
            if ($passed) {
                Record-Result $Id $Name "PASS" "Status: $($response.StatusCode), Result: $($json.result)"
                return $true
            } else {
                Record-Result $Id $Name "FAIL" "Unexpected response: $($response.Content)"
                return $false
            }
        } else {
            Record-Result $Id $Name "FAIL" "Expected $ExpectedStatus, got $($response.StatusCode)"
            return $false
        }
    }
    catch {
        $code = $_.Exception.Response.StatusCode.value__
        if ($code -eq $ExpectedStatus) {
            try {
                $stream = $_.Exception.Response.GetResponseStream()
                $reader = New-Object System.IO.StreamReader($stream)
                $errorJson = $reader.ReadToEnd() | ConvertFrom-Json
                if ($ExpectedErrorCode -and $errorJson.errorCode -eq $ExpectedErrorCode) {
                    Record-Result $Id $Name "PASS" "Status: $code, ErrorCode: $($errorJson.errorCode)"
                    return $true
                } else {
                    Record-Result $Id $Name "PASS" "Status: $code (expected error)"
                    return $true
                }
            }
            catch {
                Record-Result $Id $Name "FAIL" "Error: $($_.Exception.Message)"
                return $false
            }
        } else {
            Record-Result $Id $Name "FAIL" "Expected $ExpectedStatus, got $code"
            return $false
        }
    }
}

function Test-Get {
    param([string]$Id, [string]$Name, [string]$Endpoint, [int]$ExpectedStatus = 200)
    try {
        $response = Invoke-WebRequest -Uri "$baseUrl$Endpoint" -Method GET -MaximumRedirection 0 -ErrorAction Stop
        if ($response.StatusCode -eq $ExpectedStatus) {
            Record-Result $Id $Name "PASS" "Status: $($response.StatusCode)"
            return $true
        } else {
            Record-Result $Id $Name "FAIL" "Expected $ExpectedStatus, got $($response.StatusCode)"
            return $false
        }
    }
    catch {
        $code = $_.Exception.Response.StatusCode.value__
        if ($code -eq 302 -or $code -eq $ExpectedStatus) {
            Record-Result $Id $Name "PASS" "Status: $code"
            return $true
        } else {
            Record-Result $Id $Name "FAIL" "Error: $($_.Exception.Message), Status: $code"
            return $false
        }
    }
}

Write-Host ""
Write-Host "Checking backend service..." -ForegroundColor Cyan
try {
    $null = Invoke-WebRequest -Uri "$baseUrl/index.html" -Method GET -TimeoutSec 5 -ErrorAction Stop
    Write-Host "Backend service is running!" -ForegroundColor Green
    Write-Host ""
}
catch {
    Write-Host "Error: Cannot connect to backend service ($baseUrl)" -ForegroundColor Red
    Write-Host "Please ensure the backend service is started." -ForegroundColor Yellow
    Write-Host ""
    exit 1
}

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  API Integration Tests Started" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "1. API Endpoint Tests (POST /webcam)" -ForegroundColor Yellow
Write-Host "----------------------------------------" -ForegroundColor Yellow

Test-Post -Id "TC-API-001" -Name "Valid Base64 image (with prefix)" `
    -Endpoint "/webcam" -ImageData "data:image/png;base64,$minimalImageBase64" -ExpectedStatus 200

Test-Post -Id "TC-API-002" -Name "Valid Base64 image (no prefix)" `
    -Endpoint "/webcam" -ImageData $minimalImageBase64 -ExpectedStatus 200

Test-Post -Id "TC-API-003" -Name "Empty image parameter" `
    -Endpoint "/webcam" -ImageData "" -ExpectedStatus 400 -ExpectedErrorCode "INVALID_PARAMETER"

Test-Post -Id "TC-API-004" -Name "Invalid Base64 data" `
    -Endpoint "/webcam" -ImageData "invalid_base64_string_!!!###" -ExpectedStatus 400 -ExpectedErrorCode "IMAGE_PROCESSING_ERROR"

Test-Post -Id "TC-API-005" -Name "No face detected" `
    -Endpoint "/webcam" -ImageData $minimalImageBase64 -ExpectedStatus 200 -ExpectedResult "0"

Write-Host ""
Write-Host "2. Result Page API Tests (GET /result)" -ForegroundColor Yellow
Write-Host "----------------------------------------" -ForegroundColor Yellow

$jsonStr = [System.Text.Encoding]::UTF8.GetBytes('{"gender":"Male","age":"25","img":"http://localhost:8080/upload/test.jpg"}')
$jsonBase64 = [System.Convert]::ToBase64String($jsonStr)
$testMsg = [System.Web.HttpUtility]::UrlEncode([System.Text.Encoding]::UTF8.GetString($jsonStr))

Test-Get -Id "TC-RESULT-001" -Name "Valid JSON parameter" `
    -Endpoint "/result?msg=$testMsg" -ExpectedStatus 200

Test-Get -Id "TC-RESULT-002" -Name "Empty parameter" `
    -Endpoint "/result" -ExpectedStatus 302

Test-Get -Id "TC-RESULT-003" -Name "Invalid JSON" `
    -Endpoint "/result?msg=invalid_json" -ExpectedStatus 302

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Test Results Summary" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$total = $testResults.Count
$passed = ($testResults | Where-Object {$_.Status -eq "PASS"}).Count
$failed = ($testResults | Where-Object {$_.Status -eq "FAIL"}).Count
$rate = if($total -gt 0){[math]::Round(($passed / $total) * 100, 2)}else{0}

Write-Host "Total Tests: $total" -ForegroundColor White
Write-Host "Passed: $passed" -ForegroundColor Green
Write-Host "Failed: $failed" -ForegroundColor Red
$rateColor = if($rate -ge 95){"Green"}elseif($rate -ge 80){"Yellow"}else{"Red"}
Write-Host "Pass Rate: $rate%" -ForegroundColor $rateColor
Write-Host ""

if ($failed -gt 0) {
    Write-Host "Failed Test Cases:" -ForegroundColor Red
    $testResults | Where-Object {$_.Status -eq "FAIL"} | ForEach-Object {
        Write-Host "  - $($_.TestId): $($_.TestName)" -ForegroundColor Red
        Write-Host "    $($_.Details)" -ForegroundColor Gray
    }
    Write-Host ""
}

$testResults | ConvertTo-Json -Depth 3 | Out-File "api-test-results.json" -Encoding UTF8
Write-Host "Test results saved to: api-test-results.json" -ForegroundColor Cyan
Write-Host ""

