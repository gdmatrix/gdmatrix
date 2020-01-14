;This file will be executed next to the application bundle image
;I.e. current directory will contain folder MatrixClient with application files
[Setup]
AppId={{MatrixClient}}
AppName=SFLClient
AppVersion=0.5
AppVerName=SFLClient 0.5
AppPublisher=Ajuntament de Sant Feliu de Llobregat
AppComments=SFLClient
AppCopyright=
;AppPublisherURL=http://java.com/
;AppSupportURL=http://java.com/
;AppUpdatesURL=http://java.com/
ChangesAssociations=Yes
;Local or common installation folders
;DefaultDirName={localappdata}\MatrixClient
DefaultDirName={commonappdata}\MatrixClient
DisableStartupPrompt=Yes
DisableDirPage=Yes
DisableProgramGroupPage=Yes
DisableReadyPage=Yes
DisableFinishedPage=Yes
DisableWelcomePage=Yes
DefaultGroupName=Ajuntament de Sant Feliu de Llobregat
;Optional License
LicenseFile=
;WinXP or above
MinVersion=0,5.1 
OutputBaseFilename=SFLClientInstaller
Compression=lzma
SolidCompression=yes
PrivilegesRequired=admin
SetupIconFile=MatrixClient\MatrixClient.ico
UninstallDisplayIcon={app}\MatrixClient.ico
UninstallDisplayName=SFLClient
WizardImageStretch=No
WizardSmallImageFile=MatrixClient-setup-icon.bmp   

[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"
Name: "spanish"; MessagesFile: "compiler:Languages\Spanish.isl"
Name: "catalan"; MessagesFile: "compiler:Languages\Catalan.isl"

[Tasks]
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}"; Flags: unchecked

[Files]
Source: "MatrixClient\MatrixClient.exe"; DestDir: "{app}"; Flags: ignoreversion
Source: "MatrixClient\*"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs createallsubdirs

[Registry]
Root: HKCR; Subkey: "matrix"; ValueType: "string"; ValueData: "URL:Matrix Protocol"; Flags: uninsdeletekey
Root: HKCR; Subkey: "matrix"; ValueType: "string"; ValueName: "URL Protocol"; ValueData: ""
Root: HKCR; Subkey: "matrix\DefaultIcon"; ValueType: "string"; ValueData: "{app}\MatrixClient.exe,0"
Root: HKCR; Subkey: "matrix\shell\open\command"; ValueType: "string"; ValueData: """{app}\MatrixClient.exe"" ""%1"""

[Icons]
Name: "{group}\SFLClient"; Filename: "{app}\MatrixClient.exe"; IconFilename: "{app}\MatrixClient.ico"; Check: returnTrue()
Name: "{commondesktop}\SFLClient"; Filename: "{app}\MatrixClient.exe";  IconFilename: "{app}\MatrixClient.ico"; Check: returnFalse(); 
Name: "{userdesktop}\SFLClient"; Filename: "{app}\MatrixClient.exe"; Tasks: desktopicon

[Run]
Filename: "{app}\MatrixClient.exe"; Description: "{cm:LaunchProgram,MatrixClient}"; Flags: nowait postinstall skipifsilent

[Code]
function returnTrue(): Boolean;
begin
  Result := True;
end;

function returnFalse(): Boolean;
begin
  Result := False;
end;

function InitializeSetup(): Boolean;
begin
// Possible future improvements:
//   if version less or same => just launch app
//   if upgrade => check if same app is running and wait for it to exit
//   Add pack200/unpack200 support? 
  Result := True;
end;  

[UninstallDelete]
Type: files; Name: "{app}\app\truststore.jks"
Type: files; Name: "{app}\app\*.jar"