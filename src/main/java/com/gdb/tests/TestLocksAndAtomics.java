package com.gdb.tests;
import com.gdb.concurrency.*;
import com.gdb.service.*;
import com.gdb.repository.*;
import java.util.concurrent.*;
public class TestLocksAndAtomics {
 public static void main(String[] args) throws Exception { testMoney(); testIndependentAccounts(); testAtomics(); testRelease(); System.out.println("Activity 28 PASSED"); }
 static void testMoney() throws Exception {
  var a=new InMemoryAccountRepository(); var t=new InMemoryTransactionRepository(); var s=new LockedAccountService(a,t);
  int x=s.openAccount("SAVINGS","First",30,100000).getAccountNumber(), y=s.openAccount("SAVINGS","Second",30,100000).getAccountNumber();
  s.setPin(x,1234);s.setPin(y,1234);
  ConcurrentDepositDemo.parallel(10,()->{for(int i=0;i<100;i++)s.deposit(x,100);});
  TestConcurrency.check(s.getAccount(x).getBalance()==200000);
  ConcurrentDepositDemo.parallel(10,()->{for(int i=0;i<50;i++){s.transfer(x,y,1,1234);s.transfer(y,x,1,1234);}});
  TestConcurrency.check(s.getAccount(x).getBalance()==200000 && s.getAccount(y).getBalance()==100000 && t.findAll().size()==2000);
 }
 static void testIndependentAccounts() throws Exception {
  CountDownLatch entered=new CountDownLatch(2);
  var repo=new InMemoryAccountRepository(){ @Override public com.gdb.domain.IAccount findById(int n){
   entered.countDown(); try {if(!entered.await(3,TimeUnit.SECONDS))throw new AssertionError("Independent accounts blocked");}catch(InterruptedException e){throw new RuntimeException(e);} return super.findById(n);
  }};
  var s=new LockedAccountService(repo,new InMemoryTransactionRepository());
  s.saveAccount(com.gdb.domain.AccountFactory.createAccount("SAVINGS",1,"A",30,10000)); s.saveAccount(com.gdb.domain.AccountFactory.createAccount("SAVINGS",2,"B",30,10000));
  var ids=new java.util.concurrent.atomic.AtomicInteger(); ConcurrentDepositDemo.parallel(2,()->s.deposit(ids.incrementAndGet(),1));
 }
 static void testAtomics() throws Exception {
  var s=new AtomicAccountService(); var ids=ConcurrentHashMap.<String>newKeySet();
  ConcurrentDepositDemo.parallel(10,()->{for(int i=0;i<100;i++){ids.add(s.nextTransactionId());s.recordDepositVolume(0.01);}});
  TestConcurrency.check(ids.size()==1000 && s.getGlobalTransactionCount()==2000 && s.getTotalDepositVolume()==10);
  TestConcurrency.check(s.updateSystemStatus("RUNNING","STOPPED") && !s.updateSystemStatus("RUNNING","BAD"));
 }
 static void testRelease(){var registry=new AccountLockRegistry(); var lock=registry.getLock(1);try(var held=new AccountLock(lock)){TestConcurrency.check(lock.isHeldByCurrentThread());}TestConcurrency.check(!lock.isLocked());}
}
