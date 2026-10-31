import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Scanner;

class Main {

    public static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("Round Robin Schedule Simulation");

        Queue<Task> taskQueue = new ArrayDeque<>();
        Task newTask = new Task("Hello World", 5, 5);
        taskQueue.add(newTask);
        for (Task task : taskQueue) {
            task.execute();
        }
    }
}
