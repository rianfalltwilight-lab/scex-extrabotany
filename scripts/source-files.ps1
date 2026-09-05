# Shared source inventory for hashing, packaging, and independent ZIP verification.
# BUILD-INFO.txt is packaged separately and excluded from its own tree hash.
$script:ExtraBotanySourceTopFiles = @(
    '.gitattributes', '.gitignore', 'AI-GENERATED.md', 'AUDIT_REVIEW.md',
    'authorization.png', 'build.gradle', 'contributors.properties', 'crowdin.yml',
    'FULL_LEGACY_RESTORE.md', 'gradle.lockfile', 'gradle.properties', 'gradlew',
    'gradlew.bat', 'LEGACY_CANDY_COMPAT.md', 'LICENSE', 'NOTICE', 'PORTING.md',
    'README.md', 'README_en.md', 'settings.gradle'
)
$script:ExtraBotanySourceDirectories = @(
    '.github', 'audit-full', 'compatibility', 'docs', 'Fabric', 'Forge',
    'gradle', 'scripts', 'src', 'Xplat'
)

function Test-ExtraBotanySourceRelativePath {
    param([Parameter(Mandatory = $true)][string]$RelativePath)
    if ($RelativePath -match '[\\:\x00-\x1f]' -or $RelativePath.StartsWith('/')) { return $false }
    $segments = $RelativePath.Split('/')
    if (@($segments | Where-Object { $_ -in @('', '.', '..') -or $_ -match '[. ]$' }).Count) { return $false }
    if ($segments.Count -eq 1) { return $RelativePath -cin $script:ExtraBotanySourceTopFiles }
    if ($segments[0] -cnotin $script:ExtraBotanySourceDirectories) { return $false }
    foreach ($segment in $segments[0..($segments.Length - 2)]) {
        if ($segment -in @('.git', '.gradle', '.cache', 'build', 'deliverables', 'evidence',
                'logs', 'out', 'run', 'run-data', '__pycache__') -or $segment -like 'run-*') {
            return $false
        }
    }
    return $segments[-1] -notmatch '(?i)\.(?:log|pyc)$'
}

function Get-ExtraBotanySourceFiles {
    param([Parameter(Mandatory = $true)][string]$ProjectRoot)
    $root = (Resolve-Path -LiteralPath $ProjectRoot).Path.TrimEnd([char[]]'\/')
    $prefix = $root + [System.IO.Path]::DirectorySeparatorChar
    $files = [System.Collections.Generic.SortedDictionary[string,object]]::new([System.StringComparer]::Ordinal)
    $pending = [System.Collections.Generic.Stack[string]]::new()
    foreach ($name in $script:ExtraBotanySourceTopFiles + $script:ExtraBotanySourceDirectories) {
        $path = Join-Path $root $name
        if (Test-Path -LiteralPath $path) { $pending.Push($path) }
    }
    while ($pending.Count -gt 0) {
        $item = Get-Item -LiteralPath $pending.Pop() -Force
        if (-not $item.FullName.StartsWith($prefix, [System.StringComparison]::OrdinalIgnoreCase)) {
            throw "Source path is outside the project root: $($item.FullName)"
        }
        if ($item.Attributes -band [System.IO.FileAttributes]::ReparsePoint) {
            throw "Linked source path is not permitted: $($item.FullName)"
        }
        $relative = $item.FullName.Substring($prefix.Length).Replace('\', '/')
        if ($item.PSIsContainer) {
            if (Test-ExtraBotanySourceRelativePath -RelativePath ($relative + '/source-probe.txt')) {
                foreach ($child in Get-ChildItem -LiteralPath $item.FullName -Force) { $pending.Push($child.FullName) }
            }
        } elseif (Test-ExtraBotanySourceRelativePath -RelativePath $relative) {
            $files.Add($relative, [pscustomobject]@{ Relative = $relative; FullName = $item.FullName })
        }
    }
    return $files.Values
}

function Get-ExtraBotanyStreamSHA256 {
    param([Parameter(Mandatory = $true)][System.IO.Stream]$Stream)
    $sha256 = [System.Security.Cryptography.SHA256]::Create()
    try { return ([System.BitConverter]::ToString($sha256.ComputeHash($Stream))).Replace('-', '').ToLowerInvariant() }
    finally { $sha256.Dispose() }
}

function Get-ExtraBotanySourceTreeDigest {
    param([Parameter(Mandatory = $true)][object[]]$FileHashes)
    $lines = [System.Collections.Generic.SortedDictionary[string,string]]::new([System.StringComparer]::Ordinal)
    foreach ($file in $FileHashes) {
        if (-not (Test-ExtraBotanySourceRelativePath -RelativePath $file.Relative) -or $file.Hash -cnotmatch '^[0-9a-f]{64}$') {
            throw "Invalid source digest input: $($file.Relative)"
        }
        $lines.Add($file.Relative, "$($file.Hash)  $($file.Relative)" + [char]10)
    }
    $bytes = [System.Text.Encoding]::UTF8.GetBytes(($lines.Values -join ''))
    $stream = [System.IO.MemoryStream]::new($bytes, $false)
    try { return Get-ExtraBotanyStreamSHA256 -Stream $stream }
    finally { $stream.Dispose() }
}

function Get-ExtraBotanyDirectoryTreeDigest {
    param([Parameter(Mandatory = $true)][string]$ProjectRoot)
    $files = @(Get-ExtraBotanySourceFiles -ProjectRoot $ProjectRoot)
    $hashes = @($files | ForEach-Object {
        [pscustomobject]@{ Relative = $_.Relative; Hash = (Get-FileHash -LiteralPath $_.FullName -Algorithm SHA256).Hash.ToLowerInvariant() }
    })
    return [pscustomobject]@{ Hash = (Get-ExtraBotanySourceTreeDigest -FileHashes $hashes); Count = $files.Count; Files = $files }
}

function Read-ExtraBotanyBuildInfo {
    param([Parameter(Mandatory = $true)][string]$Content)
    $values = @{}
    foreach ($line in ($Content -split '\r?\n')) {
        if ([string]::IsNullOrWhiteSpace($line)) { continue }
        $parts = $line -split '=', 2
        if ($parts.Length -ne 2 -or $values.ContainsKey($parts[0])) { throw "Invalid or duplicate BUILD-INFO field: $line" }
        $values.Add($parts[0], $parts[1])
    }
    if ($values['Format-Version'] -ne '2' -or $values['Source-Tree-SHA256'] -cnotmatch '^[0-9a-f]{64}$' -or
        $values['Source-File-Count'] -notmatch '^[1-9][0-9]*$') {
        throw 'BUILD-INFO.txt is missing required format, hash, or file-count fields.'
    }
    return $values
}
