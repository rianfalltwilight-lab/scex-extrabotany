param([Parameter(Mandatory = $true)][string]$ArchivePath)

$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot 'source-files.ps1')
Add-Type -AssemblyName System.IO.Compression
Add-Type -AssemblyName System.IO.Compression.FileSystem
$path = (Resolve-Path -LiteralPath $ArchivePath).Path
$archive = [System.IO.Compression.ZipFile]::OpenRead($path)
try {
    $names = [System.Collections.Generic.HashSet[string]]::new([System.StringComparer]::OrdinalIgnoreCase)
    $hashes = [System.Collections.Generic.List[object]]::new()
    $buildInfo = $null
    foreach ($entry in $archive.Entries) {
        $relative = $entry.FullName
        if (-not $names.Add($relative)) { throw "Duplicate archive path: $relative" }
        if ($relative -cne 'BUILD-INFO.txt' -and -not (Test-ExtraBotanySourceRelativePath -RelativePath $relative)) {
            throw "Non-source or unsafe archive entry: $relative"
        }
        $stream = $entry.Open()
        try {
            if ($relative -ceq 'BUILD-INFO.txt') {
                $reader = [System.IO.StreamReader]::new($stream, [System.Text.Encoding]::UTF8, $true)
                try { $buildInfo = Read-ExtraBotanyBuildInfo -Content $reader.ReadToEnd() } finally { $reader.Dispose() }
            } else {
                $hashes.Add([pscustomobject]@{ Relative = $relative; Hash = (Get-ExtraBotanyStreamSHA256 -Stream $stream) })
            }
        } finally { $stream.Dispose() }
    }
    if ($null -eq $buildInfo) { throw 'Archive is missing BUILD-INFO.txt.' }
    foreach ($required in @('build.gradle', 'gradle.properties', 'gradlew', 'gradlew.bat',
            'gradle/wrapper/gradle-wrapper.properties', 'gradle/wrapper/gradle-wrapper.jar',
            'LICENSE', 'NOTICE', 'AI-GENERATED.md', 'scripts/source-files.ps1')) {
        if (-not $names.Contains($required)) { throw "Archive is missing rebuild input: $required" }
    }
    $actual = Get-ExtraBotanySourceTreeDigest -FileHashes $hashes.ToArray()
    if ($actual -cne $buildInfo['Source-Tree-SHA256'] -or $hashes.Count -ne [int]$buildInfo['Source-File-Count']) {
        throw "Source archive mismatch: declared $($buildInfo['Source-Tree-SHA256']) / $($buildInfo['Source-File-Count']); actual $actual / $($hashes.Count)."
    }
    [pscustomobject]@{ ArchivePath = $path; ModVersion = $buildInfo['Mod-Version']; SourceFileCount = $hashes.Count; SourceTreeSHA256 = $actual; Verification = 'PASS: archive independently reproduces BUILD-INFO source tree hash' }
} finally { $archive.Dispose() }
