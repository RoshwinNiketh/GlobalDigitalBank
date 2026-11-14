package com.gdb.tests;
import com.gdb.concurrency.ConcurrentDepositDemo;
import com.gdb.service.*;
import com.gdb.repository.*;
import com.gdb.domain.*;
public class TestConcurrency {
 public static void main(String[] args) throws Exception { testDeposits(); testTransfers(); System.out.println("Activity 27 PASSED"); }
 static void check(boolean c) { if(!c) throw new AssertionError("Concurrency invariant failed"); }
 static ThreadSafeAccountService service() { return new ThreadSafeAccountService(new InMemoryAccountRepository(),new InMemoryTransactionRepository()); }
 static void testDeposits() throws Exception {
  var s=service(); int a=s.openAccount("SAVINGS","Depositor",30,10000).getAccountNumber();
  check(ConcurrentDepositDemo.runSafeDemo(s,a,10,100,100)==110000);
  check(s.getTransactionRecords().size()==1000);
 }
 static void testTransfers() throws Exception {
  var s=service(); int a=s.openAccount("SAVINGS","Sender",30,100000).getAccountNumber(); int b=s.openAccount("SAVINGS","Receiver",30,100000).getAccountNumber();
  s.setPin(a,1234); s.setPin(b,1234);
  ConcurrentDepositDemo.parallel(10,()->{for(int i=0;i<50;i++){s.transfer(a,b,1,1234);s.transfer(b,a,1,1234);}});
  check(s.getAccount(a).getBalance()==100000 && s.getAccount(b).getBalance()==100000);
  check(s.getTransactionRecords().size()==1000);
 }
}
