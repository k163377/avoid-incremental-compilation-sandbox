# Verification script for the improved approach.
#
# The improved approach generates the targetCall target function as an `internal inline` function with
# a fresh random marker on every build (build/generated/aics/.../TargetCall.kt). Because the body of
# an inline function is part of the ABI of its callers, regenerating it marks every call site as
# dirty, so incremental compilation recompiles ClassA together with ClassB. The compiler plugin then
# accumulates accessors for BOTH ClassA and ClassB into GeneratedContext and the issue does not occur.
#
# Run from: integration-test/improved-sample directory.

$ErrorActionPreference = "Stop"

$rootDir = Resolve-Path "..\..\"
$gradlew = Join-Path $rootDir "gradlew.bat"
$classBPath = "src\main\kotlin\org\wrongwrong\sample\ClassB.kt"
$classBBackup = "$classBPath.bak"
$generatedFile = "build\generated\aics\org\wrongwrong\sample\aics\TargetCall.kt"

function Get-TestResults {
    $results = @{}
    Get-ChildItem -Path "build\test-results\test" -Filter "*.xml" -ErrorAction SilentlyContinue | ForEach-Object {
        [xml]$xml = Get-Content $_.FullName
        foreach ($tc in $xml.testsuite.testcase) {
            $failed = ($tc.failure -ne $null) -or ($tc.error -ne $null)
            $results[$tc.name] = $failed
        }
    }
    return $results
}

function Get-Marker {
    if (Test-Path $generatedFile) {
        $line = (Get-Content $generatedFile | Select-String "val marker")
        if ($line) { return $line.ToString().Trim() }
    }
    return $null
}

try {
    Write-Host "=== Step 1: Clean build ===" -ForegroundColor Cyan
    & $gradlew clean 2>&1 | Out-Null

    # Temporarily hide ClassB so that only ClassA exists for the first compilation.
    Move-Item -Path $classBPath -Destination $classBBackup -Force

    Write-Host "=== Step 2: Build with ClassA only (ClassB hidden) ===" -ForegroundColor Cyan
    & $gradlew compileKotlin 2>&1 | Out-Null
    if ($LASTEXITCODE -ne 0) {
        Write-Host "ERROR: Build failed in step 2" -ForegroundColor Red
        exit 1
    }
    $markerBuild1 = Get-Marker
    Write-Host "Build 1 (ClassA only): SUCCESS" -ForegroundColor Green
    Write-Host "Build 1 marker: $markerBuild1" -ForegroundColor Gray
}
finally {
    # Restore ClassB without modifying ClassA.
    if (Test-Path $classBBackup) {
        Move-Item -Path $classBBackup -Destination $classBPath -Force
    }
}

Write-Host "=== Step 3: Add ClassB (ClassA unchanged in source) ===" -ForegroundColor Cyan
# Avoid letting any gradle stderr output abort the script.
$ErrorActionPreference = "Continue"
& $gradlew test 2>&1 | Out-Null

$markerBuild2 = Get-Marker
Write-Host "Build 2 marker: $markerBuild2" -ForegroundColor Gray
if ($markerBuild1 -ne $markerBuild2) {
    Write-Host "targetCall was regenerated with a new random marker -> ClassA is forced to recompile." -ForegroundColor Green
}

$results = Get-TestResults
$classAName = "ClassA doSomething returns called by FQN()"
$classBName = "ClassB doSomething returns called by FQN()"

Write-Host "=== Test Results ===" -ForegroundColor Cyan
foreach ($name in $results.Keys) {
    $status = if ($results[$name]) { "FAILED" } else { "PASSED" }
    Write-Host ("  {0}: {1}" -f $name, $status)
}

$classAPassed = $results.ContainsKey($classAName) -and (-not $results[$classAName])
$classBPassed = $results.ContainsKey($classBName) -and (-not $results[$classBName])

if ($classAPassed -and $classBPassed) {
    Write-Host "SUCCESS: Both tests passed - the improved version avoids the incremental compilation issue." -ForegroundColor Green
    exit 0
} else {
    Write-Host "FAILURE: Tests did not all pass - check the output above." -ForegroundColor Red
    exit 1
}
