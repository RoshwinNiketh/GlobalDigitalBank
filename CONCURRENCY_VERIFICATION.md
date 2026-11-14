# Activities 27–31 verification

Each activity was implemented on its own branch, tested before pushing, and used as the starting point for the next activity.

| Activity | Test drivers passed before push |
| --- | --- |
| 27 | 14 |
| 28 | 15 |
| 29 | 16 |
| 30 | 17 |
| 31 | 18 |

Run all current test drivers with assertions, isolated working files, and a 60-second process limit:

```powershell
powershell -ExecutionPolicy Bypass -File scripts/run-tests.ps1
```

Maven compiles the project; the script explicitly executes every `Test*.java` main because these are standalone acceptance drivers, not JUnit tests. Process output is saved under `target/acceptance-work`.

Checks include exact balances and ledger counts, ordered transfers, independent-account progress, atomic ID uniqueness, Future cancellation, batch timeouts, executor shutdown, CompletableFuture error propagation and aggregation, timeout isolation, and benchmark error accounting.

Activity 31's in-memory comparison used 1,000 completed deposits and concurrency 10 after warmup. Both implementations had zero errors. Measured P95 was 0.197 ms synchronous and 0.151 ms async, below the activity's 100 ms threshold. Timings are environment-dependent and do not establish production throughput or persistence-backend SLAs.

## Scope

Call operations through the synchronized or locked service. Account objects returned by the legacy repositories remain mutable; callers must not mutate them concurrently outside the service. Locked service instances sharing the same repository object share a lock registry. Independent repository instances or JVMs require storage-level coordination.

Timeouts bound observation, not the execution of an already accepted banking operation, and do not reverse a completed transaction. Failed futures remain failures unless the caller explicitly attaches a fallback.

Legacy service/UI demonstration tests now inject in-memory repositories to match their live-object assumptions and avoid depending on application persistence settings. Persistence-specific drivers continue to test SQLite, CSV, recovery, and pooling separately.
