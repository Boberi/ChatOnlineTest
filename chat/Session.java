package chat;

import java.io.IOException;
import java.net.Socket;

public class Session extends Thread {
    private final Socket socket;
    private final int id;
    private DataBase dataBase;

    public Session(Socket socketForClient, int clientId, DataBase dataBase) {
        this.socket = socketForClient;
        this.id = clientId;
        this.dataBase = dataBase;
    }

    @Override
    public void run() {
        try {
            System.out.println("Client " + id + " connected!");
            ServerReader reader = new ServerReader(socket, id, dataBase);
            reader.start();
            reader.join(500);
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }

}
