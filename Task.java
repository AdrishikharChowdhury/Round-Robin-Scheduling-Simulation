public class Task {

    final String title;
    double time, quantum;

    public Task(String title, double time, double quantum) {
        this.title = title;
        this.time = time * 1000;
        this.quantum = quantum * 1000;
        System.out.printf(
            "The Task is %s and set for %.2f seconds\n",
            title,
            time
        );
    }

    void execute() {
        Timer timer;
        double time = this.quantum > this.time ? this.time : this.quantum;
        timer = new Timer(time);
        System.out.printf(
            "%s task is started and running for %.2f seconds\n",
            this.title,
            time/1000
        );
        timer.run();
    }
}
