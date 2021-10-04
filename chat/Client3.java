package chat;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

public class Client3 {
    Socket socket;

    public static void main(String[] args) {
        Client3 client = new Client3();
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

    private void addName() throws IOException {
        DataInputStream input = new DataInputStream(socket.getInputStream());
        DataOutputStream output = new DataOutputStream(socket.getOutputStream());
        Scanner scanner = new Scanner(System.in);
        System.out.println(input.readUTF());
        while (true) {
            output.writeUTF(scanner.nextLine());
            String answer = input.readUTF();
            if (answer.equals("Server: this name is already taken! Choose another one.")) {
                System.out.println(answer);
            } else {
                break;
            }
        }
    }
}
