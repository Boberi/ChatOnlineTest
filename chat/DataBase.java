package chat;

import java.io.Serializable;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class DataBase implements Serializable {
    private transient Map<String, Socket> clientsLogged = new LinkedHashMap<>();
    private final ArrayList<String> messages = new ArrayList<>();
    private final Map<String, Account> clients = new LinkedHashMap<>();

    public synchronized boolean checkName(String name) {
        return !clientsLogged.containsKey(name);
    }

    public synchronized void addLoggedClient(String login, Socket socket) {
        if (clientsLogged == null) {
            clientsLogged = new LinkedHashMap<String, Socket>();
        }
        clientsLogged.put(login, socket);
    }

    public synchronized void addClient(String login, Account account) {
        clients.put(login, account);
    }

    public Map<String, Account> getClients() {
        return clients;
    }

    public synchronized Map<String, Socket> getClientsLogged() {
        return clientsLogged;
    }

    public synchronized ArrayList<String> getMessages() {
        return messages;
    }

    public synchronized void removeLoggedClient(String name) {
        clientsLogged.remove(name);
    }

    public synchronized void addMessage(String message) {
        messages.add(message);
    }
}

