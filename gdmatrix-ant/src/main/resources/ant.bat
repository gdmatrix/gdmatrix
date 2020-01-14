@echo off
set CP=.
for %%a in (./lib/*.jar) do call :append %%a
goto :run

:append
if "%1"=="" goto :EOF
set CP=%CP%;./lib/%1
goto :EOF

:run
java.exe -cp %CP% org.santfeliu.ant.AntLauncher %1 %2 %3 %4 %5 %6 %7 %8 %9
