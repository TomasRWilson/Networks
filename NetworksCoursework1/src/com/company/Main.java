package com.company;
import javax.sound.sampled.LineUnavailableException;
import java.util.Objects;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws LineUnavailableException {
        boolean running = true;
        Scanner scan = new Scanner(System.in);
        Sender s;
        Receiver r;
        int x;
        String input;
        while(running) {
            System.out.println("Please enter which scenario to use: ");
            x = scan.nextInt();
            switch (x){
                case 2:
                    System.out.println("Scenario 2:");
                    s = new Sender2();
                    r = new Receiver2();

                    r.start();
                    s.start();
                    break;
                case 3:
                    System.out.println("Scenario 3:");
                    s = new Sender3();
                    r = new Receiver3();

                    r.start();
                    s.start();
                    break;
                case 4:
                    System.out.println("Scenario 4:");
                    s = new Sender4();
                    r = new Receiver4();

                    r.start();
                    s.start();
                    break;
                case 5:
                    System.out.println("Scenario 5:");
                    s = new Sender();
                    r = new ReceiverNoDecrypt();

                    r.start();
                    s.start();
                    break;
                default:
                    System.out.println("Scenario 1:");
                    s = new Sender();
                    r = new Receiver();

                    r.start();
                    s.start();
            }
            scan.nextLine();
            input = scan.nextLine();
            r.stop();
            s.stop();
            if(input.equals("exit")){
                running = false;
            }
        }
    }


}
