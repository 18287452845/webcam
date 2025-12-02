# Test Face++ API Key via Backend Service
# This uses the backend service which already has Face++ API configured

Add-Type -AssemblyName System.Web

$baseUrl = "http://localhost:8080"
$testImagePath = ""

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Face++ API Key Test via Backend" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Check if backend is running
Write-Host "Checking if backend service is running..." -ForegroundColor Yellow
try {
    $null = Invoke-WebRequest -Uri "$baseUrl/index.html" -Method GET -TimeoutSec 5 -ErrorAction Stop
    Write-Host "Backend service is running!" -ForegroundColor Green
}
catch {
    Write-Host "Error: Backend service is not running at $baseUrl" -ForegroundColor Red
    Write-Host "Please start the backend service first." -ForegroundColor Yellow
    exit 1
}

Write-Host ""

# Find a test image
$uploadDir = "..\upload"
if (Test-Path $uploadDir) {
    $images = Get-ChildItem -Path $uploadDir -Filter "*.jpeg" | Select-Object -First 1
    if ($images) {
        $testImagePath = $images.FullName
        Write-Host "Using test image: $($images.Name)" -ForegroundColor Green
        Write-Host "Image size: $([math]::Round($images.Length / 1KB, 2)) KB" -ForegroundColor Gray
    }
}

if (-not $testImagePath) {
    Write-Host "No test image found. Creating a test image..." -ForegroundColor Yellow
    # Read an existing image or create one
    $minimalImageBase64 = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg=="
    $bytes = [Convert]::FromBase64String($minimalImageBase64)
    $tempPath = "$env:TEMP\facepp_test_$(Get-Date -Format 'yyyyMMddHHmmss').png"
    [System.IO.File]::WriteAllBytes($tempPath, $bytes)
    $testImagePath = $tempPath
    Write-Host "Created temporary test image" -ForegroundColor Green
}

Write-Host ""
Write-Host "Sending test image to backend API..." -ForegroundColor Cyan
Write-Host ""

try {
    # Read image and convert to base64
    $imageBytes = [System.IO.File]::ReadAllBytes($testImagePath)
    $base64Image = [Convert]::ToBase64String($imageBytes)
    
    # Prepare request
    $body = "image=" + [System.Web.HttpUtility]::UrlEncode("data:image/jpeg;base64,$base64Image")
    
    $startTime = Get-Date
    
    $response = Invoke-RestMethod -Uri "$baseUrl/webcam" `
        -Method POST `
        -ContentType "application/x-www-form-urlencoded" `
        -Body $body `
        -TimeoutSec 30 `
        -ErrorAction Stop
    
    $endTime = Get-Date
    $duration = ($endTime - $startTime).TotalMilliseconds
    
    Write-Host "========================================" -ForegroundColor Green
    Write-Host "  Face++ API Key Test Result" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Green
    Write-Host ""
    
    Write-Host "Request ID: $($response.requestId)" -ForegroundColor Cyan
    Write-Host "Processing Time: $($response.processingTime) ms" -ForegroundColor Cyan
    Write-Host "Total Request Time: $([math]::Round($duration, 2)) ms" -ForegroundColor Cyan
    Write-Host ""
    
    if ($response.result -eq "1") {
        Write-Host "Status: SUCCESS" -ForegroundColor Green
        Write-Host "Face++ API is working correctly!" -ForegroundColor Green
        Write-Host ""
        
        if ($response.msg) {
            Write-Host "Face Detection Results:" -ForegroundColor Cyan
            $msg = $response.msg
            
            if ($msg.gender) {
                Write-Host "  Gender: $($msg.gender)" -ForegroundColor White
            }
            if ($msg.age) {
                Write-Host "  Age: $($msg.age)" -ForegroundColor White
            }
            if ($msg.smile) {
                Write-Host "  Smile: $($msg.smile)" -ForegroundColor White
            }
            if ($msg.eyestatus) {
                Write-Host "  Eye Status: $($msg.eyestatus)" -ForegroundColor White
            }
            if ($msg.faceToken) {
                Write-Host "  Face Token: $($msg.faceToken)" -ForegroundColor White
            }
            if ($msg.img) {
                Write-Host "  Image URL: $($msg.img)" -ForegroundColor White
            }
        }
        
        Write-Host ""
        Write-Host "========================================" -ForegroundColor Green
        Write-Host "  CONCLUSION: Face++ API Key is VALID" -ForegroundColor Green
        Write-Host "========================================" -ForegroundColor Green
        
    }
    elseif ($response.result -eq "0") {
        Write-Host "Status: No face detected" -ForegroundColor Yellow
        Write-Host ""
        
        if ($response.errorCode) {
            if ($response.errorCode -eq "NO_FACE_DETECTED") {
                Write-Host "Face++ API responded successfully but no face was found in the image." -ForegroundColor Yellow
                Write-Host "This indicates the API key is VALID and working." -ForegroundColor Green
                Write-Host ""
                Write-Host "Error Detail: $($response.errorDetail)" -ForegroundColor Gray
            }
            else {
                Write-Host "Error Code: $($response.errorCode)" -ForegroundColor Red
                Write-Host "Error Detail: $($response.errorDetail)" -ForegroundColor Red
            }
        }
        else {
            Write-Host "No face detected in the test image." -ForegroundColor Yellow
            Write-Host "This is normal - the API key appears to be working." -ForegroundColor Green
        }
        
        Write-Host ""
        Write-Host "========================================" -ForegroundColor Yellow
        Write-Host "  CONCLUSION: Face++ API Key appears VALID" -ForegroundColor Yellow
        Write-Host "  (No face detected, but API responded)" -ForegroundColor Yellow
        Write-Host "========================================" -ForegroundColor Yellow
    }
    
    Write-Host ""
    Write-Host "Full Response:" -ForegroundColor Cyan
    $response | ConvertTo-Json -Depth 3 | Write-Host
    Write-Host ""
    
}
catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    $errorMessage = $_.Exception.Message
    
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Red
    Write-Host "  Face++ API Key Test FAILED!" -ForegroundColor Red
    Write-Host "========================================" -ForegroundColor Red
    Write-Host ""
    
    if ($statusCode) {
        Write-Host "HTTP Status Code: $statusCode" -ForegroundColor Red
        
        # Try to get error response
        try {
            $errorStream = $_.Exception.Response.GetResponseStream()
            $reader = New-Object System.IO.StreamReader($errorStream)
            $errorContent = $reader.ReadToEnd()
            
            Write-Host ""
            Write-Host "Error Response:" -ForegroundColor Red
            Write-Host $errorContent -ForegroundColor Yellow
            
            try {
                $errorJson = $errorContent | ConvertFrom-Json
                
                if ($errorJson.errorCode -eq "FACE_API_ERROR") {
                    Write-Host ""
                    Write-Host "DIAGNOSIS:" -ForegroundColor Red
                    Write-Host "The Face++ API call failed. This could mean:" -ForegroundColor Yellow
                    Write-Host "  1. API Key or Secret is invalid" -ForegroundColor Yellow
                    Write-Host "  2. API quota/rate limit exceeded" -ForegroundColor Yellow
                    Write-Host "  3. Network connectivity issue" -ForegroundColor Yellow
                    Write-Host ""
                    Write-Host "Error Detail: $($errorJson.errorDetail)" -ForegroundColor Red
                }
                elseif ($errorJson.errorCode -eq "IMAGE_PROCESSING_ERROR") {
                    Write-Host ""
                    Write-Host "DIAGNOSIS: Image processing error (not an API key issue)" -ForegroundColor Yellow
                }
                else {
                    Write-Host ""
                    Write-Host "Error Code: $($errorJson.errorCode)" -ForegroundColor Red
                    Write-Host "Error Detail: $($errorJson.errorDetail)" -ForegroundColor Red
                }
            }
            catch {
                # Not JSON
            }
        }
        catch {
            Write-Host "Could not read error response details" -ForegroundColor Yellow
        }
    }
    else {
        Write-Host "Error: $errorMessage" -ForegroundColor Red
        
        if ($errorMessage -like "*timeout*") {
            Write-Host ""
            Write-Host "DIAGNOSIS: Request timed out. Could be network or Face++ API issue." -ForegroundColor Yellow
        }
    }
    
    Write-Host ""
    Write-Host "Test Result: FAIL" -ForegroundColor Red
    
    exit 1
}

Write-Host ""

