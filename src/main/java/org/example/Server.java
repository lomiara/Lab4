package org.example;
import java.io.*;
import java.util.*;
import java.net.*;

public class Server {

    final static int ServerPort = 2000;
    static ArrayList<ClientHandler> ar = new ArrayList<>();
    static ArrayList<String> users = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        ServerSocket ss = new ServerSocket(ServerPort,50,InetAddress.getByName("localhost"));
        Socket s;
        while (true) {
            s = ss.accept();
            System.out.println("New client request received : " + s);
            DataInputStream dataInputStream = new DataInputStream(s.getInputStream());
            DataOutputStream dataOutputStream = new DataOutputStream(s.getOutputStream());
            String a = dataInputStream.readUTF();
            if(!a.equals("HELLO-REQUEST")) {
                System.out.println("HANDSHAKE ERROR");
                break;
            } else {
                dataOutputStream.writeUTF("HELLO-APPROVE");
            }
            while (true) {
                dataOutputStream.writeUTF("Enter unique username: ");
                String username = dataInputStream.readUTF();
                if(users.contains(username)) {
                    dataOutputStream.writeUTF("Not unique");
                } else {
                    dataOutputStream.writeUTF("Unique");
                    users.add(username);
                    break;
                }
            }
            System.out.println("Creating a new handler for this client...");
            ClientHandler handler = new ClientHandler(s,users.get(users.size()-1), dataInputStream, dataOutputStream);
            Thread t = new Thread(handler);
            System.out.println("Adding this client to active client list");
            ar.add(handler);
            t.start();
        }
    }
}


class ClientHandler implements Runnable {
    private String name;
    final DataInputStream dis;
    final DataOutputStream dos;
    Socket s;

    public ClientHandler(Socket s, String name,
                         DataInputStream dis, DataOutputStream dos) {
        this.dis = dis;
        this.dos = dos;
        this.name = name;
        this.s = s;
    }

    @Override
    public void run() {
        String received;
        while (true) {
            try {
                received = dis.readUTF();
                System.out.println(name+": "+received);
                if(received.equals("logout")){
                    this.s.close();
                    Server.users.remove(name);
                    break;
                }
                if(received.equals("users")){
                    for(String s: Server.users){
                        dos.writeUTF(s);
                    }
                } else {
                    StringTokenizer st = new StringTokenizer(received, "#");
                    String MsgToSend = st.nextToken();
                    String recipient = st.nextToken();
                    for (ClientHandler mc : Server.ar) {
                        if (mc.name.equals(recipient)) {
                            mc.dos.writeUTF(this.name+" : "+MsgToSend);
                            break;
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            this.dis.close();
            this.dos.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }
} 