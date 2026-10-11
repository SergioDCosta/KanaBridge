"""Recompress an unsigned APK before alignment/signing; preserve stored resources."""
import sys
from pathlib import Path
from zipfile import ZipFile
path = Path(sys.argv[1]).resolve()
temporary = path.with_suffix('.repacked.apk')
try:
    with ZipFile(path) as source, ZipFile(temporary, 'w') as target:
        for entry in source.infolist():
            target.writestr(entry, source.read(entry), compress_type=entry.compress_type, compresslevel=9)
    temporary.replace(path)
finally:
    temporary.unlink(missing_ok=True)
