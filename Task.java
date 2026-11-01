public class Task {

    final String title;
    double time, quantum;
    boolean isCompleted;

    public Task(String title, double time) {
        this.title = title;
        this.time = time * 1000;
        this.isCompleted = false;
        System.out.printf(
            "The Task is %s and set for %.2f seconds\n",
            title,
            time
        );
    }

    void execute() {
        Timer timer;
        this.isCompleted = this.quantum < this.time ? false : true;
        double time = this.isCompleted ? this.time : this.quantum;

        timer = new Timer(time);
        System.out.printf(
            "%s task is started and running for %.2f seconds\n",
            this.title,
            time / 1000
        );
        timer.run();
    }
}
