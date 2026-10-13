"""Read-only packaged resource / bytecode audit; run with Python 3 from repo root."""
import collections
import hashlib
import json
from pathlib import Path
import re
import struct
import sys
import zipfile
import zlib

root = Path(__file__).resolve().parents[1]
jar = Path(sys.argv[1]) if len(sys.argv) > 1 else next(
    p for p in sorted((root / 'build/libs').glob('*.jar')) if not p.name.endswith('-sources.jar'))
errors = []
counts = collections.Counter()
def require(condition, message):
    if not condition:
        errors.append(message)

with zipfile.ZipFile(jar) as z:
    names = z.namelist()
    files = {n: z.read(n) for n in names if not n.endswith('/')}
    require(len(names) == len(set(names)), 'Duplicate JAR entries')
    require(z.testzip() is None, 'ZIP CRC failure')
    parsed = {}
    png_sizes = {}
    for n, data in files.items():
        counts[Path(n).suffix] += 1
        if n.endswith('.json') or n.endswith('.mcmeta'):
            try:
                parsed[n] = json.loads(data)
            except Exception as ex:
                errors.append(f'{n}: {ex}')
        if n.endswith('.class'):
            require(data[:4] == b'\xca\xfe\xba\xbe' and struct.unpack('>H', data[6:8])[0] == 65, f'Not Java 21: {n}')
        if n.endswith('.png'):
            require(data[:8] == b'\x89PNG\r\n\x1a\n', f'Bad PNG: {n}')
            png_sizes[n] = struct.unpack('>II', data[16:24])
            offset = 8
            while offset < len(data):
                if offset + 12 > len(data):
                    errors.append(f'Truncated PNG chunk: {n} at {offset}')
                    break
                size = struct.unpack('>I', data[offset:offset+4])[0]
                if offset + size + 12 > len(data):
                    errors.append(f'Invalid PNG chunk length: {n} at {offset}')
                    break
                chunk = data[offset+4:offset+8+size]
                crc = struct.unpack('>I', data[offset+8+size:offset+12+size])[0]
                require(zlib.crc32(chunk) == crc, f'PNG chunk CRC: {n}')
                offset += size + 12
                if chunk[:4] == b'IEND':
                    break
        if n.endswith('.ogg'):
            require(data[:4] == b'OggS', f'Bad OGG: {n}')
        if n.startswith('data/'):
            require(not re.search(r'^data/[^/]+/(recipes|loot_tables|advancements|structures|tags/(items|blocks|entity_types|fluids))/', n), f'Legacy path: {n}')
            require(not n.startswith('data/forge/'), f'Legacy namespace: {n}')
        if n.endswith('.json'):
            require(b'forge:' not in data, f'Legacy reference: {n}')

    for required in ['LICENSE', 'META-INF/neoforge.mods.toml', 'META-INF/accesstransformer.cfg',
                     'extrabotany_xplat.mixins.json', 'extrabotany_neoforge.mixins.json']:
        require(required in files, f'Missing {required}')
    require(b'${' not in files['META-INF/neoforge.mods.toml'], 'Unexpanded metadata')
    for n in ('extrabotany_xplat.mixins.json', 'extrabotany_neoforge.mixins.json'):
        cfg = parsed[n]
        for mixin in cfg.get('mixins', []) + cfg.get('client', []):
            require((cfg['package'] + '.' + mixin).replace('.', '/') + '.class' in files, f'Missing mixin {mixin}')
    def local_ref(ref, kind, suffix, source):
        if isinstance(ref, str) and ref.startswith('extrabotany:'):
            target = 'assets/extrabotany/' + kind + '/' + ref.split(':', 1)[1] + suffix
            require(target in files, f'{source} -> {target}')
    def model_refs(obj, source):
        if isinstance(obj, dict):
            for k, v in obj.items():
                if k in ('model', 'parent'):
                    local_ref(v, 'models', '.json', source)
                if k == 'textures' and isinstance(v, dict):
                    for ref in v.values():
                        local_ref(ref, 'textures', '.png', source)
                model_refs(v, source)
        elif isinstance(obj, list):
            for v in obj:
                model_refs(v, source)
    for n, obj in parsed.items():
        if '/models/' in n or '/blockstates/' in n:
            counts['models_and_blockstates'] += 1
            model_refs(obj, n)
        if n.endswith('.png.mcmeta') and 'animation' in obj:
            w, h = png_sizes[n[:-7]]
            a = obj['animation']
            fw = a.get('width', a.get('height', min(w, h)))
            fh = a.get('height', a.get('width', min(w, h)))
            require(w % fw == 0 and h % fh == 0, f'Animation dimensions {n}')
            for frame in a.get('frames', []):
                i = frame if isinstance(frame, int) else frame['index']
                require(0 <= i < (w // fw) * (h // fh), f'Animation frame {n}: {i}')
    for event in parsed['assets/extrabotany/sounds.json'].values():
        for sound in event.get('sounds', []):
            if isinstance(sound, str):
                local_ref(sound, 'sounds', '.ogg', 'sounds.json')
            elif sound.get('type', 'file') == 'file':
                local_ref(sound['name'], 'sounds', '.ogg', 'sounds.json')
    en = parsed['assets/extrabotany/lang/en_us.json']
    zh = parsed['assets/extrabotany/lang/zh_cn.json']
    require(en.keys() == zh.keys(), f'Language key mismatch: {set(en) ^ set(zh)}')
    counts['language_keys'] = len(en)
    resource_roots = [root / p for p in ['Xplat/src/main/resources', 'Forge/src/main/resources', 'src/generated/resources']]
    paths = collections.Counter(p.relative_to(r).as_posix() for r in resource_roots for p in r.rglob('*')
                                if p.is_file() and '.cache' not in p.relative_to(r).parts)
    require(all(c == 1 for c in paths.values()), f'Duplicate source resources: {[p for p,c in paths.items() if c > 1]}')
    counts['source_resource_entries'] = len(paths)

result = dict(jar=str(jar.resolve()), bytes=jar.stat().st_size,
              sha256=hashlib.sha256(jar.read_bytes()).hexdigest(), counts=dict(counts), errors=errors)
print(json.dumps(result, ensure_ascii=False, indent=2))
sys.exit(bool(errors))
