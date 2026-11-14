package com.gdb.concurrency;
import java.util.concurrent.locks.Lock;
public final class AccountLock implements AutoCloseable {
 private final Lock lock;
 public AccountLock(Lock lock) { this.lock=lock; lock.lock(); }
 public void close() { lock.unlock(); }
}
