package com.gdb.service;
import com.gdb.domain.*;
import com.gdb.repository.*;
import com.gdb.logging.*;
/** All access through one service instance shares its monitor. */
public class ThreadSafeAccountService extends AccountService {
 public ThreadSafeAccountService(AccountRepository a, TransactionRepository t) { super(a,t,new TransactionLogger(new MemoryLogDestination())); }
 @Override public synchronized Transaction deposit(int a,double v) throws Exception { return super.deposit(a,v); }
 @Override public synchronized Transaction withdraw(int a,double v,int p) throws Exception { return super.withdraw(a,v,p); }
 @Override public synchronized Transaction transfer(int a,int b,double v,int p) throws Exception { return super.transfer(a,b,v,p); }
 @Override public synchronized IAccount openAccount(String t,String n,int age,double v) throws com.gdb.exceptions.AccountException { return super.openAccount(t,n,age,v); }
 @Override public synchronized IAccount getAccount(int a) { return super.getAccount(a); }
 @Override public synchronized void setPin(int a,int p) throws com.gdb.exceptions.AccountException { super.setPin(a,p); }
 @Override public synchronized void closeAccount(int a,int p) throws com.gdb.exceptions.AccountException { super.closeAccount(a,p); }
 @Override public synchronized java.util.List<IAccount> getAllAccounts() { return super.getAllAccounts(); }
 @Override public synchronized java.util.List<Transaction> getTransactionRecords() { return super.getTransactionRecords(); }
 public synchronized double getBalance(int a,int p) throws Exception { IAccount x=getAccount(a); if(x==null || !x.verifyPin(p)) throw new com.gdb.exceptions.InvalidPinException("Invalid account or PIN"); return x.getBalance(); }
}
