import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Scanner;

class Main {

    public static Scanner sc = new Scanner(System.in);

    static Queue<Task> create(Queue<Task> taskQueue) {
        System.out.print("Enter the Task Title: ");
        String title = sc.nextLine();
        System.out.print("Enter the time to execute: ");
        int time = sc.nextInt();
        sc.nextLine();
        Task newTask = new Task(title, time, 5);
        taskQueue.add(newTask);
        return taskQueue;
    }

    public static void main(String[] args) {
        System.out.println("Round Robin Schedule Simulation");
        boolean isRunning = true;
        String choice = "Y";
        Queue<Task> taskQueue = new ArrayDeque<>();
        do {
            taskQueue = create(taskQueue);
            System.out.println("Do you want to conitnue: (Y/n)");
            choice = sc.nextLine();
            isRunning =
                choice.startsWith("Y") || choice.startsWith("y") ? true : false;
        } while (isRunning);
        for (Task task : taskQueue) {
            task.execute();
            taskQueue.remove(task);
        }
    }
}
