"""Validate authored reference structure and reviewed irregular forms."""
from pathlib import Path
from collections import Counter
import xml.etree.ElementTree as ET
import hashlib
root = ET.parse(Path(__file__).resolve().parents[1] / 'app/src/main/assets/reference.xml').getroot()
entries = root.findall('entry')
assert Counter(e.findtext('category') for e in entries) == {'Frases':80,'Verbos':160,'Termos':250,'Expressões':80}
ids = set()
for entry in entries:
    for field in ('category','topic','japanese','reading','romaji','meaning'):
        assert entry.findtext(field), (entry.get('id'),field)
    stable = '\0'.join(entry.findtext(k) for k in ('category','japanese','reading'))
    assert entry.get('id') == 'ref-' + hashlib.sha256(stable.encode()).hexdigest()[:16]
    assert entry.get('id') not in ids
    ids.add(entry.get('id'))
    if entry.findtext('category') != 'Termos':
        assert entry.findtext('detail'), entry.get('id')
    if entry.findtext('category') == 'Frases':
        example = entry.find('example')
        assert example is not None
        assert all(example.findtext(k) for k in ('japanese','reading','romaji','meaning'))
    if entry.findtext('category') == 'Verbos':
        forms = entry.findall('forms/form')
        assert len(forms) == 7 and len({f.get('kind') for f in forms}) == 7
        assert all(f.get('written') and f.get('reading') for f in forms)
for word, form in [('行く','いって'),('来る','きました'),('帰る','かえって'),('泳ぐ','およいで'),('買う','かって')]:
    matches = [e for e in entries if e.findtext('japanese') == word]
    assert any(f.get('reading') == form for e in matches for f in e.findall('forms/form')), (word, form)
print('OK: 570 stable reference IDs, complete content, 80 supplementary examples and 160 seven-form verbs')
