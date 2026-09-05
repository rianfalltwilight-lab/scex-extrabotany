param([string]$ProjectRoot = '')

$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot 'source-files.ps1')
if ([string]::IsNullOrWhiteSpace($ProjectRoot)) { $ProjectRoot = Join-Path $PSScriptRoot '..' }
$root = (Resolve-Path -LiteralPath $ProjectRoot).Path
$source = @(Get-ExtraBotanySourceFiles -ProjectRoot $root)

foreach ($required in @('LICENSE', 'NOTICE', 'README.md', 'README_en.md', 'AI-GENERATED.md',
        'gradlew', 'gradlew.bat', 'gradle/wrapper/gradle-wrapper.jar',
        'scripts/source-files.ps1', 'scripts/package-source.ps1',
        'scripts/verify-source-package.ps1', 'scripts/prepare-release.ps1',
        'docs/RELEASING.md')) {
    if (-not (Test-Path -LiteralPath (Join-Path $root $required) -PathType Leaf)) {
        throw "Required public source file is missing: $required"
    }
}

$self = (Join-Path $PSScriptRoot 'verify-public-source.ps1')
$textExtensions = @('.cfg', '.gradle', '.groovy', '.java', '.json', '.kts', '.md', '.mcmeta',
    '.properties', '.ps1', '.py', '.sh', '.toml', '.txt', '.yaml', '.yml')
$rules = @(
    [pscustomobject]@{ Name = 'local machine path'; Pattern = '(?i)(?:C|D|E):[\\/]' },
    [pscustomobject]@{ Name = 'GitHub credential'; Pattern = '(?:github_pat_[A-Za-z0-9_]{20,}|gh[pousr]_[A-Za-z0-9]{20,})' },
    [pscustomobject]@{ Name = 'cloud/API credential'; Pattern = '(?:AKIA[0-9A-Z]{16}|sk-[A-Za-z0-9_-]{20,}|xox[baprs]-[A-Za-z0-9-]{10,})' },
    [pscustomobject]@{ Name = 'private key'; Pattern = '-----BEGIN (?:RSA |EC |OPENSSH |DSA )?PRIVATE KEY-----' },
    [pscustomobject]@{ Name = 'fundraising text'; Pattern = '(?i)(?:sponsor|donat(?:e|ion)|patreon|ko-fi|buy me a coffee|afdian|爱发电|赞助|捐赠|捐助)' }
)
$findings = [System.Collections.Generic.List[string]]::new()
foreach ($file in $source) {
    if ($file.FullName -ceq $self -or [System.IO.Path]::GetExtension($file.FullName).ToLowerInvariant() -notin $textExtensions) { continue }
    $content = [System.IO.File]::ReadAllText($file.FullName)
    foreach ($rule in $rules) {
        if ($content -match $rule.Pattern) { $findings.Add("$($rule.Name): $($file.Relative)") }
    }
}
if ($findings.Count -gt 0) { throw "Public-source audit failed:`n$($findings -join [Environment]::NewLine)" }

$large = @($source | Where-Object { (Get-Item -LiteralPath $_.FullName).Length -gt 50MB })
if ($large.Count -gt 0) { throw "Source files exceed 50 MiB: $($large.Relative -join ', ')" }

[pscustomobject]@{
    ProjectRoot = $root
    SourceFiles = $source.Count
    Findings = 0
    LargeFiles = 0
    Verification = 'PASS: public source inventory, provenance, local-path, credential, and content gates'
}
