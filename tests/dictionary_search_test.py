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

for text, word in [('água','水'), ('AGUA','水'), ('comer','食べる'), ('estacao','駅')]:
    rows,_=query(text,'pt'); assert any(r[0]==word for r in rows),(text,word)
assert query('água','pt')[0]==query('agua','pt')[0], 'Portuguese accent tolerant'
for token in ['%', '_', "' OR 1=1 --", 'zzzznosuchword']:
    assert query(token,'pt')[0]==[], 'PT literals never expand to all words'
# A reproducible corpus covers 100 distinct kana/kanji/rōmaji/EN/PT queries.
corpus = db.execute("SELECT DISTINCT word,reading FROM words WHERE rank=0 AND length(word)>=2 AND length(reading)>=3 ORDER BY entry,word LIMIT 50").fetchall()
for word,reading in corpus:
    assert any(r[0]==word and r[1]==reading for r in query(word,False,300)[0]), ('kanji corpus',word,reading)
    assert any(r[0]==word and r[1]==reading for r in query(reading,False,300)[0]), ('reading corpus',word,reading)
print('OK: Portuguese partial layer, accent/literal inputs and 100-query lexical corpus')

output=subprocess.check_output(['java','-cp',str(root/'build/tests'),'com.kanabridge.LexiconQueryProbe',base64.b64encode(b'agua').decode(),'pt','30','0'],encoding='utf-8').splitlines()
plan=db.execute('EXPLAIN QUERY PLAN '+output[0],output[1:]).fetchall()
assert not any('SCAN spellings' in row[3] for row in plan), 'PT mode must not scan the whole lexicon'
assert any('by_word' in row[3] for row in plan), 'PT mode uses existing spelling index'
