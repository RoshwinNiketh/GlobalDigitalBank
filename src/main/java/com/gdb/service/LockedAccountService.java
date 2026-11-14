package com.gdb.service;
import com.gdb.concurrency.*;
import com.gdb.domain.*;
import com.gdb.repository.*;
import com.gdb.logging.*;
/** Services sharing the same repository object share their lock registry. */
public class LockedAccountService extends AccountService {
 private static final java.util.Map<AccountRepository,AccountLockRegistry> REGISTRIES=new java.util.WeakHashMap<>();
 private final AccountLockRegistry registry;
 private final AccountRepository accounts;
 public LockedAccountService(AccountRepository a,TransactionRepository t) {
  super(a,t,new TransactionLogger(new MemoryLogDestination())); accounts=a;
  synchronized(REGISTRIES) { registry=REGISTRIES.computeIfAbsent(a,k->new AccountLockRegistry()); }
 }
 @Override public Transaction deposit(int a,double v) throws Exception { try(var lock=new AccountLock(registry.getLock(a)); var write=new AccountLock(registry.rwLockFor(a).writeLock())) { return super.deposit(a,v); } }
 @Override public Transaction withdraw(int a,double v,int p) throws Exception { try(var lock=new AccountLock(registry.getLock(a)); var write=new AccountLock(registry.rwLockFor(a).writeLock())) { return super.withdraw(a,v,p); } }
 @Override public void setPin(int a,int p) throws com.gdb.exceptions.AccountException { try(var lock=new AccountLock(registry.getLock(a)); var write=new AccountLock(registry.rwLockFor(a).writeLock())) { super.setPin(a,p); } }
 @Override public void closeAccount(int a,int p) throws com.gdb.exceptions.AccountException { try(var lock=new AccountLock(registry.getLock(a)); var write=new AccountLock(registry.rwLockFor(a).writeLock())) { super.closeAccount(a,p); } }
 @Override public Transaction transfer(int a,int b,double v,int p) throws Exception {
  if(a==b || !Double.isFinite(v) || v<=0) throw new IllegalArgumentException("Invalid transfer");
  int first=Math.min(a,b),second=Math.max(a,b);
  try(var l1=new AccountLock(registry.getLock(first)); var l2=new AccountLock(registry.getLock(second));
      var w1=new AccountLock(registry.rwLockFor(first).writeLock()); var w2=new AccountLock(registry.rwLockFor(second).writeLock())) {
   return super.transfer(a,b,v,p);
  }
 }
 @Override public IAccount getAccount(int a) { try(var read=new AccountLock(registry.rwLockFor(a).readLock())) { return super.getAccount(a); } }
 public double getBalance(int a,int p) throws Exception { try(var read=new AccountLock(registry.rwLockFor(a).readLock())) {
  IAccount x=super.getAccount(a); if(x==null || !x.verifyPin(p)) throw new com.gdb.exceptions.InvalidPinException("Invalid account or PIN"); return x.getBalance();
 } }
 public void saveAccount(IAccount a) { try(var lock=new AccountLock(registry.getLock(a.getAccountNumber())); var write=new AccountLock(registry.rwLockFor(a.getAccountNumber()).writeLock())) { accounts.save(a); } }
 @Override public IAccount openAccount(String t,String n,int age,double v) throws com.gdb.exceptions.AccountException {
  synchronized(registry) { return super.openAccount(t,n,age,v); }
 }
}
