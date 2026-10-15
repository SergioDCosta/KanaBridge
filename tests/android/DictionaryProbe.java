package com.kanabridge;

import android.app.Instrumentation;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/** Physical-device repository checks; only this test's preference fixture is edited. */
public final class DictionaryProbe extends Instrumentation {
 private int assertions; private OfflineLexicon repository;
 private void check(boolean value,String label){if(!value)throw new AssertionError(label);assertions++;}
 @Override public void onCreate(Bundle args){super.onCreate(args);start();}
 private List<OfflineLexicon.Word> query(String query,int mode,boolean legacy)throws Exception{
  CountDownLatch latch=new CountDownLatch(1); List<OfflineLexicon.Word> result=new ArrayList<>();String[] error={null};
  OfflineLexicon.Callback callback=(words,failure)->{result.addAll(words);error[0]=failure;latch.countDown();};
  if(legacy)repository.resolveLegacy(query,callback);else repository.dictionary(query,mode,31,0,callback);
  if(!latch.await(30,TimeUnit.SECONDS))throw new AssertionError("Query timeout");
  check(error[0]==null,"repository query: "+error[0]);return result;
 }
 @Override public void onStart(){
  int status = 0; Bundle result=new Bundle(); SharedPreferences fixture=getTargetContext().getSharedPreferences("dictionary-probe-fixture",0);
  Map<String,?> liveBefore=getTargetContext().getSharedPreferences("kanabridge",0).getAll();
  try{
   repository=new OfflineLexicon(getTargetContext()); CountDownLatch reference=new CountDownLatch(1);String[] failure={null};
   repository.prepareReference(error->{failure[0]=error;reference.countDown();});check(reference.await(20,TimeUnit.SECONDS)&&failure[0]==null,"reference asset loaded");
   long first=SystemClock.elapsedRealtime();query("hashi",0,false);result.putLong("first_query_ms",SystemClock.elapsedRealtime()-first);
   for(int mode=0;mode<2;mode++){
    String text=mode==0?"hashi":"bridge";long[] samples=new long[20];
    for(int i=0;i<20;i++){long start=SystemClock.elapsedRealtimeNanos();List<OfflineLexicon.Word> words=query(text,mode,false);check(!words.isEmpty(),"mode "+mode+" returns words");samples[i]=SystemClock.elapsedRealtimeNanos()-start;}
    Arrays.sort(samples);result.putDouble("mode_"+mode+"_p95_ms",samples[18]/1000000.0);
   }
   Set<String> old=new HashSet<>(Arrays.asList("学校","橋"));fixture.edit().clear().putStringSet("favorites",old).putString("draft","fixture draft kept").commit();
   SavedWords saved=new SavedWords(fixture);check(saved.legacy().size()==2,"V5 favourites retained");
   List<OfflineLexicon.Word> school=query("学校",0,true);check(school.size()==1,"unambiguous migration fixture");
   OfflineLexicon.Word word=school.get(0);saved.migrate("学校",new SavedEntry(word.entry,word.word,word.reading,word.meaning,word.originalReading));
   check(saved.legacy().equals(Collections.singletonList("橋")),"only resolved legacy removed");
   List<OfflineLexicon.Word> bridge=query("橋",0,true);check(bridge.size()>1,"ambiguous spelling fixture");
   OfflineLexicon.Word one=bridge.get(0),two=bridge.get(1);
   saved.toggle(new SavedEntry(one.entry,one.word,one.reading,one.meaning,one.originalReading));saved.toggle(new SavedEntry(two.entry,two.word,two.reading,two.meaning,two.originalReading));
   check(new SavedWords(fixture).entries().size()==3,"same spelling with separate identities survives store reload");
   check(saved.legacy().contains("橋"),"ambiguous V5 favourite not silently assigned");check(fixture.getString("draft","").equals("fixture draft kept"),"migration preserves draft");
   check(liveBefore.equals(getTargetContext().getSharedPreferences("kanabridge",0).getAll()),"live user preferences unchanged");
   result.putString("result","OK: "+assertions+" repository and migration assertions");
  }catch(Throwable error){result.putString("result","FAILED: "+error);status = 1;}
  finally{fixture.edit().clear().commit();if(repository!=null)repository.close();}
  finish(status,result);
 }
}
