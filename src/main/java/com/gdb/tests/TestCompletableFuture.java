package com.gdb.tests;
import com.gdb.service.*;
import com.gdb.repository.*;
import com.gdb.concurrency.*;
import java.util.*;
import java.util.concurrent.*;
public class TestCompletableFuture {
 public static void main(String[] args)throws Exception{testPipelines();testFallbackTimeout();System.out.println("Activity 30 PASSED");}
 static void testPipelines()throws Exception{
  var s=new AsyncAccountService(new InMemoryAccountRepository(),new InMemoryTransactionRepository(),4);
  try{
   double balance=s.openAccountAsync("SAVINGS","Pipeline",30,50000).thenCompose(a->s.setPinAsync(a.getAccountNumber(),1234).thenApply(v->a)).thenCompose(a->s.executePipelineAsync(a.getAccountNumber(),5000,10000,1234)).get(5,TimeUnit.SECONDS);
   TestConcurrency.check(balance==45000);
   boolean recovered=s.withdrawAsync(1001,999999,1234).handle((v,e)->e!=null).get(5,TimeUnit.SECONDS);TestConcurrency.check(recovered);
   TestConcurrency.check(s.getBalanceAsync(1001,1234).get()==45000);
   List<Integer> ids=new ArrayList<>();ids.add(1001);for(int i=0;i<4;i++)ids.add(s.openAccountAsync("SAVINGS","Fanout",30,35000).get().getAccountNumber());
   TestConcurrency.check(s.getTotalBalanceAsync(ids).get(5,TimeUnit.SECONDS)==185000);
   TestConcurrency.check(s.depositAllAsync(ids,100).get(5,TimeUnit.SECONDS).size()==5);
   TestConcurrency.check(s.getTotalBalanceAsync(ids).get()==185500);
   TestConcurrency.check(s.getTotalBalanceAsync(List.of()).get()==0);
   try{s.getAccountAsync(9999).get();throw new AssertionError("Missing account succeeded");}catch(ExecutionException expected){}
  }finally{s.shutdown();}
  TestConcurrency.check(s.depositAsync(1001,1).isCompletedExceptionally());
 }
 static void testFallbackTimeout()throws Exception{
  var pool=ThreadPoolFactory.newSingleThreadExecutor();
  try{
   TestConcurrency.check(AsyncUtils.supplyAsyncWithFallback(()->{throw new IllegalStateException();},42,pool).get()==42);
   CompletableFuture<Integer> source=new CompletableFuture<>();
   TestConcurrency.check(AsyncUtils.withTimeout(source,20,TimeUnit.MILLISECONDS,7).get(2,TimeUnit.SECONDS)==7);
   TestConcurrency.check(!source.isDone());source.complete(9);TestConcurrency.check(source.get()==9);
  }finally{ThreadPoolFactory.shutdownAndAwait(pool,2);}
 }
}
