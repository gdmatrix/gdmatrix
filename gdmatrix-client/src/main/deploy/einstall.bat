@echo off
set app_name=%1
set inst_dir=%~dp0
cd %inst_dir%

reg add HKCR\matrix /f /t REG_SZ /d "URL:Matrix Protocol" 2> nul
reg add HKCR\matrix /f /v "URL Protocol" /t REG_SZ /d ""
reg add HKCR\matrix\shell\open\command /f /t REG_SZ /d "C:\ProgramData\gdmatrix\client\%app_name%.bat %%1"

rmdir /s /q "%programdata%"\gdmatrix\jre
rmdir /s /q "%programdata%"\gdmatrix\client
xcopy /s /i "%inst_dir%"\jre "%programdata%"\gdmatrix\jre
xcopy /s /i "%inst_dir%"\lib "%programdata%"\gdmatrix\client\lib

echo cd "%programdata%"\gdmatrix\client > "%programdata%"\gdmatrix\client\%app_name%.bat
echo start ..\jre\bin\javaw -cp lib/* org.santfeliu.matrix.client.MatrixClient %%1 >> "%programdata%"\gdmatrix\client\%app_name%.bat
