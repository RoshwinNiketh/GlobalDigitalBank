package com.gdb.concurrency;
import com.gdb.domain.IAccount;
import com.gdb.service.ThreadSafeAccountService;
import java.util.*;
import java.util.concurrent.*;
public final class ConcurrentDepositDemo {
 @FunctionalInterface public interface Task { void run() throws Exception; }
 public static void parallel(int threads,Task task) throws Exception {
  if(threads<1) throw new IllegalArgumentException("Positive thread count required");
  CountDownLatch start=new CountDownLatch(1);
  ExecutorService pool=Executors.newFixedThreadPool(threads);
  List<Future<?>> jobs=new ArrayList<>();
  try {
   for(int i=0;i<threads;i++) jobs.add(pool.submit(()->{ start.await(); task.run(); return null; }));
   start.countDown();
   for(Future<?> job:jobs) job.get(30,TimeUnit.SECONDS);
  } finally { pool.shutdownNow(); if(!pool.awaitTermination(5,TimeUnit.SECONDS)) throw new AssertionError("Workers did not stop"); }
 }
 public static double runUnsafeDemo(IAccount a,int threads,double amount,int iterations) throws Exception {
  parallel(threads,()->{for(int i=0;i<iterations;i++) a.deposit(amount);}); return a.getBalance();
 }
 public static double runSafeDemo(ThreadSafeAccountService s,int a,int threads,double amount,int iterations) throws Exception {
  parallel(threads,()->{for(int i=0;i<iterations;i++) s.deposit(a,amount);}); return s.getAccount(a).getBalance();
 }
}
