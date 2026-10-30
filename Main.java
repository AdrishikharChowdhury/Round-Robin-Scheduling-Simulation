import java.util.Scanner;
class Main{
    public static Scanner sc=new Scanner(System.in);
    public static void main(String[] args){
        System.out.println("Round Robin Schedule Simulation");
        Timer t1;
        int sec;
        System.out.print("Enter time period: ");
        sec=sc.nextInt();
        t1=new Timer(sec);
        t1.run();
    }
}