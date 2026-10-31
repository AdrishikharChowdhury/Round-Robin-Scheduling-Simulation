public class Task {

    final String title;
    double time, quantum;
    boolean isCompleted;

    public Task(String title, double time, double quantum) {
        this.title = title;
        this.time = time * 1000;
        this.quantum = quantum * 1000;
        this.isCompleted = false;
        System.out.printf(
            "The Task is %s and set for %.2f seconds\n",
            title,
            time
        );
    }

    void execute() {
        Timer timer;
        boolean taskNearOver = this.quantum < this.time ? false : true;
        double time = taskNearOver ? this.time : this.quantum;

        timer = new Timer(time);
        System.out.printf(
            "%s task is started and running for %.2f seconds\n",
            this.title,
            time / 1000
        );
        timer.run();
        this.isCompleted = taskNearOver ? true : false;
    }
}
