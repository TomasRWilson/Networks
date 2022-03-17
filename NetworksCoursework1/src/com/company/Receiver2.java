package com.company;/*
 * Receiver2.java
 */

/**
 *
 * @author  Sam Batchelor
 */
import java.net.*;
import java.io.*;
import java.util.*;
import java.nio.ByteBuffer;

import javax.sound.sampled.LineUnavailableException;

public class Receiver2 extends Receiver implements Runnable {

    public Receiver2() throws LineUnavailableException {
    }

    public void run() {

        //***************************************************
        //Port to open socket on
        int PORT = 55555;
        //***************************************************

        //***************************************************
        //Open a socket to receive from on port PORT
        System.out.println("You're ready to receive");
        //DatagramSocket receiving_socket;
        try {
            receiving_socket = new DatagramSocket(PORT);
        } catch (SocketException e) {
            System.out.println("ERROR: TextReceiver: Could not open UDP socket to receive from.");
            e.printStackTrace();
            System.exit(0);
        }
        //***************************************************

        //***************************************************
        //Main loop.
        Map<Short, byte[]> holder = new HashMap<>();
        Short temp = 0;
        while (running) {

            // sorting the vector
            try {
                //Receive a DatagramPacket (note that the string cant be more than 80 chars)
                byte[] buffer = new byte[514];
                DatagramPacket packet = new DatagramPacket(buffer, 0, 514);

                receiving_socket.receive(packet);

                ByteBuffer data = ByteBuffer.wrap(buffer);
                Short sequence = data.getShort();
                byte[] audioPart = new byte[512];
                data.get(audioPart, 0,audioPart.length);
                holder.put(sequence, audioPart);

                if(holder.size() == 20){
                    TreeMap<Short, byte[]> sortedBlocks = new TreeMap<>();
                    sortedBlocks.putAll(holder);
                    for (Map.Entry<Short, byte[]> entry : sortedBlocks.entrySet()){
                        if((entry.getKey() > temp) || (entry.getKey()<20)){
                            player.playBlock(entry.getValue());
                            temp = entry.getKey();
                        }
                    }
                    holder.clear();
                }

            } catch (IOException e) {
                System.out.println("ERROR: TextReceiver: Some random IO error occured!");
                e.printStackTrace();
            }
        }
        //Close the socket
        receiving_socket.close();
        //***************************************************
    }
}
