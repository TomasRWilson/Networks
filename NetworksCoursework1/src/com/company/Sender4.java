package com.company;/*
 * Sender4.java
 */

/**
 *
 * @author  Tomas Wilson
 */
import java.net.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.zip.CRC32;
import java.util.zip.Checksum;
import uk.ac.uea.cmp.voip.DatagramSocket4;

import javax.sound.sampled.LineUnavailableException;

public class Sender4 extends Sender implements Runnable {

    public Sender4() throws LineUnavailableException {
    }

    public void run (){
        int key = 229;
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

        //DatagramSocket sending_socket;
        try{
            sending_socket = new DatagramSocket4();
            System.out.println("You're Streaming");
        } catch (SocketException e){
            System.out.println("ERROR: TextSender: Could not open UDP socket to send from.");
            e.printStackTrace();
            System.exit(0);
        }

        //Main loop.
        while (running){
            try{
                //Convert it to an array of bytes
                byte[] block = recorder.getBlock();
                ByteBuffer unwrapEncrypt = ByteBuffer.allocate(block.length);
                //Encrypt block using key
                ByteBuffer plainText = ByteBuffer.wrap(block);
                for( int j = 0; j < block.length/4; j++) {
                    int fourByte = plainText.getInt();
                    fourByte = fourByte ^ key; // XOR operation with key
                    unwrapEncrypt.putInt(fourByte);
                }
                byte[] encryptedBlock = unwrapEncrypt.array();
                //Add authentication key
                ByteBuffer authPacket = ByteBuffer.allocate(520);
                if(sequenceKey + 1 == 50){
                    sequenceKey = 0;
                }
                //Create checksum value
                int hash = Arrays.hashCode(encryptedBlock);
                //Checksum checksum = new CRC32();
                //checksum.update(encryptedBlock, 0, encryptedBlock.length);
                //long checksumValue = checksum.getValue();
                //Construct the packet with all elements
                authPacket.putShort(authenticationKey);
                authPacket.putInt(hash);
                authPacket.putShort(sequenceKey);
                authPacket.put(encryptedBlock);
                byte[] authenticatedPacket = authPacket.array();
                DatagramPacket packet = new DatagramPacket(authenticatedPacket, authenticatedPacket.length, clientIP, PORT);
                //Send the packet
                sending_socket.send(packet);

                /*
                packetsToSend.add(authenticatedPacket);
                if(packetsToSend.size() == 10){
                    Vector<byte[]> tempStore = packetsToSend;
                    for (byte[] packets : packetsToSend) {
                        DatagramPacket packet = new DatagramPacket(packets, packets.length, clientIP, PORT);
                        sending_socket.send(packet);
                    }
                }
                */
                sequenceKey++;
            } catch (IOException e){
                System.out.println("ERROR: TextSender: Some random IO error occured!");
                e.printStackTrace();
            }
        }
        //Close the socket
        sending_socket.close();
        //***************************************************
    }
}