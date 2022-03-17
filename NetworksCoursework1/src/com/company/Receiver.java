package com.company;/*
 * Receiver.java
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

public class Receiver implements Runnable{

    static DatagramSocket receiving_socket;
    AudioPlayer player = new AudioPlayer();
    int key = 229, count = 0;
    short authKey = 10;
    boolean running = true;
    //Transposition
    int[] transposition = {7,5,3,2,6,0,4,1};
    //RSA private
    final int d = 233, n = 437;

    public Receiver() throws LineUnavailableException {
    }

    public void start(){
        Thread thread = new Thread(this);
        thread.start();
    }

    public void stop(){
        running = false;
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
        //***************************************************
        System.out.println("You're ready to receive");
        //***************************************************
        //Main loop.;
        while (running){

            try{
                //Receive a DatagramPacket
                byte[] buffer = new byte[516];
                DatagramPacket packet = new DatagramPacket(buffer, 0, 516);

                receiving_socket.receive(packet);
                count++;
                ByteBuffer data = ByteBuffer.wrap(buffer);
                short keyCheck = data.getShort();
                short sequenceKey = data.getShort();
                byte[] audio = new byte[512];
                data.get(audio, 0, audio.length);
                //Check if authKey matches the stored key in on receivers end
                if(keyCheck == authKey){
                    //Decrypt received data
                    int[] tempHold = new int[8];
                    ByteBuffer unwrapDecrypt = ByteBuffer.allocate(audio.length);
                    ByteBuffer cipherText = ByteBuffer.wrap(audio);
                    for(int j = 0; j < audio.length/4; j++) {
                        int fourByte = cipherText.getInt();
                        fourByte = fourByte ^ key;
                        tempHold[j%8] = fourByte;
                        if(j%8 == 7){
                            for(int i=0; i<8;i++){
                                unwrapDecrypt.putInt(tempHold[transposition[i]]);
                            }
                        }
                    }
                    byte[] decryptedAudio = unwrapDecrypt.array();
                    //Play the packet data
                    player.playBlock(decryptedAudio);
                }else{
                    System.out.println("Non-auth packet received");
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

    private int RSADecrypt(int fourByte){
        int c = (int) (Math.pow(fourByte, d)%n);
        return c;
    }
}