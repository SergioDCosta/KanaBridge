"""Validate the shipped offline derivative without Android dependencies."""
import hashlib
import json
import sqlite3
from pathlib import Path

root = Path(__file__).resolve().parents[1]
assets = root / 'app/src/main/assets'
meta = json.loads((assets / 'lexicon-source.json').read_text(encoding='utf-8'))
db = sqlite3.connect(f"file:{(assets / 'lexicon.db').as_posix()}?mode=ro", uri=True)
assert db.execute('PRAGMA integrity_check').fetchone()[0] == 'ok'
assert db.execute('SELECT COUNT(*) FROM words').fetchone()[0] == meta['rows']
assert db.execute('SELECT COUNT(DISTINCT entry) FROM words').fetchone()[0] == meta['entries']
for reading, word, meaning in [('にほんご', '日本語', 'Japanese'), ('がっこう', '学校', 'school'),
                               ('はし', '橋', 'bridge'), ('はし', '箸', 'chopsticks'),
                               ('たべる', '食べる', 'eat'), ('いらっしゃいませ', 'いらっしゃいませ', 'welcome')]:
    rows = db.execute('SELECT meaning FROM words WHERE reading=? AND word=?', (reading, word)).fetchall()
    assert any(meaning.lower() in row[0].lower() for row in rows), (reading, word)
assert db.execute("SELECT COUNT(*) FROM words WHERE reading='はし'").fetchone()[0] > 2
assert db.execute("SELECT COUNT(*) FROM words WHERE reading='' OR word='' OR meaning=''").fetchone()[0] == 0
assert 'by_reading' in str(db.execute('EXPLAIN QUERY PLAN SELECT word FROM words WHERE reading=? ORDER BY rank,word LIMIT 30', ('はし',)).fetchall())
assert 'by_word' in str(db.execute('EXPLAIN QUERY PLAN SELECT reading FROM words WHERE word=?', ('日本語',)).fetchall())
assert (assets / 'EDRDG-LICENCE.html').is_file() and (assets / 'CC-BY-SA-4.0.txt').is_file()
source = root / '.tooling/dictionary/JMdict_e.gz'
if source.exists():
    assert hashlib.sha256(source.read_bytes()).hexdigest() == meta['sha256']
db.close()
print(f"OK: SQLite integrity, {meta['entries']} entries / {meta['rows']} spellings, lexical examples, indexes and attribution")
