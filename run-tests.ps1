$ErrorActionPreference = "Stop"

$projectRoot = $PSScriptRoot
$testDir = Join-Path $projectRoot "build\tests"

New-Item -ItemType Directory -Force -Path $testDir | Out-Null
Remove-Item "$testDir\*" -Recurse -Force -ErrorAction SilentlyContinue

$transliterator = Join-Path $projectRoot "app\src\main\java\com\kanabridge\KanaTransliterator.java"
$tests = Join-Path $projectRoot "tests\KanaTransliteratorTest.java"

javac -encoding UTF-8 -d $testDir $transliterator $tests

if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}

java -cp $testDir com.kanabridge.KanaTransliteratorTest
exit $LASTEXITCODE