"""Freeze only the exact upstream-audited, actually tested candidate."""
import ast,hashlib,json,os,shutil,zipfile
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1];AUDIT=ROOT/'audit-upstream-20260916'
OUT=ROOT/'deliverables/scex.8-upstream-parity-20260917';BRIDGE=ROOT.parent/'scex-botania-extrabotany-compat-scex8'
assert not OUT.exists(),'Frozen output already exists'
def read(p):return json.loads(p.read_text(encoding='utf-8-sig'))
def digest(p):return dict(bytes=p.stat().st_size,sha256=hashlib.sha256(p.read_bytes()).hexdigest())
gt=AUDIT/'evidence/extra-items-upstream-final-gt-20260917';client=AUDIT/'evidence/extra-items-upstream-final-client-20260917'
assert read(gt/'result.json')['passed'] and 'All 38 required tests passed' in (gt/'process.log').read_text(encoding='utf-8')
probe=read(client/'verification/result.json');assert probe['status']=='captured' and len(probe['checks'])==366 and all(probe['checks'].values())
for run in [gt,client]:
 assert read(run/'result.json')['exit_code']==0 and not read(run/'result.json')['forced']
 assert 'All dimensions are saved' in (run/'process.log').read_text(encoding='utf-8')
assert len(list((client/'verification/screenshots').glob('upstream-page-*.png')))==9
assert read(AUDIT/'resource-audit.json')['errors']==[]
jar=ROOT/'build/libs/extrabotany-neoforge-1.21.1-2.0-scex.8-dev.jar';testjar=jar.with_name(jar.stem+'-validation.jar')
assert digest(jar)['sha256']==read(AUDIT/'resource-audit.json')['sha256']
with zipfile.ZipFile(jar) as prod,zipfile.ZipFile(testjar) as test:
 assert not any('/gametest/' in n for n in prod.namelist())
 assert all(prod.read(n)==test.read(n) for n in prod.namelist())
bridge=BRIDGE/'build/libs/SCEX-Botania-ExtraBotany-Compat-1.21.1-1.4.2.jar'
def classes(p):
 with zipfile.ZipFile(p) as z:return {n:z.read(n) for n in z.namelist() if n.endswith('.class')}
assert classes(bridge)==classes(ROOT/'deliverables/scex.7-item-use-20260912/SCEX-Botania-ExtraBotany-Compat-1.21.1-1.4.1.jar')
for run in [gt,client]:
 rows=read(run/'inputs/manifest.json')['files']
 for p in [testjar,bridge]:assert any(row['path']=='mods/'+p.name and row['sha256']==digest(p)['sha256'] for row in rows)
xml=Path(os.environ['TEMP'])/'scex-compat8-build/test-results/test/TEST-dev.scex.compat.botania.CompatibilityContractTest.xml'
assert 'tests="2"' in xml.read_text() and 'failures="0"' in xml.read_text()
OUT.mkdir();evidence=OUT/'evidence';evidence.mkdir()
for p in [jar,jar.with_name(jar.stem+'-sources.jar'),bridge]:shutil.copy2(p,OUT/p.name)
shutil.copy2(ROOT/'UPSTREAM_PARITY_SCEX8.md',OUT/'UPSTREAM_PARITY_SCEX8.md')
shutil.copytree(AUDIT/'evidence',evidence/'runs')
for p in AUDIT.iterdir():
 if p.is_file() and (p.suffix in ['.json','.txt','.py','.patch','.log'] or p.name.startswith('extra-items-evidence-') or p.name in ['upstream-master.zip','upstream-1.16.zip']):shutil.copy2(p,evidence/p.name)
shutil.copy2(xml,evidence/xml.name);shutil.copy2(testjar,evidence/testjar.name)
helper=ast.parse((ROOT/'scripts/freeze_gaia6.py').read_text(encoding='utf-8'))
node=next(n for n in helper.body if isinstance(n,ast.FunctionDef) and n.name=='archive_sources')
exec(compile(ast.Module(body=[node],type_ignores=[]),'<existing source archive helper>','exec'))
rootfiles=['build.gradle','settings.gradle','gradle.properties','gradle.lockfile','gradlew','gradlew.bat','LICENSE','README.md','README_en.md','PORTING.md','FULL_LEGACY_RESTORE.md','GAIA_DISARM_SCEX6.md','ITEM_USE_SCEX7.md','UPSTREAM_PARITY_SCEX8.md','contributors.properties','.gitignore','.gitattributes','authorization.png']
source=OUT/'extrabotany-neoforge-1.21.1-scex.8-dev-source-20260917.zip'
srcmanifest=archive_sources(ROOT,source,['Xplat','Forge','Fabric','gradle','src','scripts','compatibility'],rootfiles)
bridgezip=OUT/'SCEX-Botania-ExtraBotany-Compat-1.21.1-1.4.2-sources.zip'
archive_sources(BRIDGE,bridgezip,['src','gradle','libs'],['build.gradle','settings.gradle','gradle.properties','gradlew','gradlew.bat','LICENSE'])
for p in [source,bridgezip]:
 target=Path('D:/Minecraft-Projects')/p.name;assert not target.exists();shutil.copy2(p,target);assert digest(p)==digest(target)
manifest=dict(version='2.0-scex.8-dev',bridge_version='1.4.2',artifacts={p.name:digest(p) for p in OUT.iterdir() if p.suffix in ['.jar','.zip']},
 source_archives=['D:/Minecraft-Projects/'+p.name for p in [source,bridgezip]],upstream=read(AUDIT/'baseline.json')['upstream'],
 build=dict(minecraft='1.21.1',neoforge='21.1.248',java='21.0.12',gradle='9.2.1',moddev='2.0.141',botania='456-20260822.093314-4',curios='9.5.1+1.21.1',patchouli='1.21.1-93'),
 validation=dict(gametest_passed=38,gametest_total=38,client_passed=366,client_total=366,resource_errors=0,bridge_junit_passed=2,
 runtime_entries_identical_to_validation=True,bridge_classes_identical_to_1_4_1=True,host='Vicerach',normal_save_exit=True,render_pages=9,visual_inspection='9 actual renderer pages inspected',
 untested=['full modpack','network multiplayer','third party shaders','all original features and phases','long duration performance','old corrupt player NBT','full dimension unload/reload lifecycle']),
 production_modified=False,source_files=len(srcmanifest),
 evidence={p.relative_to(evidence).as_posix():digest(p) for p in evidence.rglob('*') if p.is_file()})
(OUT/'manifest.json').write_text(json.dumps(manifest,ensure_ascii=False,indent=2),encoding='utf-8')
print(json.dumps(dict(output=str(OUT),artifacts=manifest['artifacts']),ensure_ascii=False))
