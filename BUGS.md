# Bugs — Round Robin Scheduling Simulation

**Status:** No known critical bugs. Slicing and completion logic are correct, and the scheduler drains the queue by re-queueing incomplete tasks with `poll()` / `offer()`.

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
- **Location:** `Task.java:20-21`
- `isCompleted` is true when `quantum >= time` (task fits in one quantum).
- The slice is `min(time, quantum)`: uses remaining time if fits, otherwise the quantum.

### 2. ~~Completion flag inverted~~ — FIXED
- **Location:** `Task.java:20`
- `isCompleted` is true exactly when the task runs its full remaining time in that slice.
- Tasks needing another quantum are correctly marked incomplete.

### 3. ~~Queue modified during iteration~~ — FIXED
- **Location:** `Main.java:86-100`
- Replaced iteration with `while (!taskQueue.isEmpty())` using `poll()` and `offer()`.
- No iterator is invalidated; re-queued tasks are processed correctly.

### 4. ~~Remaining time could go negative~~ — FIXED
- **Location:** `Main.java:91-99`
- Subtraction (`task.time -= task.quantum`) happens only when `!task.isCompleted`.
- The final slice uses remaining time and completes the task without subtracting.

### 5. ~~Unfinished tasks left in the queue~~ — FIXED
- The loop continues until the queue is empty; no tasks are dropped.

### 6. ~~No input validation~~ — FIXED
- **Location:** `Main.java:22-44`, `NegativeTimeError.java`
- `invalidTimeHandling()` validates input with `try/catch` for `InputMismatchException` and rejects non-positive times with `NegativeTimeError`.
- `invalidTitleHandling()` ensures non-empty titles.

### 7. ~~Hardcoded quantum value passed to constructor~~ — FIXED
- **Location:** `Task.java:7`, `Main.java:64, 110-111`
- `Task` constructor now takes only `(String title, double time)`. Quantum is assigned after all tasks are added via `taskQueue.forEach(task -> task.quantum = quantum)`.
- No dead `new Task(title, time, 5)` call remains.

---

## Design notes (not correctness bugs)

### 8. Average quantum is input-dependent
- **Location:** `Main.java:10-20, 110`
- Quantum is the mean of all task times. Slicing is clamped correctly, so tasks are not overrun, but standard round-robin uses a fixed/configurable quantum.
- Behaviour changes with the submitted task set: short tasks finish in their first slice while longer tasks use the average.

### 9. Timer blocks the main thread
- **Location:** `Task.java:29`, `Timer.java:10-18`
- `timer.run()` is called directly instead of starting a thread, so each slice blocks `main`.
- `Timer implements Runnable` is never used as a thread; the interrupt handling is dead code.
- Acceptable for a sequential simulation, but there is no real interleaving or preemption.

---

## Minor

### 10. Units are misleading and handled in two places
- `Task` stores milliseconds (`time * 1000`, quantum set in milliseconds).
- `Timer.sec` actually holds **milliseconds**, while its name and `sec / 1000` output imply seconds.
- `Main` overwrites each task's quantum with the millisecond value from `quantumCreate()`, so the constructor's unit is not the source of truth.

### 11. State is not fully normalized
- Completed tasks keep a non-zero `time` field; the final slice is never written back as `0`.
- Task state is split between `Task` and `Main` (execution vs. remaining-time subtraction).
- The current split works, but it is fragile if the slice logic changes.

### 12. Redundant boolean expressions
- **Location:** `Main.java:78-80`
- `choice.startsWith("Y") || choice.startsWith("y") ? true : false` is equivalent to `choice.startsWith("Y") || choice.startsWith("y")`.

### 13. Redundant local variable
- **Location:** `Main.java:64`
- `Task newTask = new Task(title, time); taskQueue.add(newTask);` could be simplified to `taskQueue.add(new Task(title, time));`.

---

## Current core loop

```java
while (!taskQueue.isEmpty()) {
    Task task = taskQueue.poll();

    task.execute();                 // executes min(task.time, task.quantum)

    if (!task.isCompleted) {
        task.time -= task.quantum;  // slice == quantum in this branch
        System.out.printf(
            "Quantum Ended & Task Inserted at the EOQ with %.2f seconds\n",
            task.time / 1000
        );
        taskQueue.offer(task);      // back to the tail
    }
}
```
