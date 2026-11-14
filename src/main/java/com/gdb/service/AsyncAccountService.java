package com.gdb.service;
import com.gdb.domain.*;
import com.gdb.repository.*;
import com.gdb.concurrency.ThreadPoolFactory;
import java.util.*;
import java.util.concurrent.*;
public class AsyncAccountService implements AutoCloseable {
 protected final LockedAccountService service;
 protected final ExecutorService executor;
 public AsyncAccountService(AccountRepository a,TransactionRepository t,int size){service=new LockedAccountService(a,t);executor=ThreadPoolFactory.newFixedPool(size);}
 @FunctionalInterface protected interface Operation<T>{T run() throws Exception;}
 protected <T> CompletableFuture<T> submit(Operation<T> operation){
  try{return CompletableFuture.supplyAsync(()->{try{return operation.run();}catch(Exception e){throw new CompletionException(e);}},executor);}
  catch(RejectedExecutionException error){return CompletableFuture.failedFuture(error);}
 }
 public CompletableFuture<IAccount> openAccountAsync(String type,String name,int age,double balance){return submit(()->service.openAccount(type,name,age,balance));}
 public CompletableFuture<IAccount> openAccountAsync(String type,int number,String name,int age,double balance){return submit(()->{IAccount a=AccountFactory.createAccount(type,number,name,age,balance);service.saveAccount(a);return a;});}
 public CompletableFuture<Transaction> depositAsync(int a,double v){return submit(()->service.deposit(a,v));}
 public CompletableFuture<Transaction> withdrawAsync(int a,double v,int p){return submit(()->service.withdraw(a,v,p));}
 public CompletableFuture<Transaction> transferAsync(int a,int b,double v,int p){return submit(()->service.transfer(a,b,v,p));}
 public CompletableFuture<Double> getBalanceAsync(int a,int p){return submit(()->service.getBalance(a,p));}
 public CompletableFuture<IAccount> getAccountAsync(int a){return submit(()->{IAccount found=service.getAccount(a);if(found==null)throw new IllegalArgumentException("Unknown account");return found;});}
 public CompletableFuture<Void> setPinAsync(int a,int p){return submit(()->{service.setPin(a,p);return null;});}
 public CompletableFuture<Double> executePipelineAsync(int a,double deposit,double withdraw,int pin){return depositAsync(a,deposit).thenCompose(t->withdrawAsync(a,withdraw,pin)).thenCompose(t->getBalanceAsync(a,pin));}
 public CompletableFuture<List<Transaction>> depositAllAsync(List<Integer> numbers,double amount){
  var futures=numbers.stream().map(a->depositAsync(a,amount)).toList();
  return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).thenApply(ignored->futures.stream().map(CompletableFuture::join).toList());
 }
 public CompletableFuture<Double> getTotalBalanceAsync(List<Integer> numbers){
  var futures=numbers.stream().map(this::getAccountAsync).toList();
  return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).thenApply(ignored->futures.stream().map(CompletableFuture::join).mapToDouble(IAccount::getBalance).sum());
 }
 public void saveAccount(IAccount account){service.saveAccount(account);}
 public IAccount getAccount(int a){return service.getAccount(a);}
 public void shutdown(){ThreadPoolFactory.shutdownGracefully(executor,3000);}
 public void close(){shutdown();}
}
