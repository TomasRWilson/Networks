package com.company;/*
 * Receiver4.java
 */

/**
 *
 * @author  Tomas Wilson
 */
import java.net.*;
import java.io.*;
import java.util.Arrays;
import java.nio.ByteBuffer;

import javax.sound.sampled.LineUnavailableException;

public class Receiver4 extends Receiver implements Runnable{
    byte[] tempBlock = null;

    public Receiver4() throws LineUnavailableException {
    }

    public void run (){
        //***************************************************
        //Port to open socket on
        int PORT = 55555;
        //DatagramSocket receiving_socket;
        try{
            receiving_socket = new DatagramSocket(PORT);
            System.out.println("You're ready to receive");
        } catch (SocketException e){
            System.out.println("ERROR: TextReceiver: Could not open UDP socket to receive from.");
            e.printStackTrace();
            System.exit(0);
        }
        //***************************************************
        //Main loop.;
        while (running){

            try{
                //Receive a DatagramPacket (note that the string cant be more than 80 chars)
                byte[] buffer = new byte[520];
                DatagramPacket packet = new DatagramPacket(buffer, 0, 520);

                receiving_socket.receive(packet);
                //Authenticate packet
                ByteBuffer receivedPacket = ByteBuffer.wrap(buffer);
                short keyCheck = receivedPacket.getShort();
                int hashValue = receivedPacket.getInt();
                short sequenceKey = receivedPacket.getShort();
                if(keyCheck == authKey){
                    byte[] packetData = new byte[512];
                    receivedPacket.get(packetData, 0, packetData.length);
                    //Calculate checksum of received data
                    int hash = Arrays.hashCode(packetData);
                    //Compare hash codes to see if data is corrupted
                    if(hash == hashValue){
                        //Decrypt received data
                        ByteBuffer unwrapDecrypt = ByteBuffer.allocate(packetData.length);
                        ByteBuffer cipherText = ByteBuffer.wrap(packetData);
                        for(int j = 0; j < packetData.length/4; j++) {
                            int fourByte = cipherText.getInt();
                            fourByte = fourByte ^ key;// XOR decrypt
                            unwrapDecrypt.putInt(fourByte);
                        }
                        byte[] decryptedBlock = unwrapDecrypt.array();
                        tempBlock = decryptedBlock;
                        player.playBlock(decryptedBlock);
                    }else{
                        //If corrupted block is received, replay the last non-corrupted block
                        if(tempBlock != null){
                            player.playBlock(tempBlock);
                        }
                    }
                }
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