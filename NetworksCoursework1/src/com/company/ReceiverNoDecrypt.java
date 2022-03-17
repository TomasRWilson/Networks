package com.company;/*
 * ReceiverNoDecrypt.java
 */

/**
 *
 * @author  Tomas Wilson
 */
import java.net.*;
import java.io.*;
import java.nio.ByteBuffer;
import CMPC3M06.AudioPlayer;
import javax.sound.sampled.LineUnavailableException;

public class ReceiverNoDecrypt extends Receiver implements Runnable{

    public ReceiverNoDecrypt() throws LineUnavailableException {
    }

    public void run (){

        //***************************************************
        //Port to open socket on
        int PORT = 55555;
        //***************************************************

        //DatagramSocket receiving_socket;
        try{
            receiving_socket = new DatagramSocket(PORT);
        } catch (SocketException e){
            System.out.println("ERROR: TextReceiver: Could not open UDP socket to receive from.");
            e.printStackTrace();
            System.exit(0);
        }

        System.out.println("You're Receiving");

        //Main loop.;
        while (running){

            try{
                //Receive a DatagramPacket
                byte[] buffer = new byte[516];
                DatagramPacket packet = new DatagramPacket(buffer, 0, 516);

                receiving_socket.receive(packet);
                count++;
                //Strip packet into header components and data
                ByteBuffer receivedPacket = ByteBuffer.wrap(buffer);
                short keyCheck = receivedPacket.getShort();
                short sequenceKey = receivedPacket.getShort();
                byte[] packetData = new byte[512];
                receivedPacket.get(packetData, 0, 512);
                //Play the packet data
                player.playBlock(packetData);
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