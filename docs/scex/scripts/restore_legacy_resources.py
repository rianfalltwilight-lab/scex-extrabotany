"""Import absent MIT legacy resources; preserve every existing port resource.

The exact production-equivalent binary is an archived read-only input. Java
decompilation is never imported by this tool. JSON merges only add absent keys.
"""
import hashlib
import json
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT.parents[1] / 'areas/mod-mainline/hotfixes/2026-09-01-gaia3-thaumic-terminal/release/[额外植物学：重燃] extrabotany-neoforge-1.21.1-2.0-scex.1-gaia3-hotfix.jar'
EXPECTED = '8051956c2b045b9f28e78fe9c25b36235c83d118ab4823bcc31dc3c188857f42'
assert hashlib.sha256(SOURCE.read_bytes()).hexdigest() == EXPECTED
DEST = ROOT / 'Xplat/src/main/resources'
ROOTS = [DEST, ROOT / 'Forge/src/main/resources', ROOT / 'src/generated/resources']
RECORD = ROOT / 'compatibility/full-resource-provenance.json'
report = json.loads(RECORD.read_text(encoding='utf-8')) if RECORD.exists() else {
    'source_jar': str(SOURCE), 'source_sha256': EXPECTED, 'license': 'MIT (retained upstream LICENSE)',
    'policy': 'Copy only absent resources, merge absent language/sound keys; never overwrite existing port content.',
    'files': {}, 'merged_keys': {}}
with zipfile.ZipFile(SOURCE) as archive:
    for name in sorted(archive.namelist()):
        if name.endswith('/') or not name.startswith(('assets/', 'data/')): continue
        if '..' in Path(name).parts: raise ValueError(name)
        if name in report.get('path_migrations', {}): continue
        existing = next((root / name for root in ROOTS if (root / name).is_file()), None)
        data = archive.read(name)
        if existing:
            if name.startswith('assets/extrabotany/lang/') or name == 'assets/extrabotany/sounds.json':
                current = json.loads(existing.read_text(encoding='utf-8'))
                legacy = json.loads(data)
                additions = {key: value for key, value in legacy.items() if key not in current}
                if additions:
                    current.update(additions)
                    existing.write_text(json.dumps(current, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
                    report['merged_keys'].setdefault(name, {}).update({key: hashlib.sha256(json.dumps(value, sort_keys=True).encode()).hexdigest() for key, value in additions.items()})
            elif name.startswith('data/') and '/tags/' in name and name.endswith('.json'):
                current = json.loads(existing.read_text(encoding='utf-8'))
                legacy = json.loads(data)
                additions = [value for value in legacy.get('values', []) if value not in current.get('values', [])]
                if additions:
                    current.setdefault('values', []).extend(additions)
                    existing.write_text(json.dumps(current, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
                    report.setdefault('merged_tag_values', {}).setdefault(name, [])
                    report['merged_tag_values'][name].extend(value for value in additions if value not in report['merged_tag_values'][name])
            continue
        target = DEST / name
        target.parent.mkdir(parents=True, exist_ok=True)
        target.write_bytes(data)
        report['files'][name] = {'bytes': len(data), 'sha256': hashlib.sha256(data).hexdigest()}
RECORD.write_text(json.dumps(report, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
print(f'Legacy resource provenance: {len(report["files"])} files, {sum(map(len, report["merged_keys"].values()))} merged keys')
