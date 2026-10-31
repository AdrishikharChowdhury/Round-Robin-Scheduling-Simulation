# Bugs — Round Robin Scheduling Simulation

**Status:** Compiles, but does not perform round-robin scheduling. The quantum is never enforced correctly, and tasks can be left unfinished.

**Verified on:** OpenJDK 27, with tasks `A = 1s`, `B = 2s` (computed quantum = 1.5s)

| Task | Quantum | Expected slice | Actual slice | Outcome |
|------|---------|----------------|--------------|---------|
| A | 1.5s | 1.0s | 1.5s | Overran, marked incomplete, left in queue |
| B | 2.0s | 1.5s | 2.0s | No preemption, marked complete |

---

## Critical

### 1. Quantum is never enforced — slice logic is inverted
- **Location:** `Task.java:21-22`
- `taskNearOver` evaluates to `quantum <= time`, i.e. `true` when the task needs **more** time.
- The slice is then `this.time` for long tasks (runs to completion, no preemption) and `this.quantum` for short tasks (runs longer than the task exists).
- The quantum is never used as a cap; only equality behaves correctly.
- **Fix:** `double slice = Math.min(this.time, this.quantum);`

### 2. Completion flag is inverted
- **Location:** `Task.java:32`
- `isCompleted` is set from `taskNearOver`, so it becomes `true` when `quantum <= time` — exactly when the task still has work left.
- It is decided **before** the remaining time is reduced.
- **Fix:** subtract the actual slice first, then set `isCompleted = time <= 0`.

### 3. Queue is modified while iterating it
- **Location:** `Main.java:53-62`
- `add()` and `remove()` are called inside an enhanced `for` loop over the same `ArrayDeque`.
- On JDK 27 the iterator uses a stale size snapshot: re-enqueued tasks are **never revisited** and remain in the queue at the end (observed with task A).
- On older JDKs this typically throws `ConcurrentModificationException`.
- **Fix:** iterate with a `while` loop using `poll()` and `offer()`.

### 4. Remaining time is tracked incorrectly
- **Location:** `Main.java:56-57`
- `task.time -= task.quantum` runs after execution, based on a flag computed before execution.
- Remaining time can go negative (A ended at `-0.5s`), and is never clamped to zero.
- Combined with bug #2, fixing the loop alone would re-run short tasks forever.
- **Fix:** subtract the exact slice that was executed, clamp at zero, then decide completion.

---

## Major

### 5. Tasks are silently dropped / left in the queue
- **Location:** `Main.java:53-62`
- Consequence of bugs #2 and #3: the simulation can end with unfinished tasks still in `taskQueue`.
- No warning or summary of incomplete tasks is printed.

### 6. Average quantum is unsafe for short tasks
- **Location:** `Main.java:48-49`
- Quantum is the mean of all task times. Any task shorter than the average receives a slice longer than its remaining time unless clamped.
- Even with correct slicing, averaging makes the quantum depend on the input set. Standard round-robin uses a fixed/configurable quantum.

### 7. Timer blocks the main thread
- **Location:** `Task.java:30`, `Timer.java:11-13`
- `timer.run()` is called directly instead of starting a thread, so each slice blocks `main`.
- `Timer implements Runnable` is never used as a thread; the interrupt handling is dead code.
- Sequential simulation is acceptable, but there is no real interleaving or preemption.

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
- `time <= 0` produces zero or negative sleeps; a negative `Thread.sleep` argument throws `IllegalArgumentException`.
- All-zero times produce a quantum of `0`; the empty-queue case is guarded, but zero-length slices are not.

### 11. State is not normalized
- Completed tasks keep a non-zero `time` field.
- `isCompleted` is set before remaining time is updated.
- Task state is split between `Task` and `Main`, with no single source of truth.

### 12. Redundant boolean expressions
- **Location:** `Main.java:45`, `Task.java:32`
- `condition ? true : false` is equivalent to `condition`.

---

## Suggested core loop

```java
while (!taskQueue.isEmpty()) {
    Task task = taskQueue.poll();

    double slice = Math.min(task.time, task.quantum);
    task.execute(slice);            // sleep for slice, then task.time -= slice

    if (task.time > 0) {
        taskQueue.offer(task);      // not finished: back to the tail
    }
}
```
