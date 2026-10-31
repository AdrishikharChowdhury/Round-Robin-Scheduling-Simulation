/**
 * Task
 */
public class Task {

    final String title;
    int time, quantum;

    public Task(String title, int time, int quantum) {
        this.title = title;
        this.time = time * 1000;
        this.quantum = quantum * 1000;
        System.out.printf(
            "The Task is %s and set for %d seconds\n",
            title,
            time
        );
    }

    void execute() {
        Timer t1;
        t1 = new Timer(this.quantum);
        System.out.println(
            this.title +
                " task is started and running for " +
                this.quantum / 1000 +
                " seconds"
        );
        t1.run();
    }
}
