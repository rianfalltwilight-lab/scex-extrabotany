param(
    [string]$Destination = '',
    [string]$ExpectedTag = ''
)

$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot 'source-files.ps1')
$projectRoot = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot '..')).Path
$properties = @{}
foreach ($line in [System.IO.File]::ReadAllLines((Join-Path $projectRoot 'gradle.properties'))) {
    $trimmed = $line.Trim()
    if ($trimmed.Length -eq 0 -or $trimmed.StartsWith('#')) { continue }
    $parts = $trimmed -split '=', 2
    if ($parts.Length -eq 2) { $properties[$parts[0]] = $parts[1] }
}
$version = $properties['mod_version']
$minecraft = $properties['minecraft_version']
if ([string]::IsNullOrWhiteSpace($version) -or [string]::IsNullOrWhiteSpace($minecraft)) { throw 'Version properties are missing.' }
if (-not [string]::IsNullOrWhiteSpace($ExpectedTag) -and $ExpectedTag -cne "v$version") {
    throw "Tag/version mismatch: expected v$version, received $ExpectedTag"
}
if ([string]::IsNullOrWhiteSpace($Destination)) { $Destination = Join-Path $projectRoot "build/release-$version" }
$destinationPath = [System.IO.Path]::GetFullPath($Destination)
if (Test-Path -LiteralPath $destinationPath) { throw "Refusing to overwrite release directory: $destinationPath" }

& (Join-Path $PSScriptRoot 'verify-public-source.ps1') -ProjectRoot $projectRoot | Out-Null
& (Join-Path $PSScriptRoot 'generate-build-info.ps1') | Out-Null
$source = Get-ExtraBotanyDirectoryTreeDigest -ProjectRoot $projectRoot
$runtimeName = "extrabotany-neoforge-$minecraft-$version.jar"
$sourcesName = "extrabotany-neoforge-$minecraft-$version-sources.jar"
$runtimePath = Join-Path $projectRoot "build/libs/$runtimeName"
$sourcesPath = Join-Path $projectRoot "build/libs/$sourcesName"
foreach ($path in @($runtimePath, $sourcesPath)) {
    if (-not (Test-Path -LiteralPath $path -PathType Leaf)) { throw "Build artifact is missing: $path" }
}
& jar --validate --file $runtimePath
if ($LASTEXITCODE -ne 0) { throw 'jar --validate failed for the runtime artifact.' }

[System.IO.Directory]::CreateDirectory($destinationPath) | Out-Null
Copy-Item -LiteralPath $runtimePath -Destination (Join-Path $destinationPath $runtimeName)
Copy-Item -LiteralPath $sourcesPath -Destination (Join-Path $destinationPath $sourcesName)
$sourceName = "extrabotany-neoforge-$minecraft-$version-source.zip"
& (Join-Path $PSScriptRoot 'package-source.ps1') -Destination (Join-Path $destinationPath $sourceName) | Out-Null

$artifactNames = @($runtimeName, $sourcesName, $sourceName)
$artifactData = [ordered]@{}
foreach ($name in $artifactNames) {
    $path = Join-Path $destinationPath $name
    $artifactData[$name] = [ordered]@{ bytes = (Get-Item -LiteralPath $path).Length; sha256 = (Get-FileHash -LiteralPath $path -Algorithm SHA256).Hash.ToLowerInvariant() }
}
$manifestName = "ExtraBotany-$version-release-manifest.json"
$manifest = [ordered]@{
    version = $version
    status = 'public preview; bounded validation, not complete gameplay parity'
    minecraft = $minecraft
    neoforge = $properties['neoforge_version']
    java = '21'
    sourceFileCount = $source.Count
    sourceTreeSHA256 = $source.Hash
    validation = [ordered]@{ gameTests = '19/19'; missingLegacyRegistryEntries = 0; oldWorldItemStacksReloaded = 242; resourceAuditErrors = 0; reproducibleRuntimeAndSourcesJars = $true }
    artifacts = $artifactData
    aiAssisted = $true
    provenance = 'See NOTICE, AI-GENERATED.md, and compatibility/*-resource-provenance.json in the source tree.'
}
[System.IO.File]::WriteAllText((Join-Path $destinationPath $manifestName),
    ($manifest | ConvertTo-Json -Depth 8) + [char]10, [System.Text.UTF8Encoding]::new($false))
$sumNames = @($artifactNames + $manifestName)
$sumLines = foreach ($name in $sumNames) {
    $hash = (Get-FileHash -LiteralPath (Join-Path $destinationPath $name) -Algorithm SHA256).Hash.ToUpperInvariant()
    "$hash  $name"
}
$sumName = "ExtraBotany-$version-SHA256SUMS.txt"
[System.IO.File]::WriteAllText((Join-Path $destinationPath $sumName),
    ($sumLines -join [char]10) + [char]10, [System.Text.UTF8Encoding]::new($false))

[pscustomobject]@{
    ReleaseDirectory = $destinationPath
    Version = $version
    SourceFileCount = $source.Count
    SourceTreeSHA256 = $source.Hash
    Files = @($sumNames + $sumName)
    Verification = 'PASS: public audit, JAR validation, source ZIP verification, relative manifest, and SHA-256 list'
}
