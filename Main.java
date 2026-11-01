import java.util.ArrayDeque;
import java.util.InputMismatchException;
import java.util.Queue;
import java.util.Scanner;

class Main {

    public static Scanner sc = new Scanner(System.in);

    static double quantumCreate(Queue<Task> taskQueue) {
        if (taskQueue.isEmpty()) {
            return 0.0;
        }
        double sum = taskQueue
            .stream()
            .mapToDouble(t -> t.time)
            .sum();

        return sum / taskQueue.size();
    }

    static double invalidTimeHandling() {
        double time = 0.0;
        while (true) {
            try {
                time = sc.nextInt();

                if (time <= 0) {
                    throw new NegativeTimeError("Negative Time Not Allowed");
                } else {
                    break;
                }
            } catch (InputMismatchException e) {
                System.out.println("Input Mismatch Found, Try Again...");
                sc.next();
                continue;
            } catch (NegativeTimeError e) {
                System.out.println("Negative Time Found, Try Again...");
                continue;
            }
        }
        sc.nextLine();
        return time;
    }

    static String invalidTitleHandling() {
        String title;
        do {
            title = sc.nextLine();
        } while (title.isEmpty());
        return title;
    }

    static Queue<Task> create(Queue<Task> taskQueue) {
        double time;
        String title;

        System.out.print("Enter the Task Title: ");
        title = invalidTitleHandling();
        
        System.out.print("Enter the time to execute: ");
        time = invalidTimeHandling();

        Task newTask = new Task(title, time);
        taskQueue.add(newTask);

        return taskQueue;
    }

    static Queue<Task> choice(Queue<Task> taskQueue) {
        boolean isRunning = true;
        String choice = "Y";

        do {
            taskQueue = create(taskQueue);
            System.out.print("Do you want to add more? (Y/n): ");
            choice = sc.nextLine();
            isRunning =
                choice.startsWith("Y") || choice.startsWith("y") ? true : false;
        } while (isRunning);

        return taskQueue;
    }

    static void executionOfAlgorithm(Queue<Task> taskQueue) {
        while (!taskQueue.isEmpty()) {
            Task task = taskQueue.poll();

            task.execute();

            if (!task.isCompleted) {
                task.time -= task.quantum;
                System.out.printf(
                    "Quantum Ended & Task Inserted at the EOQ with %.2f seconds\n",
                    task.time / 1000
                );
                taskQueue.offer(task);
            }
        }
    }

    public static void main(String[] args) {
        Queue<Task> taskQueue = new ArrayDeque<>();

        System.out.println("Round Robin Schedule Simulation");
        System.out.println("Add tasks to make the simulation");

        taskQueue = choice(taskQueue);

        double quantum = quantumCreate(taskQueue);
        taskQueue.forEach(task -> task.quantum = quantum);

        System.out.println("Now lets start the simulation");
        executionOfAlgorithm(taskQueue);
    }
}
