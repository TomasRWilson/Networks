package com.company;/*
 * Sender2.java
 */

/**
 *
 * @author  Sam Batchelor
 */
import java.net.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Vector;
import uk.ac.uea.cmp.voip.DatagramSocket2;

import javax.sound.sampled.LineUnavailableException;

public class Sender2 extends Sender implements Runnable {

    public Sender2() throws LineUnavailableException {
    }

    public void run() {

        //***************************************************
        //Port to send to
        int PORT = 55555;
        //IP ADDRESS to send to
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
        //Open a socket to send from
        //We dont need to know its port number as we never send anything to it.
        //We need the try and catch block to make sure no errors occur.

        //DatagramSocket sending_socket;
        try {
            sending_socket = new DatagramSocket2();
        } catch (SocketException e) {
            System.out.println("ERROR: TextSender: Could not open UDP socket to send from.");
            e.printStackTrace();
            System.exit(0);
        }
        //***************************************************

        //***************************************************
        //Get a handle to the Standard Input (console) so we can read user input

        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        //***************************************************

        //***************************************************
        //Main loop.
        System.out.println("You're Streaming");

        boolean running = true;
        short authenticationKey = 0;

        Vector<byte[]> voiceVector = new Vector<byte[]>();

        while (running) {
            try {
                //Record block of audio
                byte[] buffer = recorder.getBlock();
                //Assign packet for key and data
                ByteBuffer VoIPpacket = ByteBuffer.allocate(514);
                //Allocate key no.
                if (authenticationKey + 1 == 100) {
                    authenticationKey = 0;
                }
                authenticationKey++;
                //Construct packet with key and data
                VoIPpacket.putShort(authenticationKey);
                VoIPpacket.put(buffer);
                byte[] theSending = VoIPpacket.array();
                //Add packet to group(vector)
                voiceVector.add(theSending);
                //Once vector reaches size 10 send all packets
                if(voiceVector.size() == 20){
                    //Shuffle packets to reduce effect of burst of packet loss
                    Collections.shuffle(voiceVector);
                    //Make a DatagramPacket from it, with client address and port number
                    for(int i=0;i<voiceVector.size();i++){
                        voiceVector.get(i);
                        DatagramPacket packet = new DatagramPacket(voiceVector.get(i), voiceVector.get(i).length, clientIP, PORT);
                        //Send it
                        sending_socket.send(packet);
                    }
                    voiceVector.clear();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //Close the socket
        sending_socket.close();
        //***************************************************
    }
}