# SCEX ExtraBotany release process

This repository treats a tag, its source tree, the validated release artifacts, and the GitHub Release as one immutable unit. A release is incomplete until all gates below pass and the public Release is verified.

## Required order

1. Confirm the intended version in `gradle.properties`, update `README.md`, `README_en.md`, and `docs/releases/<version>.md`, and inspect `git status` without discarding unrelated changes.
2. Run `scripts/verify-public-source.ps1`. It validates the explicit source inventory, required provenance files, local-machine-path gate, credential signatures, fundraising-content gate, linked paths, and oversized files.
3. Use Java 21 and the Gradle wrapper to run `build` and `runGameTestServer`. A successful compile alone is not a release gate.
4. Run `scripts/generate-build-info.ps1`, then rebuild if any build input changed. `BUILD-INFO.txt` binds the version, toolchain, source-file count, and source-tree SHA-256.
5. Run `scripts/prepare-release.ps1`. It validates the runtime JAR, creates a deterministic source ZIP, independently verifies the ZIP against `BUILD-INFO.txt`, writes a relative-path-only manifest, and creates `SHA256SUMS.txt`.
6. Compare the runtime and sources JAR against the accepted frozen candidate. Any byte difference requires a new validation cycle and must not silently reuse earlier evidence.
7. Commit only the declared source inventory and `BUILD-INFO.txt`. Keep local logs, worlds, caches, credentials, and private evidence outside the commit.
8. Create one annotated tag named exactly `v<mod_version>` and push `main` plus that tag without force.
9. The tag workflow rebuilds, re-runs GameTests, regenerates the release bundle, and creates or updates a GitHub prerelease. If automation fails, fix the cause or create the same prerelease manually from the already verified bundle; never declare completion with only a tag.
10. Read back the remote branch, peeled tag, Release state, asset names, sizes, and hashes. Retain the previous tag as the source-and-binary rollback point.

## Local commands

```powershell
$env:JAVA_HOME = '<Java 21 JDK>'
.\scripts\verify-public-source.ps1
.\gradlew.bat --no-daemon --no-configuration-cache build
.\gradlew.bat --no-daemon --no-configuration-cache runGameTestServer
.\scripts\generate-build-info.ps1
.\scripts\prepare-release.ps1 -ExpectedTag 'v2.0-scex.5-dev'
```

`prepare-release.ps1` refuses to overwrite an existing output directory. Use a new destination for each attempt so failed or partial evidence remains inspectable.

## Evidence boundaries

Release notes must distinguish unit/static checks, NeoForge GameTests, packaged-JAR checks, physical-client checks, old-world fixtures, and full-pack testing. Counts from different layers are not additive and are not a gameplay-completion percentage.

Publishing a repository or Release never deploys a production server. Production adoption requires its own player-count, backup, paired client/server update, health-check, and rollback procedure.
