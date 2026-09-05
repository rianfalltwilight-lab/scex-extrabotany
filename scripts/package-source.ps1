param([string]$Destination = '')

$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot 'source-files.ps1')
Add-Type -AssemblyName System.IO.Compression
Add-Type -AssemblyName System.IO.Compression.FileSystem
$projectRoot = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot '..')).Path
if ([string]::IsNullOrWhiteSpace($Destination)) {
    $Destination = Join-Path $projectRoot 'build/release/extrabotany-neoforge-1.21.1-source.zip'
}
$destinationPath = [System.IO.Path]::GetFullPath($Destination)
if (Test-Path -LiteralPath $destinationPath) { throw "Refusing to overwrite source archive: $destinationPath" }
& (Join-Path $PSScriptRoot 'verify-public-source.ps1') -ProjectRoot $projectRoot | Out-Null
$buildInfoPath = Join-Path $projectRoot 'BUILD-INFO.txt'
$buildInfo = Read-ExtraBotanyBuildInfo -Content ([System.IO.File]::ReadAllText($buildInfoPath))
$source = Get-ExtraBotanyDirectoryTreeDigest -ProjectRoot $projectRoot
if ($source.Hash -cne $buildInfo['Source-Tree-SHA256'] -or $source.Count -ne [int]$buildInfo['Source-File-Count']) {
    throw 'Source inputs changed after BUILD-INFO.txt was generated.'
}
[System.IO.Directory]::CreateDirectory([System.IO.Path]::GetDirectoryName($destinationPath)) | Out-Null
$output = [System.IO.FileStream]::new($destinationPath, [System.IO.FileMode]::CreateNew,
    [System.IO.FileAccess]::Write, [System.IO.FileShare]::None)
try {
    $archive = [System.IO.Compression.ZipArchive]::new($output, [System.IO.Compression.ZipArchiveMode]::Create,
        $true, [System.Text.Encoding]::UTF8)
    try {
        $files = [System.Collections.Generic.SortedDictionary[string,string]]::new([System.StringComparer]::Ordinal)
        foreach ($file in $source.Files) { $files.Add($file.Relative, $file.FullName) }
        $files.Add('BUILD-INFO.txt', $buildInfoPath)
        foreach ($relative in $files.Keys) {
            $entry = $archive.CreateEntry($relative, [System.IO.Compression.CompressionLevel]::Optimal)
            $entry.LastWriteTime = [System.DateTimeOffset]::new(2000, 1, 1, 0, 0, 0, [System.TimeSpan]::Zero)
            $entry.ExternalAttributes = 0
            $input = [System.IO.File]::OpenRead($files[$relative])
            try {
                $target = $entry.Open()
                try { $input.CopyTo($target) } finally { $target.Dispose() }
            } finally { $input.Dispose() }
        }
    } finally { $archive.Dispose() }
} finally { $output.Dispose() }
& (Join-Path $PSScriptRoot 'verify-source-package.ps1') -ArchivePath $destinationPath
