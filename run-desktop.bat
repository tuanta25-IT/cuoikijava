@echo off
setlocal

cd /d %~dp0

if not defined DB_HOST set DB_HOST=localhost
if not defined DB_PORT set DB_PORT=1433
if not defined DB_NAME set DB_NAME=QuanLyThuVien
if not defined DB_USERNAME set DB_USERNAME=sa
if not defined DB_PASSWORD set DB_PASSWORD=123456

mvn clean compile exec:java

endlocal
