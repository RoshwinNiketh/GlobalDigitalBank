package com.gdb.concurrency;
import java.util.concurrent.*;
import java.util.function.Supplier;
public final class AsyncUtils {
 private AsyncUtils(){}
 public static <T> CompletableFuture<T> supplyAsyncWithFallback(Supplier<T> supplier,T fallback,Executor executor){
  try{return CompletableFuture.supplyAsync(supplier,executor).exceptionally(error->fallback);}catch(RejectedExecutionException error){return CompletableFuture.completedFuture(fallback);}
 }
 public static <T> CompletableFuture<T> supplyAsyncWithDefault(Supplier<T> supplier,T fallback,Executor executor){return supplyAsyncWithFallback(supplier,fallback,executor);}
 /** Timeout affects this observation only; it does not undo or cancel a banking operation. */
 public static <T> CompletableFuture<T> withTimeout(CompletableFuture<T> future,long timeout,TimeUnit unit,T fallback){return future.copy().completeOnTimeout(fallback,timeout,unit);}
 public static <T> CompletableFuture<T> timeoutWrapper(CompletableFuture<T> future,long timeout,TimeUnit unit,T fallback){return withTimeout(future,timeout,unit,fallback);}
}
