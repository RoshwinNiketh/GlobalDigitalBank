package com.gdb.tests;
import com.gdb.concurrency.*;
import com.gdb.service.*;
import com.gdb.repository.*;
import com.gdb.domain.*;
import java.util.*;
import java.util.concurrent.*;
public class TestExecutorService {
 public static void main(String[] args)throws Exception{testOperations();testPoolsAndCancellation();System.out.println("Activity 29 PASSED");}
 static void testOperations()throws Exception{
  var s=new ExecutorAccountService(new InMemoryAccountRepository(),new InMemoryTransactionRepository(),10);
  try{
   IAccount a=AccountFactory.createAccount("SAVINGS",1001,"Async",30,10000);a.setPin(1234);s.saveAccount(a);
   List<Future<Transaction>> jobs=new ArrayList<>();for(int i=0;i<100;i++)jobs.add(s.depositAsync(1001,1000));
   for(var job:jobs)TestConcurrency.check(job.get(5,TimeUnit.SECONDS).getAmount()==1000);
   TestConcurrency.check(s.getBalanceAsync(1001,1234).get(5,TimeUnit.SECONDS)==110000);
   try{s.withdrawAsync(1001,1,9999).get(5,TimeUnit.SECONDS);throw new AssertionError("Wrong PIN accepted");}catch(ExecutionException expected){}
   List<Callable<Double>> batch=new ArrayList<>();for(int i=0;i<10;i++)batch.add(()->s.getAccount(1001).getBalance());
   for(var f:s.invokeAllTasks(batch,5,TimeUnit.SECONDS))TestConcurrency.check(f.get()==110000);
  }finally{s.shutdown();}
  TestConcurrency.check(s.isShutdown());
  try{s.depositAsync(1001,1);throw new AssertionError("Closed executor accepted work");}catch(RejectedExecutionException expected){}
 }
 static void testPoolsAndCancellation()throws Exception{
  for(var pool:List.of(ThreadPoolFactory.newFixedPool(2),ThreadPoolFactory.newCachedPool(),ThreadPoolFactory.newSingleThreadExecutor())){
   try{TestConcurrency.check(pool.submit(()->42).get(5,TimeUnit.SECONDS)==42);}finally{ThreadPoolFactory.shutdownAndAwait(pool,2);}
   TestConcurrency.check(pool.isTerminated());
  }
  var pool=ThreadPoolFactory.newSingleThreadExecutor();CountDownLatch entered=new CountDownLatch(1),release=new CountDownLatch(1);
  try{
   Future<?> blocker=pool.submit(()->{entered.countDown();release.await();return null;});TestConcurrency.check(entered.await(3,TimeUnit.SECONDS));
   Future<?> queued=pool.submit(()->{throw new AssertionError("Cancelled task ran");});TestConcurrency.check(queued.cancel(false)&&queued.isCancelled());
   List<Future<Integer>> batch=pool.invokeAll(List.of(()->1),20,TimeUnit.MILLISECONDS);TestConcurrency.check(batch.get(0).isCancelled());
   release.countDown();blocker.get(3,TimeUnit.SECONDS);
  }finally{release.countDown();ThreadPoolFactory.shutdownAndAwait(pool,2);}
 }
}
