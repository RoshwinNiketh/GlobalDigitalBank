$ErrorActionPreference='Stop'
Set-Location (Split-Path -Parent $PSScriptRoot)
mvn -q test dependency:build-classpath "-Dmdep.outputFile=target/runtime-classpath.txt"
if ($LASTEXITCODE -ne 0) { throw 'Compilation failed' }
$projectPath=(Get-Location).Path
$runtimePath=Join-Path $projectPath 'target/runtime-classpath.txt'
$cp=(Join-Path $projectPath 'target/classes')+';'+(Get-Content $runtimePath -Raw).Trim()
$testRoot=Join-Path $projectPath 'target/acceptance-work'
New-Item -ItemType Directory -Force -Path $testRoot | Out-Null
$tests=Get-ChildItem src/main/java/com/gdb/tests/Test*.java
foreach($test in $tests) {
 $out=Join-Path $testRoot ($test.BaseName+'.out')
 $err=Join-Path $testRoot ($test.BaseName+'.err')
 $process=Start-Process java -ArgumentList @('--enable-native-access=ALL-UNNAMED','-ea','-cp',('"'+$cp+'"'),('com.gdb.tests.'+$test.BaseName)) -WorkingDirectory $testRoot -RedirectStandardOutput $out -RedirectStandardError $err -WindowStyle Hidden -PassThru
 $processHandle=$process.Handle; if(-not $process.WaitForExit(60000)) { $process.Kill(); throw ($test.BaseName+' timed out') }
 if($process.ExitCode -ne 0) { Get-Content $out; Get-Content $err; throw ($test.BaseName+' FAILED') }
 Write-Output ($test.BaseName+' PASSED')
}


