param([Parameter(Mandatory=$true)][string]$Serial, [switch]$CompileOnly)
$ErrorActionPreference='Stop'
$ProjectPath=$PSScriptRoot
$SdkPath=$env:ANDROID_SDK_ROOT
if (!$SdkPath) { throw 'Define ANDROID_SDK_ROOT.' }
$ToolsPath=Join-Path $SdkPath 'build-tools/36.0.0'
$AndroidJarPath=Join-Path $SdkPath 'platforms/android-36/android.jar'
$AdbPath=Join-Path $SdkPath 'platform-tools/adb.exe'
function Run([string]$Exe,[string[]]$Arguments) { & $Exe @Arguments; if ($LASTEXITCODE -ne 0) { throw "$Exe failed." } }
Push-Location -LiteralPath $ProjectPath
try {
 $ProbePath='build/dictionary-probe'
 New-Item -ItemType Directory -Force "$ProbePath/classes","$ProbePath/dex" | Out-Null
 Run 'javac' @('-encoding','UTF-8','-source','8','-target','8','-Xlint:-options','-classpath',("$AndroidJarPath;build/apk-kanateste/classes"),'-d',"$ProbePath/classes",'tests/android/DictionaryProbe.java')
 Run 'jar' @('--create','--file',"$ProbePath/target.jar",'-C','build/apk-kanateste/classes','.')
 $ProbeClasses=@(Get-ChildItem "$ProbePath/classes" -Recurse -Filter '*.class' | ForEach-Object FullName)
 Run (Join-Path $ToolsPath 'd8.bat') (@('--lib',$AndroidJarPath,'--classpath',"$ProbePath/target.jar",'--min-api','23','--output',"$ProbePath/dex")+$ProbeClasses)
 '<manifest xmlns:android="http://schemas.android.com/apk/res/android" package="com.kanabridge.dictionaryprobe"><uses-sdk android:minSdkVersion="23" android:targetSdkVersion="36"/><application android:label="Dictionary probe"/><instrumentation android:name="com.kanabridge.DictionaryProbe" android:targetPackage="com.kanabridge.kanateste"/></manifest>' | Set-Content "$ProbePath/AndroidManifest.xml" -Encoding utf8
 Run (Join-Path $ToolsPath 'aapt2.exe') @('link','-I',$AndroidJarPath,'--manifest',"$ProbePath/AndroidManifest.xml",'-o',"$ProbePath/unsigned.apk")
 Run 'jar' @('--update','--file',"$ProbePath/unsigned.apk",'-C',"$ProbePath/dex",'classes.dex')
 Run (Join-Path $ToolsPath 'zipalign.exe') @('-f','4',"$ProbePath/unsigned.apk","$ProbePath/aligned.apk")
 $env:KANABRIDGE_TEST_PASSWORD='kanabridge-local'
 try { Run (Join-Path $ToolsPath 'apksigner.bat') @('sign','--ks','tools/kanabridge-release.jks','--ks-key-alias','kanabridge','--ks-pass','env:KANABRIDGE_TEST_PASSWORD','--out',"$ProbePath/tests.apk","$ProbePath/aligned.apk") }
 finally {Remove-Item Env:\KANABRIDGE_TEST_PASSWORD -ErrorAction SilentlyContinue}
 if($CompileOnly){Write-Output 'Repository probe compiled only.';return}
 Run $AdbPath @('-s',$Serial,'install','-r',"$ProbePath/tests.apk")
 try {
  $ProbeOutput=& $AdbPath -s $Serial shell am instrument -w com.kanabridge.dictionaryprobe/com.kanabridge.DictionaryProbe
  $ProbeOutput | Write-Output
  $ProbeOutput | Set-Content "$ProbePath/result.txt" -Encoding utf8
  if(($ProbeOutput -join "`n") -notmatch 'OK: \d+ repository and migration assertions') {throw 'Repository probe failed.'}
 } finally {Run $AdbPath @('-s',$Serial,'uninstall','com.kanabridge.dictionaryprobe')}
} finally {Pop-Location}
