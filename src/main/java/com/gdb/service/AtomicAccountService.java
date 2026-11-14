package com.gdb.service;
import java.util.concurrent.atomic.*;
public final class AtomicAccountService {
 private final AtomicLong counter=new AtomicLong(1000),volume=new AtomicLong();
 private final AtomicReference<String> status=new AtomicReference<>("RUNNING");
 public String nextTransactionId() { return "ATOMIC-TXN-"+counter.incrementAndGet(); }
 public long recordDepositVolume(double amount) {
  if(!Double.isFinite(amount)||amount<0) throw new IllegalArgumentException("Invalid amount");
  long cents=java.math.BigDecimal.valueOf(amount).movePointRight(2).setScale(0,java.math.RoundingMode.HALF_UP).longValueExact();
  return volume.updateAndGet(current->Math.addExact(current,cents));
 }
 public long getGlobalTransactionCount(){return counter.get();}
 public double getTotalDepositVolume(){return volume.get()/100.0;}
 public boolean updateSystemStatus(String expected,String updated){return status.compareAndSet(expected,java.util.Objects.requireNonNull(updated));}
 public String getSystemStatus(){return status.get();}
}
