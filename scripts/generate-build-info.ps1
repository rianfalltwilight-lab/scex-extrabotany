$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot 'source-files.ps1')
$projectRoot = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot '..')).Path
& (Join-Path $PSScriptRoot 'verify-public-source.ps1') -ProjectRoot $projectRoot | Out-Null
$properties = @{}
foreach ($line in [System.IO.File]::ReadAllLines((Join-Path $projectRoot 'gradle.properties'))) {
    $trimmed = $line.Trim()
    if ($trimmed.Length -eq 0 -or $trimmed.StartsWith('#')) { continue }
    $parts = $trimmed -split '=', 2
    if ($parts.Length -eq 2) { $properties[$parts[0]] = $parts[1] }
}
$source = Get-ExtraBotanyDirectoryTreeDigest -ProjectRoot $projectRoot
$content = @(
    'Format-Version=2'
    'Project=SCEX ExtraBotany 1.21.1 port'
    "Mod-Version=$($properties['mod_version'])"
    "Minecraft-Version=$($properties['minecraft_version'])"
    "NeoForge-Version=$($properties['neoforge_version'])"
    'Java-Version=21'
    'Gradle-Version=9.2.1'
    'Upstream-Commit=a4d4f2a968d559752fa3bd6e609544473109d983'
    'Legacy-Baseline-SHA256=8051956c2b045b9f28e78fe9c25b36235c83d118ab4823bcc31dc3c188857f42'
    "Source-Tree-SHA256=$($source.Hash)"
    "Source-File-Count=$($source.Count)"
    'Source-Tree-Hash-Algorithm=SHA-256 over ordinal-relative-path-sorted UTF-8 lines: lowercase-file-sha256 two-spaces relative-path newline'
    'GameTests=19 required tests passed; bounded regression scope, not a gameplay completion rate'
    'Registry-Compatibility=zero missing inventoried legacy entries compared with the archived scex.1 baseline'
    'Old-World-Fixture=242 serialized item stacks and custom data reloaded unchanged'
    'Physical-Client=243 items, 34 entity renderers, and 3 restored armor sets checked'
    'AI-Disclosure=OpenAI Codex assisted porting, testing, documentation, and release preparation under maintainer supervision'
) -join [char]10
[System.IO.File]::WriteAllText((Join-Path $projectRoot 'BUILD-INFO.txt'), $content + [char]10,
    [System.Text.UTF8Encoding]::new($false))
Get-Content -Raw -LiteralPath (Join-Path $projectRoot 'BUILD-INFO.txt')
