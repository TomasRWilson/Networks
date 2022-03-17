package com.company;/*
 * Sender3.java
 */

/**
 *
 * @author  Nathan Cook
 */

import CMPC3M06.AudioRecorder;
import uk.ac.uea.cmp.voip.DatagramSocket2;

import javax.sound.sampled.LineUnavailableException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

public class Sender3 extends Sender implements Runnable {

    public Sender3() throws LineUnavailableException {
    }

    public void run (){

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
        try{
            sending_socket = new DatagramSocket2();
        } catch (SocketException e){
            System.out.println("ERROR: TextSender: Could not open UDP socket to send from.");
            e.printStackTrace();
            System.exit(0);
        }

        //***************************************************

        //***************************************************
        //Get a handle to the Standard Input (console) so we can read user input
        System.out.println("You're Streaming");
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        //***************************************************

        //***************************************************
        //Main loop.
        short idKey = 0;
        while (running){
            try{
                //Convert it to an array of bytes
                byte[] buffer = recorder.getBlock();
                //makes ByteBuffer object (different to a byte[] and unfortunately not able to be sent through the datapacket, so we just use it to add bits
                //onto the data and then convert back into a byte[] type
                ByteBuffer VoIPpacket = ByteBuffer.allocate(514);
                if (idKey+1 ==100){             //the idKey gets reset once it reaches 99
                    idKey=0;
                }
                idKey++;                        //increased the idKey by 1 (up to 99 then resets) every loop to give each packet a new id
                VoIPpacket.putShort(idKey);     //puts the idKey in the packet first
                VoIPpacket.put(buffer);         //puts the voice block in next
                byte[] theSending = new byte[VoIPpacket.capacity()];        //the byte[] to be sent
                for(int i =1; i<513;i++){
                    theSending[i] = VoIPpacket.get(i);
                }

                //Make a DatagramPacket from it, with client address and port number
                DatagramPacket packet = new DatagramPacket(theSending, theSending.length, clientIP, PORT);

                //Send it
                sending_socket.send(packet);

            } catch (IOException e){
                System.out.println("ERROR: TextReceiver: Some random IO error occured!");
                e.printStackTrace();
            }
        }

        //Close the socket
        sending_socket.close();
        //***************************************************
    }
}