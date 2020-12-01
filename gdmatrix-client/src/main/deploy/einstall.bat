@echo off
set app_name=%1
set app_desc=%2
set inst_dir=%~dp0
cd %inst_dir%

echo Installing %app_name%...

reg add HKCR\matrix /f /t REG_SZ /d "URL:Matrix Protocol" 2> nul
reg add HKCR\matrix /f /v "URL Protocol" /t REG_SZ /d ""
reg add HKCR\matrix\shell\open\command /f /t REG_SZ /d "%programdata%\gdmatrix\client\%app_name%.bat %%1"

rmdir /s /q "%programdata%"\gdmatrix\jre
rmdir /s /q "%programdata%"\gdmatrix\client

7za x "%inst_dir%"\windows-jre.zip -y -o"%programdata%"\gdmatrix
7za x "%inst_dir%"\gdmatrix-client-win.zip -y -o"%programdata%"\gdmatrix

echo Set oWS = WScript.CreateObject("WScript.Shell") > %inst_dir%\shortcut.vbs
echo sLinkFile = "%USERPROFILE%\Desktop\gdmatrix-client.lnk" >> %inst_dir%\shortcut.vbs
echo Set oLink = oWS.CreateShortcut(sLinkFile) >> %inst_dir%\shortcut.vbs
echo oLink.Description = "%app_desc%" >> %inst_dir%\shortcut.vbs
echo oLink.TargetPath = "%programdata%\gdmatrix\client\%app_name%.bat" >> %inst_dir%\shortcut.vbs
echo oLink.IconLocation = "%programdata%\gdmatrix\client\gdmatrix.ico" >> %inst_dir%\shortcut.vbs
echo oLink.Save >> %inst_dir%\shortcut.vbs
cscript /nologo %inst_dir%\shortcut.vbs

echo cd "%programdata%"\gdmatrix\client > "%programdata%"\gdmatrix\client\%app_name%.bat
echo start ..\jre\bin\javaw -cp lib/* org.santfeliu.matrix.client.MatrixClient %%1 >> "%programdata%"\gdmatrix\client\%app_name%.bat
