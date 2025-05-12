@echo off
echo Building Android Display app...
call ./gradlew clean build

echo.
echo Finding connected devices...
for /f "tokens=1" %%a in ('adb devices ^| findstr /v "emulator" ^| findstr /v "List" ^| findstr /v "^$"') do (
    set DEVICE_ID=%%a
    goto :found_device
)

:found_device
if "%DEVICE_ID%"=="" (
    echo No non-emulator devices found!
    exit /b 1
)

echo Found device: %DEVICE_ID%
echo.
echo Installing app on device...
adb -s %DEVICE_ID% install app/build/outputs/apk/debug/app-debug.apk

echo.
echo Launching app...
adb -s %DEVICE_ID% shell am start -n com.systeminfo.display/com.systeminfo.display.MainActivity

echo.
echo Deployment complete! 