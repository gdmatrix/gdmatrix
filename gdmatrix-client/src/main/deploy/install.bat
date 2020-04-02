@echo off
echo GDMatrixClient installer
set app_name=gdmatrix-client
set inst_dir=%~dp0
cd %inst_dir%

elevate.exe -c -w einstall.bat %app_name%

if "%1" == "silent" goto :end
call "%programdata%"\gdmatrix\client\%app_name%.bat

:end
