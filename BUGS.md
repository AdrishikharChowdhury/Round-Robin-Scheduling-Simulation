# Bugs — Round Robin Scheduling Simulation

**Status:** No known critical bugs. Slicing and completion logic are correct, and the scheduler now drains the queue by re-queueing incomplete tasks with `poll()` / `offer()`.

**Verified on:** OpenJDK 27.

---

## Verification

### Test 1 — A = 1s, B = 2s (quantum = 1.5s)

| Order | Task | Slice | Remaining | Result |
|-------|------|-------|-----------|--------|
| 1 | A | 1.0s | 0 | Completed |
| 2 | B | 1.5s | 0.5s | Re-queued |
| 3 | B | 0.5s | 0 | Completed |

### Test 2 — A = 3s, B = 1s, C = 2s (quantum = 2.0s)

| Order | Task | Slice | Remaining | Result |
|-------|------|-------|-----------|--------|
| 1 | A | 2.0s | 1.0s | Re-queued |
| 2 | B | 1.0s | 0 | Completed |
| 3 | C | 2.0s | 0 | Completed |
| 4 | A | 1.0s | 0 | Completed |

The queue is empty after both runs, and the execution order is correct round-robin.

---

## Fixed

### 1. ~~Slice logic inverted~~ — FIXED
- **Location:** `Task.java:21-22`
- `taskNearOver` is now true when `quantum >= time`.
- The slice is remaining time when the task fits, otherwise the quantum: effectively `min(time, quantum)`.

### 2. ~~Completion flag inverted~~ — FIXED
- **Location:** `Task.java:31`
- `isCompleted` is true exactly when the task ran its full remaining time during that slice.
- Tasks needing another quantum are correctly marked incomplete.

### 3. ~~Queue modified during iteration~~ — FIXED
- **Location:** `Main.java:53-63`
- Replaced the enhanced `for` loop with `while (!taskQueue.isEmpty())` using `poll()` and `offer()`.
- No iterator is invalidated; re-queued tasks are processed correctly.
- Verified with tasks that need multiple quanta (Test 2).

### 4. ~~Remaining time could go negative~~ — FIXED
- **Location:** `Main.java:58-62`
- Subtraction now happens only when a full quantum was executed.
- The final slice uses the remaining time and completes the task without subtracting.

### 5. ~~Unfinished tasks left in the queue~~ — FIXED
- The loop continues until the queue is empty; no tasks are dropped.
- Verified: both test runs end with an empty queue.

---

## Design notes (not correctness bugs)

### 6. Average quantum is input-dependent
- **Location:** `Main.java:48-49`
- Quantum is the mean of all task times. Slicing is clamped correctly, so tasks are not overrun, but standard round-robin uses a fixed/configurable quantum.
- Behaviour changes with the submitted task set: short tasks finish in their first slice while longer tasks use the average.

### 7. Timer blocks the main thread
- **Location:** `Task.java:30`, `Timer.java:11-13`
- `timer.run()` is called directly instead of starting a thread, so each slice blocks `main`.
- `Timer implements Runnable` is never used as a thread; the interrupt handling is dead code.
- Acceptable for a sequential simulation, but there is no real interleaving or preemption.

---

## Minor

### 8. Units are misleading and handled in two places
- `Task` stores milliseconds (`time * 1000`, `quantum * 1000`).
- `Timer.sec` actually holds **milliseconds**, while its name and `sec / 1000` output imply seconds.
- `Main` overwrites each task's quantum with the millisecond value from `quantumCreate()`, so the constructor's unit is not the source of truth.

### 9. Hardcoded quantum value is dead code
- **Location:** `Main.java:27`
- `new Task(title, time, 5)` passes quantum `5`, which is immediately overwritten at `Main.java:49`.

### 10. No input validation
- `sc.nextInt()` throws `InputMismatchException` on non-numeric input.
- Negative time produces negative sleeps; `Thread.sleep` with a negative value throws `IllegalArgumentException`.
- With non-negative input and all times `0`, the quantum is `0` but every task completes immediately, so there is no hang.
- A quantum ≤ 0 with positive remaining time can loop forever (reachable with negative input); reject `time <= 0` at entry.

### 11. State is not fully normalized
- Completed tasks keep a non-zero `time` field; the final slice is never written back as `0`.
- Task state is split between `Task` and `Main` (execution vs. remaining-time subtraction).
- The current split works, but it is fragile if the slice logic changes.

### 12. Redundant boolean expressions
- **Location:** `Main.java:45`, `Task.java:31`
- `condition ? true : false` is equivalent to `condition`.

### 13. Redundant local variable
- **Location:** `Main.java:60`
- `Task newTask = task;` adds nothing; `taskQueue.offer(task);` is sufficient.

---

## Current core loop

```java
while (!taskQueue.isEmpty()) {
    Task task = taskQueue.poll();

    task.execute();                 // executes min(task.time, task.quantum)

    if (!task.isCompleted) {
        task.time -= task.quantum;  // slice == quantum in this branch
        taskQueue.offer(task);      // back to the tail
    }
}
```
