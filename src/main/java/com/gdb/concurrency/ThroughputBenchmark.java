package com.gdb.concurrency;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
public final class ThroughputBenchmark {
 public record BenchmarkResult(String name,int totalOperations,double durationMs,double opsPerSecond,double avgLatencyMs,double p95LatencyMs,double p99LatencyMs,double errorRatePercent){
  public double getP95LatencyMs(){return p95LatencyMs;}
 }
 public static BenchmarkResult runBenchmark(String name,Runnable task,int totalOps,int concurrency)throws Exception{
  if(totalOps<1||concurrency<1)throw new IllegalArgumentException("Positive operation and worker counts required");
  long[] latencies=new long[totalOps];AtomicInteger next=new AtomicInteger(),errors=new AtomicInteger();
  CountDownLatch ready=new CountDownLatch(concurrency),start=new CountDownLatch(1);
  ExecutorService pool=ThreadPoolFactory.newFixedPool(concurrency);List<Future<?>> workers=new ArrayList<>();
  long begin;
  try{
   for(int w=0;w<concurrency;w++)workers.add(pool.submit(()->{
    ready.countDown();start.await();int index;
    while((index=next.getAndIncrement())<totalOps){long time=System.nanoTime();try{task.run();}catch(RuntimeException e){errors.incrementAndGet();}finally{latencies[index]=System.nanoTime()-time;}}return null;
   }));
   if(!ready.await(5,TimeUnit.SECONDS))throw new AssertionError("Workers not ready");
   begin=System.nanoTime();start.countDown();
   for(var worker:workers)worker.get(30,TimeUnit.SECONDS);
   double duration=(System.nanoTime()-begin)/1e6;
   List<Long> samples=Arrays.stream(latencies).boxed().toList();
   return new BenchmarkResult(name,totalOps,duration,(totalOps-errors.get())*1000.0/duration,
    samples.stream().mapToLong(Long::longValue).average().orElseThrow()/1e6,
    calculatePercentile(samples,95)/1e6,calculatePercentile(samples,99)/1e6,errors.get()*100.0/totalOps);
  }finally{start.countDown();pool.shutdownNow();if(!pool.awaitTermination(5,TimeUnit.SECONDS))throw new AssertionError("Benchmark workers still running");}
 }
 /** Nearest-rank percentile, in the same units as the input; does not mutate input. */
 public static double calculatePercentile(List<Long> samples,double percentile){
  if(samples.isEmpty()||!Double.isFinite(percentile)||percentile<0||percentile>100)throw new IllegalArgumentException("Invalid percentile input");
  List<Long> sorted=new ArrayList<>(samples);Collections.sort(sorted);
  return sorted.get(Math.max(0,(int)Math.ceil(percentile/100*sorted.size())-1));
 }
 public static void printComparisonTable(BenchmarkResult sync,BenchmarkResult async){
  System.out.printf("%-20s %10s %12s %10s %10s %10s %10s%n","Service","Ops","Success/s","Mean ms","P95 ms","P99 ms","Errors %");
  for(var result:List.of(sync,async))System.out.printf("%-20s %10d %12.2f %10.3f %10.3f %10.3f %10.2f%n",result.name(),result.totalOperations(),result.opsPerSecond(),result.avgLatencyMs(),result.p95LatencyMs(),result.p99LatencyMs(),result.errorRatePercent());
 }
}
