package chat;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class ClientReader extends Thread {
    private final Socket socket;

    public ClientReader(Socket socketForClient) {
        this.socket = socketForClient;
    }

    @Override
    public void run() {
        try (DataInputStream input = new DataInputStream(socket.getInputStream());
        ) {
            while (!socket.isClosed()) {
                System.out.println(input.readUTF());
            }
        } catch (IOException e) {

        }
    }
}
