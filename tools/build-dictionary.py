"""Update the offline JMdict derivative (CC BY-SA 4.0).

Download https://www.edrdg.org/pub/Nihongo/JMdict_e.gz to .tooling/dictionary/
then run this script. Keep the source digest and licence alongside the database.
Reading/orthography and sense restrictions are honoured; no sentence translation.
"""
import gzip
import hashlib
import json
import sqlite3
import xml.etree.ElementTree as ET
from datetime import datetime, timezone
from pathlib import Path

root = Path(__file__).resolve().parents[1]
source = root / '.tooling/dictionary/JMdict_e.gz'
target = root / 'app/src/main/assets/lexicon.db'
target.parent.mkdir(parents=True, exist_ok=True)
temporary = target.with_suffix('.tmp')
temporary.unlink(missing_ok=True)
db = sqlite3.connect(temporary)
db.execute('CREATE TABLE spellings (reading TEXT NOT NULL, word TEXT NOT NULL, definition INTEGER NOT NULL, rank INTEGER NOT NULL, entry INTEGER NOT NULL, original_reading TEXT NOT NULL)')
db.execute('CREATE TABLE definitions (id INTEGER PRIMARY KEY, meaning TEXT NOT NULL, glosses TEXT NOT NULL)')
definitions = {}
def intern_definition(glosses):
    key = tuple(glosses)
    if key not in definitions:
        definitions[key] = len(definitions) + 1
        db.execute('INSERT INTO definitions VALUES (?,?,?)', (definitions[key], '; '.join(glosses), json.dumps(glosses, ensure_ascii=False, separators=(',', ':'))))
    return definitions[key]
db.execute('CREATE TABLE senses (entry INTEGER NOT NULL, ordinal INTEGER NOT NULL, definition INTEGER NOT NULL, payload TEXT NOT NULL, PRIMARY KEY(entry,ordinal))')
entries = rows = sense_count = 0
with gzip.open(source, 'rb') as stream:
    for event, entry in ET.iterparse(stream, events=('end',)):
        if entry.tag != 'entry':
            continue
        entries += 1
        ident = int(entry.findtext('ent_seq'))
        kanji = entry.findall('k_ele')
        senses = entry.findall('sense')
        inherited_pos = []
        for ordinal, sense in enumerate(senses, 1):
            pos = [x.text for x in sense.findall('pos')]
            if pos:
                inherited_pos = pos
            glosses = [x.text for x in sense.findall('gloss') if x.text and x.get('{http://www.w3.org/XML/1998/namespace}lang', 'eng') == 'eng']
            if glosses:
                payload = {tag: [x.text for x in sense.findall(tag) if x.text] for tag in ('stagk','stagr','field','misc','dial','s_inf','xref','ant')}
                payload = {key: value for key, value in payload.items() if value}
                payload.update(pos=inherited_pos)
                db.execute('INSERT INTO senses VALUES (?,?,?,?)', (ident, ordinal, intern_definition(glosses), json.dumps(payload, ensure_ascii=False, separators=(',', ':'))))
                sense_count += 1
        for reading in entry.findall('r_ele'):
            kana = reading.findtext('reb')
            restricted = [x.text for x in reading.findall('re_restr')]
            spellings = [] if reading.find('re_nokanji') is not None else [k for k in kanji if not restricted or k.findtext('keb') in restricted]
            for spelling in spellings or [None]:
                word = spelling.findtext('keb') if spelling is not None else kana
                glosses = []
                for sense in senses:
                    stagk = [x.text for x in sense.findall('stagk')]
                    stagr = [x.text for x in sense.findall('stagr')]
                    if (stagk and word not in stagk) or (stagr and kana not in stagr):
                        continue
                    for gloss in sense.findall('gloss'):
                        if gloss.text and gloss.get('{http://www.w3.org/XML/1998/namespace}lang', 'eng') == 'eng':
                            glosses.append(gloss.text)
                if not glosses:
                    continue
                priorities = [x.text for x in reading.findall('re_pri')]
                if spelling is not None:
                    priorities += [x.text for x in spelling.findall('ke_pri')]
                rank = 0 if any(p in ('ichi1', 'news1', 'spec1', 'gai1') for p in priorities) else 1 if priorities else 2
                # Indexed form uses hiragana for equivalent katakana readings.
                key = ''.join(chr(ord(c) - 96) if '\u30a1' <= c <= '\u30f6' else c for c in kana)
                db.execute('INSERT INTO spellings VALUES (?,?,?,?,?,?)', (key, word, intern_definition(list(dict.fromkeys(glosses))), rank, ident, kana))
                rows += 1
        entry.clear()
db.execute('CREATE INDEX by_reading ON spellings(reading,rank,word)')
db.execute('CREATE INDEX by_word ON spellings(word,rank)')
db.execute('CREATE INDEX by_definition ON spellings(definition)')
db.execute('CREATE VIEW words AS SELECT reading,word,meaning,rank,entry,original_reading FROM spellings JOIN definitions ON definitions.id=spellings.definition')
db.execute('PRAGMA user_version=2')
db.commit()
db.execute('VACUUM')
assert db.execute('PRAGMA integrity_check').fetchone()[0] == 'ok'
assert db.execute("SELECT COUNT(*) FROM words WHERE reading='にほんご' AND word='日本語'").fetchone()[0]
assert db.execute("SELECT COUNT(*) FROM words WHERE reading='はし'").fetchone()[0] > 1
db.close()
temporary.replace(target)
metadata = dict(source='https://www.edrdg.org/pub/Nihongo/JMdict_e.gz',
                generated=datetime.now(timezone.utc).isoformat(), entries=entries, rows=rows, senses=sense_count, schema_version=2, importer_version=2, database_sha256=hashlib.sha256(target.read_bytes()).hexdigest(),
                sha256=hashlib.sha256(source.read_bytes()).hexdigest(),
                attribution='JMdict © James William Breen and the Electronic Dictionary Research and Development Group',
                licence='CC BY-SA 4.0', modifications='SQLite extraction; reading normalization; sense restrictions; priority ranking; original readings; structured senses and grammatical/usage tags')
(target.parent / 'lexicon-source.json').write_text(json.dumps(metadata, ensure_ascii=False, indent=2), encoding='utf-8')
print(json.dumps(metadata, ensure_ascii=False))
