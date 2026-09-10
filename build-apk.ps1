param([switch]$Preview)
$ErrorActionPreference = "Stop"

# Android 16 / API 36. An explicit SDK root takes precedence over the local SDK.
$ProjectRoot = $PSScriptRoot
$DefaultSdkRoot = if (Test-Path (Join-Path $ProjectRoot '.tooling\android-sdk')) { Join-Path $ProjectRoot '.tooling\android-sdk' } else { "C:\Android" }
$SdkRoot = if ($env:ANDROID_SDK_ROOT -and
    (Test-Path -LiteralPath (Join-Path $env:ANDROID_SDK_ROOT "platforms\android-36\android.jar") -PathType Leaf)) {
    $env:ANDROID_SDK_ROOT
} else {
    $DefaultSdkRoot
}
$BuildToolsVersion = "36.0.0"
$MinSdk = "23"

$AndroidJar = Join-Path $SdkRoot "platforms\android-36\android.jar"
$BuildTools = Join-Path $SdkRoot "build-tools\$BuildToolsVersion"
$D8 = Join-Path $BuildTools "d8.bat"
$Aapt2 = Join-Path $BuildTools "aapt2.exe"
$Zipalign = Join-Path $BuildTools "zipalign.exe"
$Apksigner = Join-Path $BuildTools "apksigner.bat"

$JavaSourceDir = Join-Path $ProjectRoot "app\src\main\java"
$ResourceDir = Join-Path $ProjectRoot "app\src\main\res"
$Manifest = Join-Path $ProjectRoot "app\src\main\AndroidManifest.xml"
$BuildSubdir = if ($Preview) { 'build\apk-preview' } else { 'build\apk' }
$BuildDir = Join-Path $ProjectRoot $BuildSubdir
$ClassesDir = Join-Path $BuildDir "classes"
$DexDir = Join-Path $BuildDir "dex"
$CompiledResourcesDir = Join-Path $BuildDir "compiled-res"
$UnsignedApk = Join-Path $BuildDir "KanaBridge-unsigned.apk"
$AlignedApk = Join-Path $BuildDir "KanaBridge-aligned.apk"
$DistDir = Join-Path $ProjectRoot "dist"
$FinalApk = Join-Path $DistDir $(if ($Preview) { 'KanaBridge-preview.apk' } else { 'KanaBridge.apk' })
$SignatureSidecar = "$FinalApk.idsig"
$ToolsDir = Join-Path $ProjectRoot "tools"
$Keystore = Join-Path $ToolsDir "kanabridge-release.jks"

function Require-Command([string]$Name) {
    $command = Get-Command $Name -ErrorAction SilentlyContinue
    if ($null -eq $command) {
        throw "Java não foi encontrado: falta '$Name' no PATH. Instale/configure o JDK 17 ou 21 e abra uma nova janela PowerShell."
    }
    return $command.Source
}

function Require-File([string]$Path, [string]$Description) {
    if (-not (Test-Path -LiteralPath $Path -PathType Leaf)) {
        throw "$Description não foi encontrado em: $Path`nConfirme que o Android SDK 36 está instalado em '$SdkRoot' ou defina ANDROID_SDK_ROOT."
    }
}

function Invoke-Tool([string]$Description, [string]$Path, [string[]]$Arguments) {
    # aapt2 on Windows cannot reliably open absolute paths containing accents.
    # Keep project arguments relative and run every tool from the project root.
    $PortableArguments = @($Arguments | ForEach-Object {
        if ($_.StartsWith($ProjectRoot + [IO.Path]::DirectorySeparatorChar)) { $_.Substring($ProjectRoot.Length + 1) } else { $_ }
    })
    Push-Location -LiteralPath $ProjectRoot
    try {
        & $Path @PortableArguments
        if ($LASTEXITCODE -ne 0) {
            throw "$Description falhou (código de saída $LASTEXITCODE)."
        }
    } finally {
        Pop-Location
    }
}

$Javac = Require-Command "javac"
$Jar = Require-Command "jar"
$Keytool = Require-Command "keytool"

Require-File $AndroidJar "android.jar"
Require-File $D8 "d8"
Require-File $Aapt2 "aapt2"
Require-File $Zipalign "zipalign"
Require-File $Apksigner "apksigner"
Require-File $Manifest "AndroidManifest.xml"

$JavaSources = @(Get-ChildItem -Path $JavaSourceDir -Filter "*.java" -File -Recurse |
    ForEach-Object { $_.FullName })
if ($JavaSources.Count -eq 0) {
    throw "Não foram encontrados ficheiros Java em: $JavaSourceDir"
}

if (Test-Path -LiteralPath $BuildDir) {
    $ResolvedBuild = (Resolve-Path -LiteralPath $BuildDir).Path
    $ExpectedBuild = [IO.Path]::GetFullPath((Join-Path $ProjectRoot $BuildSubdir))
    if ($ResolvedBuild -ne $ExpectedBuild -or -not $ResolvedBuild.StartsWith($ProjectRoot + [IO.Path]::DirectorySeparatorChar)) { throw 'Build path is outside the project.' }
    Remove-Item -LiteralPath $BuildDir -Recurse -Force
}
New-Item -ItemType Directory -Force -Path $ClassesDir, $DexDir, $CompiledResourcesDir, $DistDir, $ToolsDir | Out-Null
if ($Preview) {
    $PreviewManifest = Join-Path $BuildDir 'AndroidManifest.xml'
    (Get-Content -LiteralPath $Manifest -Raw).Replace('package="com.kanabridge"', 'package="com.kanabridge.preview"').Replace('android:name=".MainActivity"', 'android:name="com.kanabridge.MainActivity"').Replace('android:label="KanaBridge"', 'android:label="KanaBridge 3"') | Set-Content -LiteralPath $PreviewManifest -Encoding utf8
    $Manifest = $PreviewManifest
}

Write-Host "A compilar Java..."
$JavacArguments = @(
    "-encoding", "UTF-8", "-source", "8", "-target", "8", "-Xlint:-options",
    "-classpath", $AndroidJar, "-d", $ClassesDir
) + $JavaSources
Invoke-Tool "javac" $Javac $JavacArguments

$ClassFiles = @(Get-ChildItem -Path $ClassesDir -Filter "*.class" -File -Recurse |
    ForEach-Object { $_.FullName })
if ($ClassFiles.Count -eq 0) {
    throw "javac terminou sem gerar classes Java."
}

Write-Host "A criar classes.dex..."
$D8Arguments = @(
    "--lib", $AndroidJar, "--min-api", $MinSdk, "--output", $DexDir
) + $ClassFiles
Invoke-Tool "d8" $D8 $D8Arguments

$ClassesDex = Join-Path $DexDir "classes.dex"
Require-File $ClassesDex "classes.dex"

Write-Host "A compilar recursos Android..."
Invoke-Tool "aapt2 compile" $Aapt2 @("compile", "--dir", $ResourceDir, "-o", $CompiledResourcesDir)

$CompiledResources = @(Get-ChildItem -Path $CompiledResourcesDir -Filter "*.flat" -File -Recurse |
    ForEach-Object { $_.FullName })
if ($CompiledResources.Count -eq 0) {
    throw "aapt2 não gerou recursos compilados."
}

$LinkArguments = @(
    "link", "-I", $AndroidJar, "--manifest", $Manifest,
    "--min-sdk-version", $MinSdk, "--target-sdk-version", "36",
    "--version-code", "3", "--version-name", "3.0.0",
    "-o", $UnsignedApk
)
foreach ($Resource in $CompiledResources) {
    $LinkArguments += @("-R", $Resource)
}

Write-Host "A criar APK..."
Invoke-Tool "aapt2 link" $Aapt2 $LinkArguments
Invoke-Tool "jar" $Jar @("--update", "--file", $UnsignedApk, "-C", $DexDir, "classes.dex")

Write-Host "A alinhar APK..."
Invoke-Tool "zipalign" $Zipalign @("-f", "4", $UnsignedApk, $AlignedApk)

# This is a local development signing key. It stays ignored by Git and is reused on later builds.
# The password is never written to the console.
$SigningPassword = "kanabridge-local"
if (-not (Test-Path -LiteralPath $Keystore -PathType Leaf)) {
    Write-Host "A criar keystore local..."
    Invoke-Tool "keytool" $Keytool @(
        "-genkeypair", "-noprompt", "-storetype", "JKS",
        "-keystore", $Keystore, "-storepass", $SigningPassword,
        "-keypass", $SigningPassword, "-alias", "kanabridge",
        "-keyalg", "RSA", "-keysize", "2048", "-validity", "10000",
        "-dname", "CN=KanaBridge, OU=Offline Apps, O=KanaBridge, C=PT"
    )
}

$env:KANABRIDGE_KEYSTORE_PASSWORD = $SigningPassword
try {
    Write-Host "A assinar APK..."
    Remove-Item -LiteralPath $SignatureSidecar -Force -ErrorAction SilentlyContinue
    Invoke-Tool "apksigner" $Apksigner @(
        "sign", "--ks", $Keystore, "--ks-key-alias", "kanabridge",
        "--ks-pass", "env:KANABRIDGE_KEYSTORE_PASSWORD",
        "--key-pass", "env:KANABRIDGE_KEYSTORE_PASSWORD",
        "--v4-signing-enabled", "false",
        "--out", $FinalApk, $AlignedApk
    )
} finally {
    Remove-Item Env:\KANABRIDGE_KEYSTORE_PASSWORD -ErrorAction SilentlyContinue
}

Write-Host "APK criado: $FinalApk"
Invoke-Tool "Verificação da assinatura" $Apksigner @("verify", "--verbose", $FinalApk)
