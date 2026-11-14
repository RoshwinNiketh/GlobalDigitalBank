package com.gdb.concurrency;
import java.util.concurrent.*;
public final class ThreadPoolFactory {
 private ThreadPoolFactory(){}
 public static ExecutorService newFixedPool(int size){return Executors.newFixedThreadPool(size);}
 public static ExecutorService newCachedPool(){return Executors.newCachedThreadPool();}
 public static ExecutorService newSingleThreadExecutor(){return Executors.newSingleThreadExecutor();}
 public static void shutdownAndAwait(ExecutorService pool,long seconds){shutdownGracefully(pool,Math.multiplyExact(seconds,1000));}
 public static void shutdownGracefully(ExecutorService pool,long millis){
  if(millis<0)throw new IllegalArgumentException("Negative shutdown timeout");
  pool.shutdown();
  try {if(!pool.awaitTermination(millis,TimeUnit.MILLISECONDS)){pool.shutdownNow();if(!pool.awaitTermination(millis,TimeUnit.MILLISECONDS))throw new IllegalStateException("Executor did not terminate");}}
  catch(InterruptedException e){pool.shutdownNow();Thread.currentThread().interrupt();throw new IllegalStateException("Interrupted during shutdown",e);}
 }
}
