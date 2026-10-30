class Timer implements Runnable{
    int sec;
    Timer(int sec){
        this.sec=sec;
        System.out.printf("Timer set for %d seconds\n",sec);

    }
    @Override
    public void run(){
        try {
         Thread.sleep(this.sec*1000);
         System.out.println("Timer Ended");
        } catch (InterruptedException e) {
           System.out.println("Timer Interrupted");
        }
    }
}