"""Execute production Java query plans against the shipped SQLite dictionary.
Run run-tests.ps1 first to compile LexiconQueryProbe.
"""
import sqlite3, subprocess, time, json, base64
from pathlib import Path
root = Path(__file__).resolve().parents[1]
db = sqlite3.connect(f"file:{(root / 'app/src/main/assets/lexicon.db').as_posix()}?mode=ro", uri=True)
def query(text, meaning=False, limit=30, offset=0):
    output = subprocess.check_output(['java', '-Dfile.encoding=UTF-8', '-cp', str(root/'build/tests'), 'com.kanabridge.LexiconQueryProbe', base64.b64encode(text.encode('utf-8')).decode('ascii'), str(meaning).lower(), str(limit), str(offset)], encoding='utf-8')
    lines=output.splitlines()
    start=time.perf_counter(); rows=db.execute(lines[0],lines[1:]).fetchall()
    return rows, (time.perf_counter()-start)*1000
measurements=[]
for text in ['hashi','はし','ハシ']:
    rows, ms=query(text); assert any(r[0]=='橋' for r in rows) and any(r[0]=='箸' for r in rows), text
    assert rows[:9] and all(r[1]=='はし' for r in rows[:9]), 'exact before prefixes'
    assert any(r[1]!='はし' for r in rows), 'prefixes not suppressed by exact hits'
    measurements.append((text,ms))
rows,_=query('橋'); assert any(r[0]=='橋' for r in rows) and any(r[0].startswith('橋') and r[0]!='橋' for r in rows)
for text, word in [('bridge','橋'),('school','学校'),('chopsticks','箸')]:
    rows,ms=query(text,True); assert any(r[0]==word for r in rows), (text,word)
    measurements.append((text,ms))
assert query('BRIDGE',True)[0]==query('bridge',True)[0], 'case tolerant meaning search'
# Wildcards are literal user input, never an instruction to return all entries.
for token in ['%','_',"' OR 1=1 --",'zzzznosuchword']:
    rows,_=query(token,True)
    assert all(token.lower() in r[2].lower() for r in rows), 'literal parameterized query'
full,_=query('はし',False,60); first,_=query('はし',False,30); second,_=query('はし',False,30,30)
assert len(first)==30 and len(second)==30 and full==first+second, 'stable pagination'
assert query('zzzznosuchword')[0]==[], 'missing Japanese query'
print('OK: production dictionary SQL, exact/prefix/kanji/English/literal inputs and stable pages')
print(json.dumps({'host_query_ms':measurements},ensure_ascii=False))
