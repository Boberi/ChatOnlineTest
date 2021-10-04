package chat.client;

import chat.ClientReader;
import chat.ClientWriter;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;


public class Client {

    Socket socket;

    public static void main(String[] args) {
        Client client = new Client();
        client.startClient();

    }

    private void startClient() {
        try {
            int port = 23456;
            String address = "127.0.0.1";
            socket = new Socket(InetAddress.getByName(address), port);
            System.out.println("Client started!");
            ClientWriter writer = new ClientWriter(socket);
            ClientReader reader = new ClientReader(socket);
            writer.start();
            reader.start();
            writer.join();
            reader.join();
        } catch (IOException | InterruptedException e) {
            System.out.println(e);
        }
    }
}


