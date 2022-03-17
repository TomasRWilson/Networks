package com.company;/*
 * Receiver3.java
 */

/**
 *
 * @author  Nathan Cook
 */

import CMPC3M06.AudioPlayer;
import uk.ac.uea.cmp.voip.DatagramSocket2;
import javax.sound.sampled.LineUnavailableException;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class Receiver3 extends Receiver implements Runnable{

    public static void selectionSort(ArrayList<byte[]> T) {

        int min, temp;
        byte[] list1, list2;

        for (int i = 0; i < T.size() - 1; i++) {
            min = i;


            for (int j = i + 1; j < T.size(); j++) {
                list1 = T.get(min);
                list2 = T.get(j);
                if (list2[1] < list1[1])
                    min = j;
            }
            if (i != min) {
                list1 = T.get(i);
                T.set(i,T.get(min));
                T.set(min,list1);
            }
        }
    }

    public Receiver3() throws LineUnavailableException {
    }

    public void run (){

        //***************************************************
        //Port to open socket on
        int PORT = 55555;
        InetAddress clientIP = null;
        try {
            clientIP = InetAddress.getByName("localhost");  //CHANGE localhost to IP or NAME of client machine
        } catch (UnknownHostException e) {
            System.out.println("ERROR: TextSender: Could not find client IP");
            e.printStackTrace();
            System.exit(0);
        }
        //***************************************************

        //***************************************************
        //Open a socket to receive from on port PORT

        //DatagramSocket receiving_socket;
        try{
            receiving_socket = new DatagramSocket2(PORT);
        } catch (SocketException e){
            System.out.println("ERROR: TextReceiver: Could not open UDP socket to receive from.");
            e.printStackTrace();
            System.exit(0);
        }
        //***************************************************
        System.out.println("You're ready to receive");
        //***************************************************
        //Main loop.
        Map<Short, byte[]> holder = new HashMap<>();
        int[] idCat = new int[50];
        idCat[0] = 0;
        int newNum = 1;
        ArrayList<byte[]> theSorter = new ArrayList<byte[]>();

        while (running){

            try{

                //Receive a DatagramPacket
                byte[] buffer = new byte[514];
                DatagramPacket packet = new DatagramPacket(buffer, 0, 514);
                receiving_socket.setSoTimeout(500);
                receiving_socket.receive(packet);

                byte[] tempByte = packet.getData();
                byte[] audioPart = new byte[512];
                idCat[newNum] = tempByte[1];
                //System.out.println(tempByte[1]);
                theSorter.add(packet.getData());
                /*
                if (idCat[newNum] != (idCat[newNum-1]+1)){
                    System.out.println("Missing Block");
                    receiving_socket.receive(packet);
                    Testes = packet.getData();
                    idCat[newNum] = tempByte[1];
                    theSorter.add(tempByte);
                    //System.out.println("2nd Recieved block is: "+ tempByte[1]);
                }
                 */
                if(theSorter.size() +1 > 9){
                    selectionSort(theSorter);
                    for (int i =0; i < theSorter.size(); i++) {
                        tempByte = theSorter.get(i);
                        for (int ii = 2; ii < 511; ii++) {
                            audioPart[ii] = tempByte[ii];
                        }
                        //System.out.println("Packet being played now is "+ tempByte[1]);
                        player.playBlock(audioPart);
                    }
                    theSorter.clear();
                }
                /*
                System.out.println("newNum = "+newNum);
                if(newNum + 1 ==50){
                    newNum=1;
                }
                else newNum++;

                 */
            } catch (IOException e){
                System.out.println("ERROR: TextReceiver: Some random IO error occured!");
                e.printStackTrace();
            }
        }
        //Close the socket
        receiving_socket.close();
        //***************************************************
    }
}