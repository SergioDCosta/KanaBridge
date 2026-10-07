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
db.execute('CREATE TABLE words (reading TEXT NOT NULL, word TEXT NOT NULL, meaning TEXT NOT NULL, rank INTEGER NOT NULL, entry INTEGER NOT NULL)')
entries = rows = 0
with gzip.open(source, 'rb') as stream:
    for event, entry in ET.iterparse(stream, events=('end',)):
        if entry.tag != 'entry':
            continue
        entries += 1
        ident = int(entry.findtext('ent_seq'))
        kanji = entry.findall('k_ele')
        senses = entry.findall('sense')
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
                db.execute('INSERT INTO words VALUES (?,?,?,?,?)', (key, word, '; '.join(dict.fromkeys(glosses)), rank, ident))
                rows += 1
        entry.clear()
db.execute('CREATE INDEX by_reading ON words(reading,rank,word)')
db.execute('CREATE INDEX by_word ON words(word,rank)')
db.execute('PRAGMA user_version=1')
db.commit()
db.execute('VACUUM')
assert db.execute('PRAGMA integrity_check').fetchone()[0] == 'ok'
assert db.execute("SELECT COUNT(*) FROM words WHERE reading='にほんご' AND word='日本語'").fetchone()[0]
assert db.execute("SELECT COUNT(*) FROM words WHERE reading='はし'").fetchone()[0] > 1
db.close()
temporary.replace(target)
metadata = dict(source='https://www.edrdg.org/pub/Nihongo/JMdict_e.gz',
                generated=datetime.now(timezone.utc).isoformat(), entries=entries, rows=rows,
                sha256=hashlib.sha256(source.read_bytes()).hexdigest(),
                attribution='JMdict © James William Breen and the Electronic Dictionary Research and Development Group',
                licence='CC BY-SA 4.0', modifications='SQLite extraction; reading normalization; sense restrictions; priority ranking')
(target.parent / 'lexicon-source.json').write_text(json.dumps(metadata, ensure_ascii=False, indent=2), encoding='utf-8')
print(json.dumps(metadata, ensure_ascii=False))
