@echo off
echo GDMatrixClient installer
set app_name=gdmatrix-client
set inst_dir=%~dp0
cd %inst_dir%

reg add HKCR\matrix /f /t REG_SZ /d "URL:Matrix Protocol" 2> nul
if %errorlevel% neq 0 goto :error
reg add HKCR\matrix /f /v "URL Protocol" /t REG_SZ /d ""
reg add HKCR\matrix\shell\open\command /f /t REG_SZ /d "C:\ProgramData\gdmatrix\client\%app_name%.bat %%1"

rmdir /s /q "%programdata%"\gdmatrix\jre
rmdir /s /q "%programdata%"\gdmatrix\client
xcopy /s /i "%inst_dir%"\jre "%programdata%"\gdmatrix\jre
xcopy /s /i "%inst_dir%"\lib "%programdata%"\gdmatrix\client\lib

echo cd "%programdata%"\gdmatrix\client > "%programdata%"\gdmatrix\client\%app_name%.bat
echo start ..\jre\bin\javaw -cp lib/* org.santfeliu.matrix.client.MatrixClient %%1 >> "%programdata%"\gdmatrix\client\%app_name%.bat

if "%1" == "silent" goto :end
call "%programdata%"\gdmatrix\client\%app_name%.bat
goto :end

:error
echo ==========================================================
echo   You must run this installer as administrator.            
echo   Debes ejecutar este instalador como administrador.       
echo   Has d'executar aquest instal.lador com a administrador.  
echo ==========================================================
pause

:end
