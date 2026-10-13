"""Freeze verified scex.5 artifacts into a new directory; never overwrite a previous release."""
import hashlib
import json
import os
import shutil
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
VERSION = '2.0-scex.5-dev'
OUT = ROOT / 'deliverables/scex.5-full-legacy-restore-final'
ARCHIVE_NAME = 'extrabotany-neoforge-1.21.1-scex.5-dev-source-20260905-full-restore.zip'
def digest(path):
    return {'bytes': path.stat().st_size, 'sha256': hashlib.sha256(path.read_bytes()).hexdigest()}
def write_json(path, value):
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')

assert not OUT.exists(), 'Frozen destination already exists; do not overwrite it.'
repro = json.loads((ROOT / 'audit-full/repro-after.json').read_text())
assert repro['identical']
for name, expected in repro['artifacts'].items():
    assert digest(ROOT / 'build/libs' / name) == expected
assert not json.loads((ROOT / 'audit-full/resource-audit02.json').read_text(encoding='utf-8'))['errors']
assert 'All 19 required tests passed' in (ROOT / 'audit-full/gametest-build12.log').read_text(encoding='utf-8')
assert json.loads((ROOT / 'audit-full/runs/scex1-fixture/old-fixture-seeded.json').read_text()) == json.loads((ROOT / 'audit-full/runs/scex5-fixture/old-fixture-reloaded.json').read_text())
assert (ROOT / 'audit-full/runs/client/client-render-success.json').exists()

frozen4 = ROOT / 'deliverables/scex.4-legacy-compat-final'
old_hashes = {
    'extrabotany-neoforge-1.21.1-2.0-scex.4-dev.jar': '9b55f02f6ba72b395dcb9eaa650b0b5268faaaf0e7eb530dcbe8bdffae644f35',
    'extrabotany-neoforge-1.21.1-2.0-scex.4-dev-sources.jar': '190d1a406cd06c354cab25beb8880f268ef886cb76964f979425ff04de624a0a',
    'extrabotany-neoforge-1.21.1-scex.4-dev-source-20260905-legacy-compat.zip': '97f0505877bd5020de2b0f8d60167554ea5fd7de4ae6ef665bac997b35505352',
    'manifest.json': '1cd5e8ac899f60bda1a1e43a6a1ffef06f766d97e5c58241846c54f584d6db18'}
for name, expected in old_hashes.items():
    assert digest(frozen4 / name)['sha256'] == expected, f'Prior release changed: {name}'

paths = [ROOT / 'audit-full/runs' / run / 'registry-dump.json' for run in ('scex1', 'scex4', 'scex5-fixture')]
old, previous, current = [json.loads(path.read_text()) for path in paths]
registry_report = {'baseline': 'scex.1', 'candidate': VERSION, 'evidence': {str(path.relative_to(ROOT)): digest(path) for path in paths}, 'registries': {}}
for registry in sorted(old.keys() | current.keys()):
    before, prior, after = set(old.get(registry, {})), set(previous.get(registry, {})), set(current.get(registry, {}))
    missing = sorted(before - after)
    assert not missing, (registry, missing)
    registry_report['registries'][registry] = {'old_count': len(before), 'new_count': len(after), 'missing': missing,
                                              'added': sorted(after - before), 'restored_since_scex4': sorted((before - prior) & after)}
write_json(ROOT / 'compatibility/runtime-registry-complete.json', registry_report)
write_json(ROOT / 'compatibility/full-restore-validation.json', {
    'version': VERSION, 'gametest': {'required': 19, 'passed': 19}, 'resource_audit_errors': 0,
    'old_world_items_identical': 242, 'entity_codec_roundtrips': 34,
    'physical_client': {'item_renderers': 243, 'entity_renderers': 34, 'armor_sets': 3},
    'reproducible_artifacts': repro['artifacts'], 'scex4_frozen_unchanged': old_hashes,
    'production_release_approved': False,
    'remaining_acceptance': 'Frozen scex.5 wand/full-pack integration, followed by maintenance backup/deployment acceptance.',
    'baseline_limitations': ['Archived flamescion weapon item uses its original palette texture.', 'Archived music.ego has no audio and no active caller.', 'Archived butterfly projectile has no completed hit action.'],
    'model_source': {'archive_sha256': '8051956c2b045b9f28e78fe9c25b36235c83d118ab4823bcc31dc3c188857f42',
                     'method': 'Original texture bytes and numeric mesh/UV/animation mappings; no generated replacement artwork.'}})

OUT.mkdir(parents=True)
for name in repro['artifacts']:
    shutil.copyfile(ROOT / 'build/libs' / name, OUT / name)
shutil.copyfile(ROOT / 'FULL_LEGACY_RESTORE.md', OUT / 'FULL_LEGACY_RESTORE.md')
evidence = OUT / 'evidence'
evidence.mkdir()
logs = ['gametest-build12.log', 'repro-build.log', 'repro-before.json', 'repro-after.json', 'resource-audit02.json',
        'client-render02.log', 'fixture-seed.log', 'fixture-reload01.log']
for name in logs:
    shutil.copyfile(ROOT / 'audit-full' / name, evidence / name)
for path in paths:
    shutil.copyfile(path, evidence / (path.parent.name + '-registry.json'))
for folder, filename in [('scex1-fixture', 'old-fixture-seeded.json'), ('scex5-fixture', 'old-fixture-reloaded.json'), ('client', 'client-render-success.json')]:
    shutil.copyfile(ROOT / 'audit-full/runs' / folder / filename, evidence / filename)
for path in (ROOT / 'audit-full/runs/client/screenshots').glob('full-legacy-*.png'):
    shutil.copyfile(path, evidence / path.name)
for name in ('runtime-registry-complete.json', 'full-restore-validation.json', 'full-resource-provenance.json'):
    shutil.copyfile(ROOT / 'compatibility' / name, evidence / name)

root_files = {'build.gradle', 'settings.gradle', 'gradle.properties', 'gradle.lockfile', 'gradlew', 'gradlew.bat', 'LICENSE',
              'README.md', 'README_en.md', 'PORTING.md', 'AUDIT_REVIEW.md', 'LEGACY_CANDY_COMPAT.md', 'FULL_LEGACY_RESTORE.md',
              'contributors.properties', 'crowdin.yml', '.gitignore', '.gitattributes', 'authorization.png'}
source_paths = [ROOT / name for name in root_files if (ROOT / name).is_file()]
for folder in ('Xplat', 'Forge', 'Fabric', 'gradle', 'src', 'scripts', 'compatibility', 'audit-full/harness'):
    for directory, dirs, files in os.walk(ROOT / folder):
        dirs[:] = [name for name in dirs if name not in {'.git', '.gradle', 'build', 'run', '.cache', '__pycache__', 'out'}]
        source_paths.extend(Path(directory) / name for name in files if not name.endswith(('.pyc', '.log', '.lock')))
source_paths = sorted(set(source_paths), key=lambda path: path.relative_to(ROOT).as_posix())
source_manifest = {path.relative_to(ROOT).as_posix(): digest(path) for path in source_paths}
with zipfile.ZipFile(OUT / ARCHIVE_NAME, 'w', zipfile.ZIP_DEFLATED, compresslevel=9) as archive:
    for path in source_paths:
        info = zipfile.ZipInfo(path.relative_to(ROOT).as_posix(), (2026, 9, 5, 0, 0, 0))
        info.compress_type = zipfile.ZIP_DEFLATED
        archive.writestr(info, path.read_bytes())
    info = zipfile.ZipInfo('SOURCE_MANIFEST.json', (2026, 9, 5, 0, 0, 0)); info.compress_type = zipfile.ZIP_DEFLATED
    archive.writestr(info, json.dumps(source_manifest, ensure_ascii=False, indent=2).encode())
with zipfile.ZipFile(OUT / ARCHIVE_NAME) as archive:
    assert archive.testzip() is None
    for name, expected in source_manifest.items(): assert hashlib.sha256(archive.read(name)).hexdigest() == expected['sha256']
artifacts = {path.name: digest(path) for path in OUT.iterdir() if path.is_file() and path.suffix in ('.jar', '.zip')}
manifest = {'version': VERSION, 'artifacts': artifacts, 'source_archive_path': str(Path('D:/Minecraft-Projects') / ARCHIVE_NAME),
            'source_file_count': len(source_manifest), 'validation': '19/19 GameTest; zero missing legacy registry entries; 242 old-world stacks identical; physical-client render checked; reproducible JARs.',
            'production_release_approved': False, 'report': 'FULL_LEGACY_RESTORE.md',
            'evidence_sha256': {path.name: digest(path)['sha256'] for path in sorted(evidence.iterdir())}}
write_json(OUT / 'manifest.json', manifest)
print(json.dumps(manifest, ensure_ascii=False, indent=2))
