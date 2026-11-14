package com.gdb.concurrency;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;
public final class AccountLockRegistry {
 private final ConcurrentHashMap<Integer,ReentrantLock> locks=new ConcurrentHashMap<>();
 private final ConcurrentHashMap<Integer,ReentrantReadWriteLock> reads=new ConcurrentHashMap<>();
 public ReentrantLock getLock(int a) { return locks.computeIfAbsent(a,k->new ReentrantLock()); }
 public ReentrantLock lockFor(int a) { return getLock(a); }
 public ReentrantReadWriteLock rwLockFor(int a) { return reads.computeIfAbsent(a,k->new ReentrantReadWriteLock()); }
}
