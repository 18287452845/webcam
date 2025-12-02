# Face++ API Key Validation Test Script
# Tests if the Face++ API key and secret are valid

Add-Type -AssemblyName System.Web

$apiUrl = "https://api-cn.faceplusplus.com/facepp/v3/detect"
$apiKey = "s1ArCGvj1T91BFbkaDoP3c279i78Vebk"
$apiSecret = "VL4rQADCJnoflL6D2LAjHdcr1FrangF5"

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Face++ API Key Validation Test" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "API URL: $apiUrl" -ForegroundColor Yellow
Write-Host "API Key: $($apiKey.Substring(0, 10))..." -ForegroundColor Yellow
Write-Host ""

# Check if we have a test image in upload directory
$testImagePath = ""
$uploadDir = "..\upload"
if (Test-Path $uploadDir) {
    $images = Get-ChildItem -Path $uploadDir -Filter "*.jpeg" | Select-Object -First 1
    if ($images) {
        $testImagePath = $images.FullName
        Write-Host "Found test image: $($images.Name)" -ForegroundColor Green
    }
}

if (-not $testImagePath) {
    Write-Host "No test image found in upload directory." -ForegroundColor Yellow
    Write-Host "Creating a minimal test image..." -ForegroundColor Yellow
    
    # Create a simple 1x1 pixel PNG as base64 and save it
    $minimalImageBase64 = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg=="
    $bytes = [Convert]::FromBase64String($minimalImageBase64)
    $tempPath = "$env:TEMP\facepp_test.png"
    [System.IO.File]::WriteAllBytes($tempPath, $bytes)
    $testImagePath = $tempPath
    Write-Host "Created temporary test image: $tempPath" -ForegroundColor Green
}

Write-Host ""
Write-Host "Testing Face++ API connection..." -ForegroundColor Cyan
Write-Host ""

try {
    # Use multipart/form-data with proper encoding
    $boundary = [System.Guid]::NewGuid().ToString()
    $fileBytes = [System.IO.File]::ReadAllBytes($testImagePath)
    $fileName = Split-Path -Leaf $testImagePath
    
    # Build multipart form data
    $newline = "`r`n"
    $bodyParts = New-Object System.Collections.ArrayList
    
    # Add api_key
    $bodyParts.Add("--$boundary" + $newline) | Out-Null
    $bodyParts.Add("Content-Disposition: form-data; name=`"api_key`"" + $newline) | Out-Null
    $bodyParts.Add($newline) | Out-Null
    $bodyParts.Add($apiKey + $newline) | Out-Null
    
    # Add api_secret
    $bodyParts.Add("--$boundary" + $newline) | Out-Null
    $bodyParts.Add("Content-Disposition: form-data; name=`"api_secret`"" + $newline) | Out-Null
    $bodyParts.Add($newline) | Out-Null
    $bodyParts.Add($apiSecret + $newline) | Out-Null
    
    # Add image_file
    $bodyParts.Add("--$boundary" + $newline) | Out-Null
    $bodyParts.Add("Content-Disposition: form-data; name=`"image_file`"; filename=`"$fileName`"" + $newline) | Out-Null
    $bodyParts.Add("Content-Type: application/octet-stream" + $newline) | Out-Null
    $bodyParts.Add($newline) | Out-Null
    
    # Convert string parts to bytes
    $bodyBytes = New-Object System.Collections.ArrayList
    foreach ($part in $bodyParts) {
        $bytes = [System.Text.Encoding]::UTF8.GetBytes($part)
        foreach ($b in $bytes) {
            $bodyBytes.Add($b) | Out-Null
        }
    }
    
    # Add file bytes
    foreach ($b in $fileBytes) {
        $bodyBytes.Add($b) | Out-Null
    }
    
    # Add closing boundary
    $closing = $newline + "--$boundary--" + $newline
    $closingBytes = [System.Text.Encoding]::UTF8.GetBytes($closing)
    foreach ($b in $closingBytes) {
        $bodyBytes.Add($b) | Out-Null
    }
    
    # Convert ArrayList to byte array
    $finalBody = New-Object byte[] $bodyBytes.Count
    for ($i = 0; $i -lt $bodyBytes.Count; $i++) {
        $finalBody[$i] = $bodyBytes[$i]
    }
    
    $headers = @{
        "Content-Type" = "multipart/form-data; boundary=$boundary"
    }
    
    Write-Host "Sending request to Face++ API..." -ForegroundColor Yellow
    Write-Host "Request size: $($finalBody.Length) bytes" -ForegroundColor Gray
    
    $response = Invoke-RestMethod -Uri $apiUrl `
        -Method POST `
        -Headers $headers `
        -Body $finalBody `
        -TimeoutSec 30 `
        -ErrorAction Stop
    
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Green
    Write-Host "  API Key is VALID!" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Green
    Write-Host ""
    
    # Parse response
    if ($response.faces -and $response.faces.Count -gt 0) {
        Write-Host "Response: Success" -ForegroundColor Green
        Write-Host "Faces detected: $($response.faces.Count)" -ForegroundColor Green
        $firstFace = $response.faces[0]
        if ($firstFace.attributes) {
            Write-Host ""
            Write-Host "Face Attributes:" -ForegroundColor Cyan
            if ($firstFace.attributes.gender) {
                Write-Host "  Gender: $($firstFace.attributes.gender.value)" -ForegroundColor White
            }
            if ($firstFace.attributes.age) {
                Write-Host "  Age: $($firstFace.attributes.age.value)" -ForegroundColor White
            }
        }
    } else {
        Write-Host "Response: Success (No faces detected in test image)" -ForegroundColor Yellow
        Write-Host "This is normal if using a minimal test image." -ForegroundColor Yellow
    }
    
    Write-Host ""
    Write-Host "API Response Details:" -ForegroundColor Cyan
    $response | ConvertTo-Json -Depth 3 | Write-Host
    
    Write-Host ""
    Write-Host "Test Result: PASS" -ForegroundColor Green
    Write-Host "Your Face++ API credentials are valid and working!" -ForegroundColor Green
    
}
catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    $errorMessage = $_.Exception.Message
    
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Red
    Write-Host "  API Key Test FAILED!" -ForegroundColor Red
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
            
            # Parse JSON error if possible
            try {
                $errorJson = $errorContent | ConvertFrom-Json
                if ($errorJson.error_message) {
                    Write-Host ""
                    Write-Host "Error Message: $($errorJson.error_message)" -ForegroundColor Red
                    
                    # Common error messages
                    if ($errorJson.error_message -like "*AUTHORIZATION*" -or $errorJson.error_message -like "*AUTH*" -or $errorJson.error_message -like "*MISSING_ARGUMENTS*") {
                        Write-Host ""
                        Write-Host "DIAGNOSIS: API Key or Secret might be invalid, or request format issue!" -ForegroundColor Red
                        Write-Host "Please check your API credentials in application.properties" -ForegroundColor Yellow
                    }
                    elseif ($errorJson.error_message -like "*CONCURRENCY*" -or $errorJson.error_message -like "*QUOTA*") {
                        Write-Host ""
                        Write-Host "DIAGNOSIS: API quota exceeded or rate limit reached." -ForegroundColor Yellow
                    }
                    elseif ($errorJson.error_message -like "*INVALID_IMAGE*") {
                        Write-Host ""
                        Write-Host "DIAGNOSIS: Test image format issue (but API credentials are valid)" -ForegroundColor Yellow
                    }
                }
            }
            catch {
                # Not JSON, ignore
            }
        }
        catch {
            Write-Host "Could not read error response" -ForegroundColor Yellow
        }
    } else {
        Write-Host "Error: $errorMessage" -ForegroundColor Red
        
        if ($errorMessage -like "*timeout*" -or $errorMessage -like "*timed out*") {
            Write-Host ""
            Write-Host "DIAGNOSIS: Request timed out. This could be a network issue." -ForegroundColor Yellow
        }
        elseif ($errorMessage -like "*SSL*" -or $errorMessage -like "*certificate*") {
            Write-Host ""
            Write-Host "DIAGNOSIS: SSL/TLS certificate issue." -ForegroundColor Yellow
        }
    }
    
    Write-Host ""
    Write-Host "Test Result: FAIL" -ForegroundColor Red
    
    exit 1
}

Write-Host ""
