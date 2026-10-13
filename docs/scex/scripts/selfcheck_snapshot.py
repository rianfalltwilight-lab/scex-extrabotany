"""Preserve the pre-audit dirty source tree without touching Git or production."""
import hashlib
import json
import subprocess
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / 'audit-selfcheck-20260921'
OUT.mkdir(exist_ok=True)
snapshot = OUT / 'before-source.zip'
assert not snapshot.exists(), 'Do not overwrite the initial source snapshot'
files = []
for name in ['Xplat', 'Forge', 'Fabric', 'src', 'gradle', 'scripts', 'compatibility', 'annotations']:
    files.extend(p for p in (ROOT / name).rglob('*') if p.is_file()
                 and not set(p.relative_to(ROOT / name).parts).intersection({'build', '.gradle', '__pycache__'}))
files.extend(p for p in ROOT.iterdir() if p.is_file())
files = sorted(set(files))
def digest(p):
    return {'bytes': p.stat().st_size, 'sha256': hashlib.sha256(p.read_bytes()).hexdigest()}
manifest = {p.relative_to(ROOT).as_posix(): digest(p) for p in files}
with zipfile.ZipFile(snapshot, 'x', zipfile.ZIP_DEFLATED) as z:
    for p in files:
        z.write(p, p.relative_to(ROOT).as_posix())
production = Path('E:/Minecarft/B-03-SCEX-LegacyGenesis/server/mods')
baseline = {
    'scope': 'Read-only production identity; dirty source preserved before audit edits',
    'source_root': str(ROOT),
    'git_head': subprocess.check_output(['git', 'rev-parse', 'HEAD'], cwd=ROOT, text=True).strip(),
    'git_status': subprocess.check_output(['git', 'status', '--short'], cwd=ROOT, text=True),
    'snapshot': digest(snapshot),
    'files': manifest,
    'production': {str(p): digest(p) for p in production.glob('*.jar') if 'botan' in p.name.lower()},
    'upstream': json.loads((ROOT / 'audit-upstream-20260916/baseline.json').read_text())['upstream'],
}
(OUT / 'baseline.json').write_text(json.dumps(baseline, ensure_ascii=False, indent=2), encoding='utf-8')
print(json.dumps({'snapshot': str(snapshot), 'files': len(files), 'identity': digest(snapshot),
                  'production': baseline['production']}, ensure_ascii=False))
