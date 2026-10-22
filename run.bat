@echo off
title Menjalankan Aplikasi Kasir Frozen (Java Swing)
echo ========================================================
echo        MENYIAPKAN & MENJALANKAN KASIR FROZEN
echo ========================================================
echo.

if not exist "bin" mkdir bin

echo [1/2] Melakukan kompilasi file Java...
javac -encoding UTF-8 -d bin -sourcepath src/main/java src/main/java/com/kasir/*.java src/main/java/com/kasir/model/*.java src/main/java/com/kasir/service/*.java src/main/java/com/kasir/util/*.java src/main/java/com/kasir/view/*.java

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Kompilasi gagal! Pastikan JDK sudah terpasang.
    pause
    exit /b %ERRORLEVEL%
)

echo [2/2] Membuka antarmuka Kasir Frozen...
start javaw -cp bin com.kasir.Main

echo.
echo Aplikasi Kasir Frozen berhasil dibuka!
echo ========================================================
timeout /t 3 >nul
