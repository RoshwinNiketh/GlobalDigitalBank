package com.gdb.service;
import com.gdb.concurrency.ThreadPoolFactory;
import com.gdb.domain.*;
import com.gdb.repository.*;
import java.util.*;
import java.util.concurrent.*;
public final class ExecutorAccountService implements AutoCloseable {
 private final LockedAccountService service;
 private final ExecutorService executor;
 public ExecutorAccountService(AccountRepository a,TransactionRepository t,int size){this(new LockedAccountService(a,t),ThreadPoolFactory.newFixedPool(size));}
 /** This facade owns the supplied executor and shuts it down on close. */
 public ExecutorAccountService(LockedAccountService service,ExecutorService executor){this.service=Objects.requireNonNull(service);this.executor=Objects.requireNonNull(executor);}
 public Future<Transaction> depositAsync(int a,double v){return executor.submit(()->service.deposit(a,v));}
 public Future<Transaction> withdrawAsync(int a,double v,int p){return executor.submit(()->service.withdraw(a,v,p));}
 public Future<Transaction> transferAsync(int a,int b,double v,int p){return executor.submit(()->service.transfer(a,b,v,p));}
 public Future<Double> getBalanceAsync(int a,int p){return executor.submit(()->service.getBalance(a,p));}
 public <T> List<Future<T>> invokeAllTasks(List<Callable<T>> tasks,long timeout,TimeUnit unit)throws InterruptedException{return executor.invokeAll(tasks,timeout,unit);}
 public void saveAccount(IAccount a){service.saveAccount(a);}
 public IAccount getAccount(int a){return service.getAccount(a);}
 public boolean isShutdown(){return executor.isShutdown();}
 public void shutdown(){ThreadPoolFactory.shutdownGracefully(executor,3000);}
 public void close(){shutdown();}
}
