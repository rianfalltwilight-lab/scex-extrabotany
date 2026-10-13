"""Freeze the verified Gaia III equipment-disarm + livingrock barrel release."""
import hashlib
import json
import os
from pathlib import Path
import shutil
import zipfile

ROOT = Path(__file__).resolve().parents[1]
COMPAT = ROOT.parent / 'scex-botania-extrabotany-compat-scex6'
COMPAT_BUILD = Path(os.environ['TEMP']) / 'scex-compat6-build'
OUT = ROOT / 'deliverables/scex.6-gaia-disarm-barrel-final'
VERSION = '2.0-scex.6-dev'

def digest(path):
    return {'bytes': path.stat().st_size, 'sha256': hashlib.sha256(path.read_bytes()).hexdigest()}

def read_json(path):
    return json.loads(path.read_text(encoding='utf-8-sig'))

def write_json(path, value):
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')

def archive_sources(root, out, folders, root_files):
    paths = [root / name for name in root_files if (root / name).is_file()]
    for folder in folders:
        for directory, dirs, files in os.walk(root / folder):
            dirs[:] = [name for name in dirs if name not in {'.git', '.gradle', 'build', 'run', '.cache', '__pycache__', 'out'}]
            paths.extend(Path(directory) / name for name in files if not name.endswith(('.pyc', '.log', '.lock')))
    paths = sorted(set(paths), key=lambda path: path.relative_to(root).as_posix())
    manifest = {p.relative_to(root).as_posix(): digest(p) for p in paths}
    with zipfile.ZipFile(out, 'w', zipfile.ZIP_DEFLATED, compresslevel=9) as archive:
        for path in paths:
            info = zipfile.ZipInfo(path.relative_to(root).as_posix(), (2026, 9, 5, 0, 0, 0))
            info.compress_type = zipfile.ZIP_DEFLATED
            archive.writestr(info, path.read_bytes())
        archive.writestr('SOURCE_MANIFEST.json', json.dumps(manifest, ensure_ascii=False, indent=2))
    with zipfile.ZipFile(out) as archive:
        assert archive.testzip() is None
        for name, expected in manifest.items():
            assert hashlib.sha256(archive.read(name)).hexdigest() == expected['sha256']
    return manifest

assert not OUT.exists(), 'Frozen destination already exists; do not overwrite.'
before = read_json(ROOT / 'audit-full/gaia6-final-before.json')
after = read_json(ROOT / 'audit-full/gaia6-final-after.json')
assert before == after, 'Repeated builds differ'
for artifact in after:
    assert digest(ROOT / 'build/libs' / artifact['name'])['sha256'].upper() == artifact['Hash']
assert 'All 24 required tests passed' in (ROOT / 'audit-full/gaia6-barrel-build.log').read_text(encoding='utf-8')
assert not read_json(ROOT / 'audit-full/gaia6-final-resource.json')['errors']
client = (COMPAT / 'compat6-client-final.log').read_text(encoding='utf-8')
assert client.count('SCEX_WAND_ASSERT_PASS') == 7
assert 'SCEX_WAND_CLIENT_TEST_PASS' in client and 'SCEX_WAND_CLIENT_TEST_FAIL' not in client
assert '2.0-scex.6-dev' in client and '1.4.0' in client
test_xml = COMPAT_BUILD / 'test-results/test/TEST-dev.scex.compat.botania.CompatibilityContractTest.xml'
assert 'tests="2"' in test_xml.read_text() and 'failures="0"' in test_xml.read_text()
old_jar = ROOT / 'deliverables/scex.5-full-legacy-restore-final/extrabotany-neoforge-1.21.1-2.0-scex.5-dev.jar'
assert digest(old_jar)['sha256'] == '8d385776211f1a449f1c7c25c480286c1cb46c8d050145b9d5c7014216a6218a'
old_compat = Path('D:/Minecraft-Projects/scex-botania-extrabotany-compat/artifacts/SCEX-Botania-ExtraBotany-Compat-1.21.1-1.3.0.jar')
compat_jar = COMPAT_BUILD / 'libs/SCEX-Botania-ExtraBotany-Compat-1.21.1-1.4.0.jar'
def classes(path):
    with zipfile.ZipFile(path) as jar:
        return {name: jar.read(name) for name in jar.namelist() if name.endswith('.class')}
assert classes(old_compat) == classes(compat_jar), 'Compatibility release Java changed unexpectedly'
assert not any('testharness' in name for name in classes(compat_jar))

OUT.mkdir(parents=True)
for artifact in after:
    shutil.copyfile(ROOT / 'build/libs' / artifact['name'], OUT / artifact['name'])
shutil.copyfile(compat_jar, OUT / compat_jar.name)
shutil.copyfile(ROOT / 'GAIA_DISARM_SCEX6.md', OUT / 'GAIA_DISARM_SCEX6.md')
evidence = OUT / 'evidence'; evidence.mkdir()
for name in ['gaia6-barrel-build.log', 'gaia6-final-repro.log', 'gaia6-final-before.json', 'gaia6-final-after.json', 'gaia6-final-resource.json']:
    shutil.copyfile(ROOT / 'audit-full' / name, evidence / name)
for name in ['compat6-build02.log', 'compat6-client-final.log']:
    shutil.copyfile(COMPAT / name, evidence / name)
shutil.copyfile(test_xml, evidence / test_xml.name)
source_name = 'extrabotany-neoforge-1.21.1-scex.6-dev-source-20260905-gaia-barrel.zip'
root_files = ['build.gradle', 'settings.gradle', 'gradle.properties', 'gradle.lockfile', 'gradlew', 'gradlew.bat', 'LICENSE',
    'README.md', 'README_en.md', 'PORTING.md', 'AUDIT_REVIEW.md', 'LEGACY_CANDY_COMPAT.md', 'FULL_LEGACY_RESTORE.md',
    'GAIA_DISARM_SCEX6.md', 'contributors.properties', 'crowdin.yml', '.gitignore', '.gitattributes', 'authorization.png']
source_manifest = archive_sources(ROOT, OUT / source_name,
    ['Xplat', 'Forge', 'Fabric', 'gradle', 'src', 'scripts', 'compatibility', 'audit-full/harness'], root_files)
compat_source_name = 'SCEX-Botania-ExtraBotany-Compat-1.21.1-1.4.0-sources.zip'
archive_sources(COMPAT, OUT / compat_source_name, ['src', 'gradle', 'libs'],
    ['build.gradle', 'settings.gradle', 'gradle.properties', 'gradlew', 'gradlew.bat', 'LICENSE', '.gitignore'])
old_source = ROOT / 'deliverables/scex.5-full-legacy-restore-final/extrabotany-neoforge-1.21.1-scex.5-dev-source-20260905-full-restore.zip'
with zipfile.ZipFile(old_source) as archive:
    old_manifest = json.loads(archive.read('SOURCE_MANIFEST.json'))
changes = [name for name in source_manifest if old_manifest.get(name) != source_manifest[name]]
write_json(OUT / 'manifest.json', {
    'version': VERSION, 'compatibility_version': '1.4.0', 'production_release_approved': False,
    'artifacts': {p.name: digest(p) for p in sorted(OUT.iterdir()) if p.suffix in {'.jar', '.zip'}},
    'validation': {'gametest_passed': 24, 'gametest_total': 24, 'resources_errors': 0,
        'reproducible_runtime_and_sources_jar': True, 'physical_client_wand_assertions': 7,
        'client_save_reload_passed': True, 'compatibility_junit_passed': 2,
        'compatibility_release_classes_identical_to_1_3_0': True, 'scex5_runtime_unchanged': True,
        'client_known_errors': ['Botania README invalid path', 'Manalink legacy empty optional field: Not a map: null'],
        'not_tested': ['236-JAR full pack with scex.6', 'Each third-party ring curse side effect']},
    'source_archive_paths': ['D:/Minecraft-Projects/' + source_name, 'D:/Minecraft-Projects/' + compat_source_name],
    'changed_source_files_since_scex5': changes,
    'evidence': {p.name: digest(p) for p in sorted(evidence.iterdir())},
    'deployment': 'Back up world/config/old pair, then replace both ExtraBotany and its matching compatibility JAR; maintenance owns production acceptance.'
})
print(json.dumps(read_json(OUT / 'manifest.json'), ensure_ascii=False, indent=2))
