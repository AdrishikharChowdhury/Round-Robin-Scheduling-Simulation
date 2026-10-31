class Timer implements Runnable {

    double sec;

    Timer(double sec) {
        this.sec = sec;
        System.out.printf("Timer set for %.2f seconds\n", sec/1000);
    }

    @Override
    public void run() {
        try {
            Thread.sleep((long) this.sec);
            System.out.println("Timer Ended");
        } catch (InterruptedException e) {
            System.out.println("Timer Interrupted");
        }
    }
}
