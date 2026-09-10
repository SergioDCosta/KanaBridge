param([string]$Serial = 'emulator-5556', [string]$ScreenshotFolder = 'android16')
$ErrorActionPreference = 'Stop'
if ($Serial -notmatch '^emulator-\d+$') { throw 'This test suite is restricted to emulators.' }
$projectPath = $PSScriptRoot
$sdkPath = if ($env:ANDROID_SDK_ROOT) { $env:ANDROID_SDK_ROOT } else { Join-Path $projectPath '.tooling/android-sdk' }
$toolPath = Join-Path $sdkPath 'build-tools/36.0.0'
$adbPath = Join-Path $sdkPath 'platform-tools/adb.exe'
$env:ANDROID_USER_HOME = Join-Path $projectPath '.tooling/android-user'
function Tool([string]$exe, [string[]]$arguments) {
    & $exe @arguments
    if ($LASTEXITCODE -ne 0) { throw "$exe failed: $LASTEXITCODE" }
}
Push-Location -LiteralPath $projectPath
try {
    $testRoot = 'build/android-tests'
    New-Item -ItemType Directory -Force -Path "$testRoot/classes", "$testRoot/dex" | Out-Null
    $androidJarPath = Join-Path $sdkPath 'platforms/android-36/android.jar'
    if ($androidJarPath.StartsWith($projectPath + '\')) { $androidJarPath = $androidJarPath.Substring($projectPath.Length + 1) }
    Tool 'javac' @('-encoding', 'UTF-8', '-source', '8', '-target', '8', '-Xlint:-options', '-classpath', $androidJarPath,
        '-d', "$testRoot/classes", 'tests/android/AndroidSmokeTest.java')
    $classFiles = @(Get-ChildItem "$testRoot/classes" -Recurse -Filter '*.class' | ForEach-Object { $_.FullName.Substring($projectPath.Length + 1) })
    Tool (Join-Path $toolPath 'd8.bat') (@('--lib', $androidJarPath, '--min-api', '23', '--output', "$testRoot/dex") + $classFiles)
    Tool (Join-Path $toolPath 'aapt2.exe') @('link', '-I', $androidJarPath, '--manifest', 'tests/android/AndroidManifest.xml', '-o', "$testRoot/unsigned.apk")
    Tool 'jar' @('--update', '--file', "$testRoot/unsigned.apk", '-C', "$testRoot/dex", 'classes.dex')
    Tool (Join-Path $toolPath 'zipalign.exe') @('-f', '4', "$testRoot/unsigned.apk", "$testRoot/aligned.apk")
    $env:KANABRIDGE_TEST_PASSWORD = 'kanabridge-local'
    try {
        Tool (Join-Path $toolPath 'apksigner.bat') @('sign', '--ks', 'tools/kanabridge-release.jks', '--ks-key-alias', 'kanabridge',
            '--ks-pass', 'env:KANABRIDGE_TEST_PASSWORD', '--key-pass', 'env:KANABRIDGE_TEST_PASSWORD', '--out', "$testRoot/tests.apk", "$testRoot/aligned.apk")
    } finally { Remove-Item Env:\KANABRIDGE_TEST_PASSWORD -ErrorAction SilentlyContinue }
    Tool $adbPath @('-s', $Serial, 'install', '-r', 'dist/KanaBridge.apk')
    Tool $adbPath @('-s', $Serial, 'install', '-r', "$testRoot/tests.apk")
    $output = & $adbPath -s $Serial shell am instrument -w com.kanabridge.tests/com.kanabridge.tests.AndroidSmokeTest
    $output | Write-Output
    if ($LASTEXITCODE -ne 0 -or ($output -join "`n") -notmatch 'OK: \d+ Android UI assertions') { throw 'Android instrumentation failed.' }
    $destination = Join-Path 'build/screenshots' $ScreenshotFolder
    New-Item -ItemType Directory -Force -Path $destination | Out-Null
    Tool $adbPath @('-s', $Serial, 'pull', '/sdcard/Android/data/com.kanabridge/files/screenshots/.', $destination)
} finally { Pop-Location }
