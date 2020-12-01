@echo off
set app_name=gdmatrix-client
set app_desc=GDMatrixClient

echo %app_desc% installer

set inst_dir=%~dp0
cd %inst_dir%

elevate.exe -c -w einstall.bat %app_name% %app_desc%

if "%1" == "silent" goto :end
call "%programdata%"\gdmatrix\client\%app_name%.bat

:end
