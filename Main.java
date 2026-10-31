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
        Task newTask = new Task(title, time, 5);
        taskQueue.add(newTask);
        return taskQueue;
    }

    public static void main(String[] args) {
        System.out.println("Round Robin Schedule Simulation");

        Queue<Task> taskQueue = new ArrayDeque<>();
        taskQueue = create(taskQueue);
        for (Task task : taskQueue) {
            task.execute();
        }
    }
}
