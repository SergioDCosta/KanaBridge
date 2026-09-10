$ErrorActionPreference = "Stop"

$projectRoot = $PSScriptRoot
$testDir = Join-Path $projectRoot "build\tests"

New-Item -ItemType Directory -Force -Path $testDir | Out-Null

$javaRoot = Join-Path $projectRoot "app\src\main\java\com\kanabridge"
$sources = @(
    (Join-Path $javaRoot "KanaEntry.java"),
    (Join-Path $javaRoot "KanaData.java"),
    (Join-Path $javaRoot "KanaTransliterator.java"),
    (Join-Path $javaRoot "LearningContent.java"),
    (Join-Path $javaRoot "ReviewSchedule.java"),
    (Join-Path $javaRoot "StudySession.java")
)
$tests = @(Get-ChildItem -LiteralPath (Join-Path $projectRoot "tests") -Filter '*.java' | ForEach-Object FullName)

javac -encoding UTF-8 -d $testDir $sources $tests

if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}

java -cp $testDir com.kanabridge.KanaTransliteratorTest
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
java -cp $testDir com.kanabridge.StudySessionTest
exit $LASTEXITCODE
