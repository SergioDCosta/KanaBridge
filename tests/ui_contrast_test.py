"""Check readable text and distinguishable control/focus outlines in both palettes."""
from pathlib import Path
import re
import xml.etree.ElementTree as ET

source = (Path(__file__).resolve().parents[1] / 'app/src/main/java/com/kanabridge/Ui.java').read_text(encoding='utf-8')
palette = dict((name, (dark, light)) for name, dark, light in re.findall(r'(\w+) = Color.parseColor\(dark \? "(#[0-9A-Fa-f]{6})" : "(#[0-9A-Fa-f]{6})"\)', source))
styles = ET.parse(Path(__file__).resolve().parents[1] / 'app/src/main/res/values/styles.xml').getroot()
accents = {style.get('name'): next(item.text for item in style if item.get('name') == 'android:colorAccent') for style in styles.findall('style')}
assert 'resolveAttribute(android.R.attr.colorAccent' in source, 'Ui must use the native theme accent'
palette['accent'] = (accents['KanaBridgeDark'], accents['KanaBridgeLight'])
def rgb(hex_color):
    return tuple(int(hex_color[i:i+2],16)/255 for i in (1,3,5))
def luminance(color):
    channels = [v/12.92 if v <= .04045 else ((v+.055)/1.055)**2.4 for v in color]
    return sum(v*w for v,w in zip(channels,(.2126,.7152,.0722)))
def contrast(a,b):
    x,y=sorted((luminance(a),luminance(b)))
    return (y+.05)/(x+.05)
rows=[]
for index,theme in enumerate(('dark','light')):
    colors={name:rgb(values[index]) for name,values in palette.items()}
    checks=[('ink','background',4.5),('ink','surface',4.5),('muted','background',4.5),('muted','surface',4.5),('muted','soft',4.5),('onAccent','accent',4.5),('accent','background',4.5),('error','surface',4.5),('line','surface',3),('line','background',3),('accent','surface',3),('onAccent','accent',3)]
    for foreground,background,minimum in checks:
        value=contrast(colors[foreground],colors[background])
        assert value >= minimum, (theme,foreground,background,round(value,2),minimum)
        rows.append((theme,foreground,background,round(value,2)))
    # Ripple overlays use 32/255 alpha. Check text at the darkest/lightest pressed endpoint.
    for foreground,background in (('ink','surface'),('onAccent','accent')):
        fill=tuple(top*32/255+bottom*(1-32/255) for top,bottom in zip(colors['ink'],colors[background]))
        value=contrast(colors[foreground],fill)
        assert value>=4.5,(theme,'pressed',foreground,value)
        rows.append((theme,'pressed '+foreground,background,round(value,2)))
for row in rows: print('%s %s / %s: %.2f' % row)
print('OK: %d palette contrast checks' % len(rows))
