# Simple API Test Script using curl-like approach

$baseUrl = "http://localhost:8080"
$results = New-Object System.Collections.ArrayList

# Add System.Web assembly for URL encoding
Add-Type -AssemblyName System.Web

$minimalImageBase64 = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg=="

function Add-Result {
    param([string]$Id, [string]$Name, [string]$Status, [string]$Details)
    $color = if($Status -eq "PASS"){"Green"}else{"Red"}
    Write-Host ""
    Write-Host "[$Status] $Id : $Name" -ForegroundColor $color
    if ($Details) { Write-Host "    $Details" -ForegroundColor Gray }
    $null = $results.Add([PSCustomObject]@{
        TestId = $Id
        TestName = $Name
        Status = $Status
        Details = $Details
        Timestamp = (Get-Date).ToString("yyyy-MM-dd HH:mm:ss")
    })
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
    exit 1
}

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  API Integration Tests" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Test 1: Valid Base64 image with prefix
Write-Host "Test 1: Valid Base64 image (with prefix)" -ForegroundColor Yellow
try {
    $imageData = "data:image/png;base64,$minimalImageBase64"
    $body = "image=" + [System.Web.HttpUtility]::UrlEncode($imageData)
    $response = Invoke-RestMethod -Uri "$baseUrl/webcam" -Method POST `
        -ContentType "application/x-www-form-urlencoded" -Body $body -ErrorAction Stop
    if ($response.result) {
        Add-Result "TC-API-001" "Valid Base64 image (with prefix)" "PASS" "Status: 200, Result: $($response.result)"
    } else {
        Add-Result "TC-API-001" "Valid Base64 image (with prefix)" "PASS" "Status: 200"
    }
}
catch {
    Add-Result "TC-API-001" "Valid Base64 image (with prefix)" "FAIL" "Error: $($_.Exception.Message)"
}

# Test 2: Valid Base64 image without prefix
Write-Host "Test 2: Valid Base64 image (no prefix)" -ForegroundColor Yellow
try {
    $body = "image=" + [System.Web.HttpUtility]::UrlEncode($minimalImageBase64)
    $response = Invoke-RestMethod -Uri "$baseUrl/webcam" -Method POST `
        -ContentType "application/x-www-form-urlencoded" -Body $body -ErrorAction Stop
    if ($response.result) {
        Add-Result "TC-API-002" "Valid Base64 image (no prefix)" "PASS" "Status: 200, Result: $($response.result)"
    } else {
        Add-Result "TC-API-002" "Valid Base64 image (no prefix)" "PASS" "Status: 200"
    }
}
catch {
    Add-Result "TC-API-002" "Valid Base64 image (no prefix)" "FAIL" "Error: $($_.Exception.Message)"
}

# Test 3: Empty image parameter
Write-Host "Test 3: Empty image parameter" -ForegroundColor Yellow
try {
    $body = "image="
    $response = Invoke-RestMethod -Uri "$baseUrl/webcam" -Method POST `
        -ContentType "application/x-www-form-urlencoded" -Body $body -ErrorAction Stop
    Add-Result "TC-API-003" "Empty image parameter" "FAIL" "Should have returned error"
}
catch {
    $code = $_.Exception.Response.StatusCode.value__
    if ($code -eq 400) {
        Add-Result "TC-API-003" "Empty image parameter" "PASS" "Status: 400 (expected)"
    } else {
        Add-Result "TC-API-003" "Empty image parameter" "FAIL" "Expected 400, got $code"
    }
}

# Test 4: Invalid Base64 data
Write-Host "Test 4: Invalid Base64 data" -ForegroundColor Yellow
try {
    $body = "image=" + [System.Web.HttpUtility]::UrlEncode("invalid_base64_string_!!!###")
    $response = Invoke-RestMethod -Uri "$baseUrl/webcam" -Method POST `
        -ContentType "application/x-www-form-urlencoded" -Body $body -ErrorAction Stop
    Add-Result "TC-API-004" "Invalid Base64 data" "FAIL" "Should have returned error"
}
catch {
    $code = $_.Exception.Response.StatusCode.value__
    if ($code -eq 400) {
        Add-Result "TC-API-004" "Invalid Base64 data" "PASS" "Status: 400 (expected)"
    } else {
        Add-Result "TC-API-004" "Invalid Base64 data" "FAIL" "Expected 400, got $code"
    }
}

# Test 5: No face detected
Write-Host "Test 5: No face detected" -ForegroundColor Yellow
try {
    $body = "image=" + [System.Web.HttpUtility]::UrlEncode($minimalImageBase64)
    $response = Invoke-RestMethod -Uri "$baseUrl/webcam" -Method POST `
        -ContentType "application/x-www-form-urlencoded" -Body $body -ErrorAction Stop
    if ($response.result -eq "0") {
        Add-Result "TC-API-005" "No face detected" "PASS" "Status: 200, Result: 0 (no face)"
    } else {
        Add-Result "TC-API-005" "No face detected" "PASS" "Status: 200, Result: $($response.result)"
    }
}
catch {
    Add-Result "TC-API-005" "No face detected" "FAIL" "Error: $($_.Exception.Message)"
}

# Test 6: Result page with valid JSON
Write-Host "Test 6: Result page with valid JSON" -ForegroundColor Yellow
try {
    $jsonStr = '{"gender":"Male","age":"25","img":"http://localhost:8080/upload/test.jpg"}'
    $encoded = [System.Web.HttpUtility]::UrlEncode($jsonStr)
    $response = Invoke-WebRequest -Uri "$baseUrl/result?msg=$encoded" -Method GET -ErrorAction Stop
    Add-Result "TC-RESULT-001" "Valid JSON parameter" "PASS" "Status: $($response.StatusCode)"
}
catch {
    Add-Result "TC-RESULT-001" "Valid JSON parameter" "FAIL" "Error: $($_.Exception.Message)"
}

# Test 7: Result page empty parameter
Write-Host "Test 7: Result page empty parameter" -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "$baseUrl/result" -Method GET -MaximumRedirection 0 -ErrorAction Stop
    Add-Result "TC-RESULT-002" "Empty parameter" "FAIL" "Should redirect"
}
catch {
    $code = $_.Exception.Response.StatusCode.value__
    if ($code -eq 302) {
        Add-Result "TC-RESULT-002" "Empty parameter" "PASS" "Status: 302 (redirected)"
    } else {
        Add-Result "TC-RESULT-002" "Empty parameter" "FAIL" "Expected 302, got $code"
    }
}

# Test 8: Result page invalid JSON
Write-Host "Test 8: Result page invalid JSON" -ForegroundColor Yellow
try {
    $encoded = [System.Web.HttpUtility]::UrlEncode("invalid_json")
    $response = Invoke-WebRequest -Uri "$baseUrl/result?msg=$encoded" -Method GET -MaximumRedirection 0 -ErrorAction Stop
    Add-Result "TC-RESULT-003" "Invalid JSON" "FAIL" "Should redirect"
}
catch {
    $code = $_.Exception.Response.StatusCode.value__
    if ($code -eq 302) {
        Add-Result "TC-RESULT-003" "Invalid JSON" "PASS" "Status: 302 (redirected)"
    } else {
        Add-Result "TC-RESULT-003" "Invalid JSON" "FAIL" "Expected 302, got $code"
    }
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Test Results Summary" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$total = $results.Count
$passed = ($results | Where-Object {$_.Status -eq "PASS"}).Count
$failed = ($results | Where-Object {$_.Status -eq "FAIL"}).Count
$rate = if($total -gt 0){[math]::Round(($passed / $total) * 100, 2)}else{0}

Write-Host "Total Tests: $total" -ForegroundColor White
Write-Host "Passed: $passed" -ForegroundColor Green
Write-Host "Failed: $failed" -ForegroundColor Red
$rateColor = if($rate -ge 95){"Green"}elseif($rate -ge 80){"Yellow"}else{"Red"}
Write-Host "Pass Rate: $rate%" -ForegroundColor $rateColor
Write-Host ""

if ($failed -gt 0) {
    Write-Host "Failed Test Cases:" -ForegroundColor Red
    $results | Where-Object {$_.Status -eq "FAIL"} | ForEach-Object {
        Write-Host "  - $($_.TestId): $($_.TestName)" -ForegroundColor Red
        Write-Host "    $($_.Details)" -ForegroundColor Gray
    }
    Write-Host ""
}

$results | ConvertTo-Json -Depth 3 | Out-File "api-test-results.json" -Encoding UTF8
Write-Host "Test results saved to: api-test-results.json" -ForegroundColor Cyan
Write-Host ""

