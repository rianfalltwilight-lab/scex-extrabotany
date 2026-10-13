"""Compare evidence captured by the isolated audit mod, not guessed resource names."""
import hashlib
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
old_path = ROOT / 'audit-full/runs/scex1/registry-dump.json'
new_path = ROOT / 'audit-full/runs/scex4/registry-dump.json'
old, new = (json.loads(p.read_text(encoding='utf-8')) for p in (old_path, new_path))
report = {'baseline': 'scex.1 runtime vs frozen scex.4 runtime',
          'evidence_sha256': {str(p.relative_to(ROOT)): hashlib.sha256(p.read_bytes()).hexdigest()
                              for p in (old_path, new_path)}, 'registries': {}}
for registry in sorted(old.keys() | new.keys()):
    before, after = old.get(registry, {}), new.get(registry, {})
    missing = {key: before[key] for key in sorted(before.keys() - after.keys())}
    added = {key: after[key] for key in sorted(after.keys() - before.keys())}
    report['registries'][registry] = {'old_count': len(before), 'scex4_count': len(after),
                                     'missing': missing, 'added': added}
    if missing or added:
        print(f'{registry}: old={len(before)} scex4={len(after)} missing={len(missing)} added={len(added)}')
(ROOT / 'compatibility/runtime-registry-baseline.json').write_text(
    json.dumps(report, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
