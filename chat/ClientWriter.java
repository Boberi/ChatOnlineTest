package chat;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class ClientWriter extends Thread {
    private final Socket socket;

    public ClientWriter(Socket socketForClient) {
        this.socket = socketForClient;
    }

    @Override
    public void run() {
        try (DataOutputStream output = new DataOutputStream(socket.getOutputStream());
             Scanner scanner = new Scanner(System.in);
        ) {
            while (!isInterrupted()) {
                String msg = scanner.nextLine();
                output.writeUTF(msg);
                if (msg.equals("/exit")){
                    interrupt();
                }
            }
        } catch (IOException e) {
            System.out.println("CLIENTWRITER ABOBA");
        }
    }
}
