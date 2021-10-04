package chat.server;


import chat.Account;
import chat.DataBase;
import chat.ServerReader;
import chat.Session;
import chat.client.Serializator;

import java.io.*;
import java.net.*;


public class Server {
    private final String address = "127.0.0.1";
    private final int port = 23456;

    public static void main(String[] args) throws InterruptedException {
        Server server = new Server();
        server.run();
        Thread.sleep(100);
    }

    private void run() {
        try (ServerSocket server = new ServerSocket(port, 50, InetAddress.getByName(address));) {
            System.out.println("Server started!");
            server.setSoTimeout(7000);
            DataBase dataBase = new DataBase();
            dataBase.getClients().put("admin", new Account("admin", "12345678"));
            File f = new File("D:\\ChatOnline\\Online Chat\\task\\src\\chatcitizens.data");
            if (f.length() != 0){
                dataBase = (DataBase) Serializator.deserialize("D:\\ChatOnline\\Online Chat\\task\\src\\chatcitizens.data");
            }
            int id = 1;
            while (true){
                Session session = new Session(server.accept(), id, dataBase);
                session.start();
                id++;
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}

