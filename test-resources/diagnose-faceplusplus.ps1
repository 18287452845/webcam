# Detailed Face++ API Diagnosis Script
# Tests Face++ API with detailed error information

Add-Type -AssemblyName System.Web
[System.Net.ServicePointManager]::SecurityProtocol = [System.Net.SecurityProtocolType]::Tls12

$apiUrl = "https://api-cn.faceplusplus.com/facepp/v3/detect"
$apiKey = "s1ArCGvj1T91BFbkaDoP3c279i78Vebk"
$apiSecret = "VL4rQADCJnoflL6D2LAjHdcr1FrangF5"

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Face++ API Detailed Diagnosis" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "API URL: $apiUrl" -ForegroundColor Yellow
Write-Host "API Key: $($apiKey.Substring(0, 10))..." -ForegroundColor Yellow
Write-Host "API Secret: $($apiSecret.Substring(0, 10))..." -ForegroundColor Yellow
Write-Host ""

# Create a simple test image using a URL (Face++ supports image URLs)
Write-Host "Testing with a publicly accessible test image URL..." -ForegroundColor Cyan
Write-Host ""

# Use a simple test - try with image_base64 parameter instead
try {
    # Create a minimal valid image (slightly larger than 100 chars)
    $testImageBase64 = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg==" + ("A" * 50)
    
    Write-Host "Attempting test with image_base64 parameter..." -ForegroundColor Yellow
    
    $bodyParams = @{
        api_key = $apiKey
        api_secret = $apiSecret
        image_base64 = $testImageBase64
        return_attributes = "gender,age"
    }
    
    $body = ($bodyParams.GetEnumerator() | ForEach-Object { 
        "$([System.Web.HttpUtility]::UrlEncode($_.Key))=$([System.Web.HttpUtility]::UrlEncode($_.Value))" 
    }) -join "&"
    
    $response = Invoke-RestMethod -Uri $apiUrl `
        -Method POST `
        -ContentType "application/x-www-form-urlencoded" `
        -Body $body `
        -TimeoutSec 30 `
        -ErrorAction Stop
    
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Green
    Write-Host "  SUCCESS: API Key is VALID!" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Green
    Write-Host ""
    Write-Host "Response:" -ForegroundColor Cyan
    $response | ConvertTo-Json -Depth 3 | Write-Host
    
    Write-Host ""
    Write-Host "CONCLUSION: Your Face++ API credentials are VALID and working!" -ForegroundColor Green
    
}
catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    $errorMessage = $_.Exception.Message
    
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Red
    Write-Host "  Test Result" -ForegroundColor Red
    Write-Host "========================================" -ForegroundColor Red
    Write-Host ""
    
    if ($statusCode) {
        Write-Host "HTTP Status Code: $statusCode" -ForegroundColor $(if($statusCode -eq 200){"Green"}else{"Red"})
        
        try {
            $errorStream = $_.Exception.Response.GetResponseStream()
            $reader = New-Object System.IO.StreamReader($errorStream)
            $errorContent = $reader.ReadToEnd()
            
            Write-Host ""
            Write-Host "Error Response:" -ForegroundColor Red
            Write-Host $errorContent -ForegroundColor Yellow
            
            try {
                $errorJson = $errorContent | ConvertFrom-Json
                
                if ($errorJson.error_message) {
                    Write-Host ""
                    Write-Host "Error Message: $($errorJson.error_message)" -ForegroundColor Red
                    Write-Host ""
                    Write-Host "DIAGNOSIS:" -ForegroundColor Cyan
                    
                    if ($errorJson.error_message -like "*AUTHORIZATION*" -or 
                        $errorJson.error_message -like "*AUTH*" -or 
                        $errorJson.error_message -like "*INVALID*KEY*" -or
                        $errorJson.error_message -like "*INVALID*SECRET*") {
                        Write-Host "  ❌ API Key or Secret is INVALID" -ForegroundColor Red
                        Write-Host "     Please check your credentials in application.properties" -ForegroundColor Yellow
                        Write-Host "     Current Key: $($apiKey.Substring(0, 10))..." -ForegroundColor Gray
                    }
                    elseif ($errorJson.error_message -like "*CONCURRENCY*" -or 
                            $errorJson.error_message -like "*QUOTA*" -or
                            $errorJson.error_message -like "*RATE*") {
                        Write-Host "  ⚠️  API quota exceeded or rate limit reached" -ForegroundColor Yellow
                        Write-Host "     Wait a moment and try again, or upgrade your plan" -ForegroundColor Yellow
                    }
                    elseif ($errorJson.error_message -like "*INVALID_IMAGE*" -or
                            $errorJson.error_message -like "*IMAGE*") {
                        Write-Host "  ⚠️  Image format issue (but API key might be valid)" -ForegroundColor Yellow
                        Write-Host "     Try with a different image format" -ForegroundColor Yellow
                    }
                    else {
                        Write-Host "  ⚠️  Unexpected error: $($errorJson.error_message)" -ForegroundColor Yellow
                        Write-Host "     Check Face++ API documentation for details" -ForegroundColor Yellow
                    }
                }
            }
            catch {
                Write-Host "Could not parse error response as JSON" -ForegroundColor Yellow
            }
        }
        catch {
            Write-Host "Could not read error response" -ForegroundColor Yellow
        }
        
        Write-Host ""
        if ($statusCode -eq 200) {
            Write-Host "CONCLUSION: API Key appears to be VALID (HTTP 200)" -ForegroundColor Green
        }
        else {
            Write-Host "CONCLUSION: API Key test FAILED (HTTP $statusCode)" -ForegroundColor Red
        }
    }
    else {
        Write-Host "Error: $errorMessage" -ForegroundColor Red
        
        if ($errorMessage -like "*timeout*") {
            Write-Host ""
            Write-Host "DIAGNOSIS: Request timed out" -ForegroundColor Yellow
            Write-Host "  - Network connectivity issue" -ForegroundColor Yellow
            Write-Host "  - Face++ API server might be slow" -ForegroundColor Yellow
        }
        elseif ($errorMessage -like "*SSL*" -or $errorMessage -like "*certificate*") {
            Write-Host ""
            Write-Host "DIAGNOSIS: SSL/TLS certificate issue" -ForegroundColor Yellow
        }
        else {
            Write-Host ""
            Write-Host "DIAGNOSIS: Unknown error occurred" -ForegroundColor Yellow
        }
        
        Write-Host ""
        Write-Host "CONCLUSION: Unable to determine API key validity" -ForegroundColor Yellow
    }
}

Write-Host ""

