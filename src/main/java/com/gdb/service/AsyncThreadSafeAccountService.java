package com.gdb.service;
import com.gdb.repository.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
/** Async facade over ordered account locks. A timeout does not undo an accepted operation. */
public final class AsyncThreadSafeAccountService extends AsyncAccountService {
 private final long timeoutMs;
 private final AtomicLong operations=new AtomicLong();
 public AsyncThreadSafeAccountService(AccountRepository a,TransactionRepository t,Properties config){
  super(a,t,poolSize(config));
  timeoutMs=timeout(config);
 }
 private static int poolSize(Properties config){timeout(config);return Integer.parseInt(config==null?"10":config.getProperty("service.thread.pool.size","10"));}
 private static long timeout(Properties config){long value=Long.parseLong(config==null?"5000":config.getProperty("service.timeout.ms","5000"));if(value<1)throw new IllegalArgumentException("Timeout must be positive");return value;}
 @Override protected <T> CompletableFuture<T> submit(Operation<T> operation){
  return super.submit(()->{T result=operation.run();operations.incrementAndGet();return result;}).copy().orTimeout(timeoutMs,TimeUnit.MILLISECONDS);
 }
 public long getTotalOperations(){return operations.get();}
}
