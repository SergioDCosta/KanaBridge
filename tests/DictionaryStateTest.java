package com.kanabridge;
public final class DictionaryStateTest {
 private static int count;
 private static void check(boolean value,String label){if(!value)throw new AssertionError(label);count++;}
 public static void main(String[] args)throws Exception{
  SavedEntry a=new SavedEntry(100,"橋","はし","bridge\n\t% +","はし");
  SavedEntry b=new SavedEntry(101,"橋","はし","different entry","はし");
  SavedEntry c=new SavedEntry(100,"橋","きょう","different reading","きょう");
  check(!a.key().equals(b.key()),"homographs with different IDs stay separate");
  check(!a.key().equals(c.key()),"selected reading stays separate");
  SavedEntry restored=SavedEntry.parse(a.serialize());check(restored!=null&&restored.key().equals(a.key())&&restored.meaning.equals(a.meaning),"unicode and delimiters round trip");
  check(a.key().equals(new SavedEntry(100,"橋","はし","updated meaning","はし").key()),"updated dictionary gloss preserves saved identity");
  check(SavedEntry.parse("bad")==null&&SavedEntry.parse("0\tx\ty\tz\tz")==null,"invalid identity rejected");
  String recent="";for(int i=0;i<40;i++)recent=RecentQueries.add(recent,"query "+i,0);
  check(RecentQueries.read(recent).size()==30,"history capped at 30");check(RecentQueries.read(recent).get(0).query.equals("query 39"),"newest first");
  recent=RecentQueries.add(recent,"query 20",0);check(RecentQueries.read(recent).size()==30&&RecentQueries.read(recent).get(0).query.equals("query 20"),"duplicate moves to front");
  recent=RecentQueries.add(recent,"bridge\t\n% +",1);check(RecentQueries.read(recent).get(0).query.equals("bridge\t\n% +")&&RecentQueries.read(recent).get(0).mode==1,"query and mode round trip");
  check(RecentQueries.add(recent,"água",2).equals(recent),"Portuguese searches no longer accepted");
  check(RecentQueries.read("2\t"+SavedEntry.encode("água")+"\n0\t"+SavedEntry.encode("hashi")).size()==1,"stored Portuguese history dropped on read");
  check(RecentQueries.add(recent,"",0).equals(recent),"blank query not retained");
  try(java.io.InputStream in=new java.io.FileInputStream("app/src/main/assets/reference.xml")){JapaneseReference.load(in);}
  check(JapaneseReference.inflected("tabemashita").stream().anyMatch(e->e.japanese.equals("食べる")),"polite past mapped to reviewed dictionary form");
  check(JapaneseReference.inflected("いって").stream().anyMatch(e->e.japanese.equals("行く")),"iku irregular te form");
  check(JapaneseReference.inflected("きました").stream().anyMatch(e->e.japanese.equals("来る"))&&JapaneseReference.inflected("きました").stream().anyMatch(e->e.japanese.equals("着る")),"ambiguous inflections retain both candidates");
  check(JapaneseReference.inflected("かえって").stream().anyMatch(e->e.japanese.equals("帰る")),"kaeru is godan despite eru ending");
  check(JapaneseReference.inflected("zzzz").isEmpty(),"unknown forms not invented");
  System.out.println("OK: "+count+" dictionary identity, history and inflection assertions");
 }
}
