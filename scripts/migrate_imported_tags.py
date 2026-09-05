"""Migrate only hash-verified imported scex.1 plural tag paths to 1.21 singular paths."""
import hashlib
import json
from pathlib import Path

root = Path(__file__).resolve().parents[1]
provenance_path = root / 'compatibility/full-resource-provenance.json'
provenance = json.loads(provenance_path.read_text(encoding='utf-8'))
resource_roots = [root / folder for folder in ('Xplat/src/main/resources', 'Forge/src/main/resources', 'src/generated/resources')]
renames = {'blocks': 'block', 'items': 'item', 'entity_types': 'entity_type', 'fluids': 'fluid'}
records = provenance.setdefault('path_migrations', {})
for name, record in list(provenance['files'].items()):
    parts = name.split('/')
    if len(parts) < 5 or parts[0] != 'data' or parts[2] != 'tags' or parts[3] not in renames:
        continue
    source = resource_roots[0] / name
    if not source.exists():
        continue
    assert hashlib.sha256(source.read_bytes()).hexdigest() == record['sha256'], f'Edited source: {source}'
    parts[3] = renames[parts[3]]
    destination_name = '/'.join(parts)
    destination = next((folder / destination_name for folder in resource_roots if (folder / destination_name).exists()), resource_roots[0] / destination_name)
    imported = json.loads(source.read_text(encoding='utf-8'))
    current = json.loads(destination.read_text(encoding='utf-8')) if destination.exists() else {'replace': False, 'values': []}
    values = current.setdefault('values', [])
    values.extend(value for value in imported.get('values', []) if value not in values)
    destination.parent.mkdir(parents=True, exist_ok=True)
    destination.write_text(json.dumps(current, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    records[name] = {'destination': destination_name, 'source_sha256': record['sha256']}
    source.unlink()
provenance['policy'] = 'Import absent licensed resources and absent JSON keys/tag values; migrate verified plural tag paths to 1.21 singular paths. Retain all pre-existing port fixes.'
registry_renames = {'botania:redstone_spreader': 'botania:pulse_mana_spreader', 'botania:elven_spreader': 'botania:elven_mana_spreader',
                    'botania:gaia_spreader': 'botania:gaia_mana_spreader', 'botania:creative_pool': 'botania:creative_mana_pool',
                    'botania:diluted_pool': 'botania:diluted_mana_pool', 'botania:fabulous_pool': 'botania:fabulous_mana_pool'}
for migrated in records.values():
    destination = next((folder / migrated['destination'] for folder in resource_roots if (folder / migrated['destination']).exists()), None)
    if destination is None:
        continue
    current = json.loads(destination.read_text(encoding='utf-8'))
    values = []
    for value in current.get('values', []):
        replacement = registry_renames.get(value, value) if isinstance(value, str) else value
        if replacement not in values:
            values.append(replacement)
    current['values'] = values
    destination.write_text(json.dumps(current, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
provenance['botania_registry_renames'] = registry_renames
provenance_path.write_text(json.dumps(provenance, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
print(f'Migrated {len(records)} archived tag paths.')
