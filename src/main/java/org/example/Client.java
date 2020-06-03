package org.example;
import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {
    final static int ServerPort = 2000;
    public static void main(String[] args) throws  IOException {
        final Scanner scn = new Scanner(System.in);
        InetAddress ip = InetAddress.getByName("localhost");
        final Socket s = new Socket(ip, ServerPort);
        final DataInputStream dataInputStream = new DataInputStream(s.getInputStream());
        final DataOutputStream dataOutputStream = new DataOutputStream(s.getOutputStream());
        dataOutputStream.writeUTF("HELLO-REQUEST");
        if(!dataInputStream.readUTF().equals("HELLO-APPROVE")){
           System.out.println("HANDSHAKE ERROR");
           return;
        }
        Thread sendMessage = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    String msg = scn.nextLine();
                    try {
                        dataOutputStream.writeUTF(msg);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        Thread readMessage = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        String msg = dataInputStream.readUTF();
                        System.out.println(msg);
                    } catch (EOFException e) {
                        System.out.println("Socket was closed");
                        System.exit(0);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        sendMessage.start();
        readMessage.start();
    }
}
