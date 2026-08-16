$ErrorActionPreference = "Stop"

$projectRoot = $PSScriptRoot
$testDir = Join-Path $projectRoot "build\tests"

New-Item -ItemType Directory -Force -Path $testDir | Out-Null
Remove-Item "$testDir\*" -Recurse -Force -ErrorAction SilentlyContinue

$javaRoot = Join-Path $projectRoot "app\src\main\java\com\kanabridge"
$sources = @(
    (Join-Path $javaRoot "KanaEntry.java"),
    (Join-Path $javaRoot "KanaData.java"),
    (Join-Path $javaRoot "KanaTransliterator.java")
)
$tests = Join-Path $projectRoot "tests\KanaTransliteratorTest.java"

javac -encoding UTF-8 -d $testDir $sources $tests

if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}

java -cp $testDir com.kanabridge.KanaTransliteratorTest
exit $LASTEXITCODE
