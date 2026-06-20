# Verification script for the incremental compilation issue with the legacy approach.
#
# The legacy approach declares the targetCall target function in runtime-api (a stable, non-inline
# function) and the aics plugin does NOT generate any source for it
# (aics { generateTargetFunction = false }).
#
# Because the interface of ClassA never changes, incremental compilation skips recompiling ClassA
# when ClassB is added. The compiler plugin only accumulates an accessor for ClassB into the
# regenerated GeneratedContext, so the accessor for ClassA disappears and the already-compiled
# ClassA.class fails at runtime with NoSuchMethodError.
#
# Run from: integration-test/legacy-sample directory.

$ErrorActionPreference = "Stop"

$rootDir = Resolve-Path "..\..\"
$gradlew = Join-Path $rootDir "gradlew.bat"
$classBPath = "src\main\kotlin\org\wrongwrong\legacy\ClassB.kt"
$classBBackup = "$classBPath.bak"

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
    Write-Host "Build 1 (ClassA only): SUCCESS" -ForegroundColor Green
}
finally {
    # Restore ClassB without modifying ClassA so incremental compilation skips ClassA.
    if (Test-Path $classBBackup) {
        Move-Item -Path $classBBackup -Destination $classBPath -Force
    }
}

Write-Host "=== Step 3: Add ClassB (ClassA unchanged - skipped by incremental compile) ===" -ForegroundColor Cyan
# The test task is expected to fail here, so do not let stderr abort the script.
$ErrorActionPreference = "Continue"
& $gradlew test 2>&1 | Out-Null

$results = Get-TestResults
$classAName = "ClassA doSomething returns called by FQN()"
$classBName = "ClassB doSomething returns called by FQN()"

Write-Host "=== Test Results ===" -ForegroundColor Cyan
foreach ($name in $results.Keys) {
    $status = if ($results[$name]) { "FAILED" } else { "PASSED" }
    Write-Host ("  {0}: {1}" -f $name, $status)
}

$classAFailed = $results.ContainsKey($classAName) -and $results[$classAName]
$classBPassed = $results.ContainsKey($classBName) -and (-not $results[$classBName])

if ($classAFailed -and $classBPassed) {
    Write-Host "EXPECTED: Incremental compilation issue reproduced (ClassA failed, ClassB passed)." -ForegroundColor Green
    exit 0
} else {
    Write-Host "UNEXPECTED: The incremental compilation issue was NOT reproduced." -ForegroundColor Red
    exit 1
}
