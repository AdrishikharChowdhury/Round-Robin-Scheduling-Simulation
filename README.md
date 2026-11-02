# Round-Robin-Scheduling-Simulation

A Java console simulation of Round Robin CPU scheduling. Tasks are added interactively, a quantum is derived as the average of all task times, and the scheduler drains the queue by executing each task for one quantum and re-queueing incomplete tasks.

## Files

- `Main.java` — entry point; task input, quantum creation, scheduling loop
- `Task.java` — task model with `execute()` slicing logic
- `Timer.java` — blocking timer used by each slice
- `NegativeTimeError.java` — custom exception for non-positive times

## Build & Run

```bash
javac *.java
java Main
```

Requires OpenJDK 27 (verified).

## Usage

1. Enter a task title (non-empty).
2. Enter its execution time in seconds (positive integer).
3. Choose whether to add more tasks (`Y`/`n`).
4. The quantum is computed as the mean of all task times (in milliseconds).
5. The scheduler runs each task for `min(task time, quantum)` and re-queues unfinished tasks until the queue is empty.

## Behavior

- Slicing is clamped to remaining time, so no task overruns its quantum.
- Incomplete tasks are re-queued at the tail of the queue.
- Input is validated for non-positive times and mismatched types.
