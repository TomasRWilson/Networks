package com.company;/*
 * Sender.java
 */

/**
 *
 * @author  Tomas Wilson
 */
import java.net.*;
import java.io.*;
import CMPC3M06.AudioRecorder;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.BitSet;

import javax.sound.sampled.LineUnavailableException;

public class Sender implements Runnable {

    AudioRecorder recorder = new AudioRecorder();
    static DatagramSocket sending_socket;
    int key = 229;
    //String key = "WnZr4u7x";
    byte[][] keys;
    short authenticationKey = 10, sequenceKey = 0;
    boolean running = true;
    //Transposition
    int[] transposition = {5,7,3,2,6,1,4,0};
    //Values for RSA
    final int p = 19, q = 23, n = p*q, z = (p-1)*(q-1), e = 17;

    //Values for DES encryption
    final int[][] SBoxValues = {{2,12,4,1,7,10,11,6,8,5,3,15,13,0,14,9},{14,11,2,12,4,7,13,1,5,0,15,10,3,9,8,6},{4,2,1,11,10,13,7,8,15,9,12,5,6,3,0,14},{11,8,12,7,1,14,2,13,6,15,0,9,10,4,5,3}};
    final int[] straightPerm = {16,7,20,21,29,12,28,17,1,15,23,26,5,18,31,10,2,8,24,14,32,27,3,9,19,13,30,6,22,11,4,25};
    int[] perm1 = {1,2,58,64}, perm2 = {40,8,1,25};
    final int[] pc1 = {57,49,41,33,25,17,9,1,58,50,42,34,26,18,10,2,59,51,43,35,27,19,11,3,60,52,44,36,63,55,47,39,31,23,15,7,62,54,46,38,30,22,14,6,61,53,45,37,29,21,13,5,28,20,12,4};
    final int[] pc2 = {14,17,11,24,1,5,3,28,15,6,21,10,23,19,12,4,26,8,16,7,27,20,13,2,41,52,31,37,47,55,30,40,51,45,33,48,44,49,39,56,34,53,46,42,50,36,29,32};

    public Sender() throws LineUnavailableException {
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
        //DatagramSocket sending_socket;
        try{
            sending_socket = new DatagramSocket();
        } catch (SocketException e){
            System.out.println("ERROR: TextSender: Could not open UDP socket to send from.");
            e.printStackTrace();
            System.exit(0);
        }
        System.out.println("You're Streaming");
        //Main loop.
        while (running){
            try{
                //Convert it to an array of bytes
                byte[] block = recorder.getBlock();
                //Assign sequence key and reset if it hits the max value
                if(sequenceKey + 1 == 50){
                    sequenceKey = 0;
                }
                ByteBuffer unwrapEncrypt = ByteBuffer.allocate(block.length);
                //Encrypt block using key
                ByteBuffer packetData = ByteBuffer.wrap(block);
                int[] tempHold = new int[8];
                for( int j = 0; j < block.length/4; j++) {
                    int fourByte = packetData.getInt();
                    fourByte = fourByte ^ key;
                    tempHold[j%8] = fourByte;
                    if(j%8 == 7){
                        for(int i=0; i<8;i++){
                            unwrapEncrypt.putInt(tempHold[transposition[i]]);
                        }
                    }
                }
                //Construct packet with authentication, sequence number and data
                byte[] authenticatedPacket = unwrapEncrypt.array();
                ByteBuffer authPacket = ByteBuffer.allocate(516);
                authPacket.putShort(authenticationKey);
                authPacket.putShort(sequenceKey);
                authPacket.put(authenticatedPacket);
                byte[] fullPacket = authPacket.array();

                DatagramPacket packet = new DatagramPacket(fullPacket, fullPacket.length, clientIP, PORT);
                //Send packet
                sending_socket.send(packet);
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
    //RSA Encryption
    private int RSAEncrypt(int fourByte){
        int c = (int) ((Math.pow(fourByte, e))%n);
        return c;
    }

    //DES Encryption (Need to fix)
    private BitSet SBox(BitSet bits){
        BitSet middle = bits.get(1,4);
        BitSet outer = new BitSet(2);
        outer.set(0, bits.get(0));
        outer.set(1, bits.get(5));
        int outerIndex = 0;
        int middleIndex = 0;
        //Calc outer index of table
        for(int i=0;i<2;i++){
            if(outer.get(i)){
                outerIndex+=(Math.pow(2,1-i));
            }
        }
        //Calc middle index of table
        for(int i=0;i<4;i++){
            if(middle.get(i)){
                middleIndex+=(Math.pow(2,3-i));
            }
        }
        //Retrieve S-Box bits
        int answer = SBoxValues[outerIndex][middleIndex];
        BitSet exitBits = new BitSet(4);
        for(int i=3;i>=0;i--){
            if((middleIndex/Math.pow(2,i))>1){
                middleIndex = (int) (answer%Math.pow(2,i));
                exitBits.set(3-i, true);
            }else{
                exitBits.set(3-i, false);
            }
        }
        return exitBits;
    }

    private byte[] StraightP(byte[] array){
        BitSet bitArray = BitSet.valueOf(array);
        BitSet newArray = new BitSet(32);
        for(int i=0;i<straightPerm.length;i++){
            newArray.set(i,bitArray.get(straightPerm[i]-1));
        }
        return newArray.toByteArray();
    }

    private byte[] IP(byte[] array){
        BitSet bits = BitSet.valueOf(array);
        BitSet newArray = (BitSet) bits.clone();
        for(int i=0;i<4;i++){
            newArray.set(perm2[i]-1,bits.get(perm1[i]-1));
        }
        return newArray.toByteArray();
    }

    private byte[] FP(byte[] array){
        BitSet bits = BitSet.valueOf(array);
        BitSet newArray = (BitSet) bits.clone();
        for(int i=0;i<4;i++){
            newArray.set(perm1[i]-1,bits.get(perm2[i]-1));
        }
        return newArray.toByteArray();
    }

    private byte[] ExpansionP(byte[] array){
        BitSet bits = BitSet.valueOf(array);
        BitSet newArray = new BitSet(48);
        for(int i=0;i<8;i++){
            if(i==0){
                newArray.set(0,bits.get(31));
            }else{
                newArray.set((i*6)-1,bits.get(i*4));
            }
            for(int j=0;j<4;j++){
                newArray.set((i*6)+1+j, bits.get((i*4)+j));
            }
            newArray.set((i+1)*6, bits.get(i*4));
            if(i==7){
                newArray.set(47,bits.get(0));
            }
        }
        return newArray.toByteArray();
    }

    private byte[] XOR(byte[] array, byte[] key){
        BitSet bits = BitSet.valueOf(array);
        BitSet keyBS = BitSet.valueOf(key);
        bits.xor(keyBS);
        return bits.toByteArray();
    }

    private byte[] DES(byte[] array, byte[] key){
        array = ExpansionP(array);
        array = XOR(array, key);
        BitSet SArray = BitSet.valueOf(array);
        BitSet temp;
        BitSet compArray = new BitSet(32);
        int j = 0;
        for(int i=0;i<48;i+=6){
            temp = SBox(SArray.get(i,i+6));
            for(int k=0;k<4;k++){
                compArray.set(j+k, temp.get(k));
            }
            j+=4;
        }
        array = compArray.toByteArray();
        array = StraightP(array);
        return array;
    }

    private byte[] Round(byte[] array, byte[] key, boolean swap){
        System.out.println(Arrays.toString(array));
        byte[] l = Arrays.copyOfRange(array,0,3);
        System.out.println(Arrays.toString(l));
        byte[] r = Arrays.copyOfRange(array,4,7);
        System.out.println(Arrays.toString(r));
        byte[] rf = DES(array, key);
        rf = XOR(l, rf);
        byte[] exitArray = new byte[8];
        if(swap){
            for(int i=0;i<8;i++){
                if(i<4){
                    exitArray[i] = r[i];
                }else{
                    exitArray[i] = rf[i-4];
                }
            }
        }else{
            for(int i=0;i<8;i++){
                if(i<4){
                    exitArray[i] = rf[i];
                }else{
                    System.out.println(i);
                    exitArray[i] = r[i-4];
                }
            }
        }
        return exitArray;
    }

    private byte[] Encrypt(byte[] array, byte[][] keys){
        System.out.println(array.length);
        array = IP(array);
        System.out.println(array.length);
        for(int i=0;i<16;i++){
            if (i == 15) {
                array = Round(array, keys[i], false);
            }else{
                array = Round(array, keys[i], true);
            }
        }
        array = FP(array);
        return array;
    }

    private byte[] Decrypt(byte[] array, byte[][] keys){
        array = IP(array);
        for(int i=15;i>=0;i--){
            if (i == 0) {
                array = Round(array, keys[i], false);
            }else{
                array = Round(array, keys[i], true);
            }
        }
        array = FP(array);
        return array;
    }

    private byte[][] KeyGenerator(byte[] key){
        BitSet keyBits = BitSet.valueOf(key);
        BitSet cleanKey = new BitSet(56);
        for(int i=0;i<56;i++){
            cleanKey.set(i, keyBits.get(pc1[i]-1));
        }
        BitSet C = cleanKey.get(0,27);
        BitSet D = cleanKey.get(28,56);
        byte[][] keys = new byte[16][];
        for(int i=0;i<16;i++){
            if(i==0||i==1||i==8||i==15){
                C=ShiftBits(C,1);
                D=ShiftBits(D,1);
            }else{
                C=ShiftBits(C,2);
                D=ShiftBits(D,2);
            }
            keys[i] = pc2Function(C,D);
        }
        return keys;
    }

    private byte[] pc2Function(BitSet C, BitSet D){
        BitSet key = new BitSet(48);
        for(int i=0;i<48;i++){
            if(i<24){
                key.set(i, C.get(pc2[i]-1));
            }else{
                key.set(i, D.get(pc2[i]-29));
            }
        }
        return key.toByteArray();
    }

    private BitSet ShiftBits(BitSet bits,int x){
        BitSet temp = new BitSet(x);
        BitSet shifted = new BitSet(28);
        for(int i=0;i<x;i++){
            temp.set(i,bits.get(i));
        }
        for(int i=x-1;i<bits.length();i++){
            shifted.set(i-x+1, bits.get(i));
        }
        int j=0;
        for(int i=bits.length()-x-1;i<temp.length();i++){
            shifted.set(i,temp.get(j));
        }
        return shifted;
    }
}