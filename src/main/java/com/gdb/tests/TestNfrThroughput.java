package com.gdb.tests;
import com.gdb.concurrency.*;
import com.gdb.service.*;
import com.gdb.repository.*;
import java.util.*;
import java.util.concurrent.*;
public class TestNfrThroughput {
 public static void main(String[] args)throws Exception{testCorrectness();testMetrics();testComparison();testTimeout();System.out.println("Activity 31 PASSED");}
 static void testCorrectness()throws Exception{
  var accounts=new InMemoryAccountRepository();var ledger=new InMemoryTransactionRepository();
  try(var s=new AsyncThreadSafeAccountService(accounts,ledger,new Properties())){
   int a=s.openAccountAsync("SAVINGS","First",30,100000).get().getAccountNumber(),b=s.openAccountAsync("SAVINGS","Second",30,100000).get().getAccountNumber();
   s.setPinAsync(a,1234).get();s.setPinAsync(b,1234).get();
   List<CompletableFuture<?>> jobs=new ArrayList<>();for(int i=0;i<1000;i++)jobs.add(s.depositAsync(a,1));
   CompletableFuture.allOf(jobs.toArray(CompletableFuture[]::new)).get(10,TimeUnit.SECONDS);
   TestConcurrency.check(s.getBalanceAsync(a,1234).get()==101000);
   jobs.clear();for(int i=0;i<100;i++){jobs.add(s.transferAsync(a,b,1,1234));jobs.add(s.transferAsync(b,a,1,1234));}
   CompletableFuture.allOf(jobs.toArray(CompletableFuture[]::new)).get(10,TimeUnit.SECONDS);
   TestConcurrency.check(s.getBalanceAsync(a,1234).get()==101000 && s.getBalanceAsync(b,1234).get()==100000);
   TestConcurrency.check(ledger.findAll().size()==1200 && s.getTotalOperations()>=1204);
   long before=s.getTotalOperations();try{s.withdrawAsync(a,999999,1234).get();throw new AssertionError("Invalid withdrawal succeeded");}catch(ExecutionException expected){}
   TestConcurrency.check(s.getTotalOperations()==before);
  }
 }
 static void testMetrics()throws Exception{
  TestConcurrency.check(ThroughputBenchmark.calculatePercentile(List.of(4L,1L,3L,2L),95)==4);
  var failed=ThroughputBenchmark.runBenchmark("Failure probe",()->{throw new IllegalStateException();},20,2);
  TestConcurrency.check(failed.errorRatePercent()==100 && failed.opsPerSecond()==0);
 }
 static void testComparison()throws Exception{
  var a=new InMemoryAccountRepository();var t=new InMemoryTransactionRepository();var sync=new LockedAccountService(a,t);sync.openAccount("SAVINGS","Sync",30,10000);
  var b=new InMemoryAccountRepository();var u=new InMemoryTransactionRepository();
  try(var async=new AsyncThreadSafeAccountService(b,u,new Properties())){
   async.openAccountAsync("SAVINGS","Async",30,10000).get();
   // Warm up both implementations before collecting comparable completed-operation latencies.
   for(int i=0;i<100;i++){sync.deposit(1001,1);async.depositAsync(1001,1).get();}
   var sr=ThroughputBenchmark.runBenchmark("Synchronous",()->{try{sync.deposit(1001,1);}catch(Exception e){throw new CompletionException(e);}},1000,10);
   var ar=ThroughputBenchmark.runBenchmark("Async thread-safe",()->async.depositAsync(1001,1).join(),1000,10);
   ThroughputBenchmark.printComparisonTable(sr,ar);
   for(var result:List.of(sr,ar))TestConcurrency.check(result.p95LatencyMs()<100 && result.errorRatePercent()==0 && result.opsPerSecond()>0);
   TestConcurrency.check(a.findById(1001).getBalance()==11100 && b.findById(1001).getBalance()==11100);
   TestConcurrency.check(t.findAll().size()==1100 && u.findAll().size()==1100);
  }
 }
 static void testTimeout()throws Exception{
  CountDownLatch entered=new CountDownLatch(1),release=new CountDownLatch(1);
  var repo=new InMemoryAccountRepository(){@Override public com.gdb.domain.IAccount findById(int id){entered.countDown();try{release.await();}catch(InterruptedException e){Thread.currentThread().interrupt();throw new RuntimeException(e);}return super.findById(id);}};
  Properties config=new Properties();config.setProperty("service.timeout.ms","50");
  var s=new AsyncThreadSafeAccountService(repo,new InMemoryTransactionRepository(),config);
  try{
   var pending=s.getAccountAsync(9999);TestConcurrency.check(entered.await(2,TimeUnit.SECONDS));
   try{pending.get(2,TimeUnit.SECONDS);throw new AssertionError("Expected timeout");}catch(ExecutionException expected){TestConcurrency.check(expected.getCause() instanceof TimeoutException);}
  }finally{release.countDown();s.shutdown();}
 }
}
