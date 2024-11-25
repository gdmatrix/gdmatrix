cd %~dp0
start javaw -cp "../lib/*" --add-opens java.base/sun.net.www.protocol.http=ALL-UNNAMED --add-opens java.base/sun.net.www.protocol.https=ALL-UNNAMED org.santfeliu.matrix.ide.MatrixIDE

