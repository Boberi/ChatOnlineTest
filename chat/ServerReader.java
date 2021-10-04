package chat;

import chat.client.Serializator;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class ServerReader extends Thread {

    private final Socket socket;
    private final int id;
    private final DataBase dataBase;
    private final DataInputStream input;
    private final DataOutputStream output;
    private Account user;
    private boolean authorized = false;
    private boolean inChat = false;
    private Account chatClient;
    private boolean inOnlineChat = false;

    public ServerReader(Socket socketForClient, int ClientId, DataBase dataBase) throws IOException {
        this.socket = socketForClient;
        this.id = ClientId;
        this.dataBase = dataBase;
        this.input = new DataInputStream(socket.getInputStream());
        this.output = new DataOutputStream(socket.getOutputStream());
    }

    private void register(String login, String password) throws IOException {
        if (password.split("").length < 8) {
            output.writeUTF("Server: the password is too short!");
        } else if (dataBase.getClients().containsKey(login)) {
            output.writeUTF("Server: this login is already taken! Choose another one.");
        } else {
            output.writeUTF("Server: you are registered successfully!");
            dataBase.addLoggedClient(login, socket);
            this.user = new Account(login, password);
            dataBase.addClient(login, user);
            authorized = true;
        }
        Serializator.serialize(dataBase, "D:\\ChatOnline\\Online Chat\\task\\src\\chatcitizens.data");
    }

    private void auth(String login, String password) throws IOException {
        if (!dataBase.getClients().containsKey(login)) {
            output.writeUTF("Server: incorrect login!");
        } else if (!dataBase.getClients().get(login).getPassword().equals(password)) {
            output.writeUTF("Server: incorrect password!");
        } else if (dataBase.getClients().get(login).isBanned()) {
            output.writeUTF("You are banned!");
        } else {
            dataBase.addLoggedClient(login, socket);
            output.writeUTF("Server: you are authorized successfully!");
            user = dataBase.getClients().get(login);
            authorized = true;
        }
    }

    private void list() throws IOException {
        if (dataBase.getClientsLogged().size() == 1) {
            output.writeUTF("Server: no one online");
        } else {
            StringBuilder onlineList = new StringBuilder("Server: online:");
            ArrayList<String> listOnline = new ArrayList<>();
            for (String s : dataBase.getClientsLogged().keySet()) {
                if (s.equals(user.getLogin())) {
                    continue;
                }
                listOnline.add(s);
            }
            Collections.sort(listOnline);
            for (String s : listOnline) {
                onlineList.append(" ").append(s);
            }
            output.writeUTF(onlineList.toString());
        }

    }

    private void grant(String name) throws IOException {
        if (!user.isAdmin()) {
            output.writeUTF("Server: you are not an admin!");
        } else if (dataBase.getClients().get(name).isModerator()) {
            output.writeUTF("Server: this user is already a moderator!");
        } else {
            dataBase.getClients().get(name).setModerator(true);
            if (dataBase.getClientsLogged().containsKey(name)) {
                Socket moderator = dataBase.getClientsLogged().get(name);
                DataOutputStream outputModerator = new DataOutputStream(moderator.getOutputStream());
                outputModerator.writeUTF("Server: you are the new moderator now!");
                output.writeUTF("Server: " + name + " is the new moderator!");
            }
        }
    }

    private void stat() throws IOException {
        int fromUser = 0;
        int all = user.getIncomingMessages().get(chatClient).size();
        for (Message message : user.getIncomingMessages().get(chatClient)) {
            if (message.getText().contains(user.getLogin() + ":")) {
                fromUser++;
            }
        }
        int fromChatter = all - fromUser;
        output.writeUTF(String.format("Server:\n" +
                "Statistics with %s:\n" +
                "Total messages: %d\n" +
                "Messages from %s: %d\n" +
                "Messages from %s: %d", chatClient.getLogin(), all, user.getLogin(), fromUser, chatClient.getLogin(), fromChatter));

    }

    private void history(String number) throws IOException {
        try {
            int n = Integer.parseInt(number);
            ArrayList<Message> messages = user.getIncomingMessages().get(chatClient);
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Server: \n");
            if (n > 25) {
                int start = n;
                n = 25;
                int counter = 0;
                for (int i = messages.size() - start; counter < 25; i++) {
                    if (counter == 24) {
                        stringBuilder.append(messages.get(i).getText());
                        break;
                    }
                    stringBuilder.append(messages.get(i).getText()).append("\n");
                    counter++;
                }
            } else {
                for (int i = messages.size() - n; i < messages.size(); i++) {
                    stringBuilder.append(messages.get(i).getText()).append("\n");
                }
            }
            output.writeUTF(stringBuilder.toString());


        } catch (Exception e) {
            output.writeUTF("Server: " + number + " is not a number!");
        }
    }

    private void revoke(String name) throws IOException {
        if (!user.isAdmin()) {
            output.writeUTF("Server: you are not an admin!");
        } else if (!dataBase.getClients().get(name).isModerator()) {
            output.writeUTF("Server: this user is not a moderator!");
        } else {
            dataBase.getClients().get(name).setModerator(false);
            if (dataBase.getClientsLogged().containsKey(name)) {
                Socket moderator = dataBase.getClientsLogged().get(name);
                DataOutputStream outputModerator = new DataOutputStream(moderator.getOutputStream());
                outputModerator.writeUTF("Server: you are no longer a moderator!");
                output.writeUTF("Server: " + name + " is no longer a moderator!");
            }
        }
    }

    private void unread() throws IOException {
        StringBuilder unreadList = new StringBuilder();
        unreadList.append("Server: unread from:");
        boolean notEmpty = false;
        ArrayList<String> senders = new ArrayList<>();
        for (Account sender : user.getNewIncomingMessages().keySet()) {
            if (!user.getNewIncomingMessages().get(sender).isEmpty()) {
                notEmpty = true;
                senders.add(sender.getLogin());
            }
        }
        Collections.sort(senders);
        for (String name : senders) {
            unreadList.append(" ").append(name);
        }
        if (notEmpty) {
            output.writeUTF(unreadList.toString());
        } else {
            output.writeUTF("Server: no one unread");
        }
    }


    private void startChat(String login) {
        try {
            if (dataBase.getClients().containsKey(login)) {
                inChat = true;
                chatClient = dataBase.getClients().get(login);
                if (!chatClient.getIncomingMessages().containsKey(user)) {
                    chatClient.getIncomingMessages().put(user, new ArrayList<Message>());
                    chatClient.getNewIncomingMessages().put(user, new ArrayList<Message>());
                }
                if (!user.getIncomingMessages().containsKey(chatClient)) {
                    user.getIncomingMessages().put(chatClient, new ArrayList<Message>());
                    user.getNewIncomingMessages().put(chatClient, new ArrayList<Message>());
                }
                user.setInChatWith(dataBase.getClients().get(login));
                DataOutputStream output = new DataOutputStream(socket.getOutputStream());
                ArrayList<Message> incomingMessages = user.getIncomingMessages().get(chatClient);
                ArrayList<Message> newIncomingMessages = user.getNewIncomingMessages().get(chatClient);
                if (!incomingMessages.isEmpty() || !newIncomingMessages.isEmpty()) {
                    int counter = 0;
                    if (newIncomingMessages.size() >= 25) {
                        for (int i = newIncomingMessages.size() - 25; counter < 25; counter++) {
                            output.writeUTF("(new) " + newIncomingMessages.get(i).getText());
                            i++;
                        }
                    } else {
                        if (newIncomingMessages.size() + incomingMessages.size() <= 25) {
                            if (incomingMessages.size() <= 10) {
                                for (Message s : incomingMessages) {
                                    output.writeUTF(s.getText());
                                }
                            } else {
                                for (int i = incomingMessages.size() - 10; i < incomingMessages.size(); i++) {
                                    output.writeUTF(incomingMessages.get(i).getText());
                                }
                            }
                            for (Message s : newIncomingMessages) {
                                output.writeUTF("(new) " + s.getText());
                            }
                        } else {
                            int diff = 25 - newIncomingMessages.size();
                            for (int i = incomingMessages.size() - diff; i < incomingMessages.size(); i++) {
                                output.writeUTF(incomingMessages.get(i).getText());
                            }
                            for (Message s : newIncomingMessages) {
                                output.writeUTF("(new) " + s.getText());
                            }
                        }

                    }
                    for (Message msg : newIncomingMessages) {
                        user.getIncomingMessages().get(chatClient).add(msg);
                    }
                    user.getNewIncomingMessages().get(chatClient).clear();
                }
                Serializator.serialize(dataBase, "D:\\ChatOnline\\Online Chat\\task\\src\\chatcitizens.data");
                System.out.println("Client " + id + " in chat!");
            } else if (!dataBase.getClientsLogged().containsKey(login)) {
                output.writeUTF("Server: the user is not online!");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void kick(String name) throws IOException {
        if (name.equals(user.getLogin())) {
            output.writeUTF("Server: you can't kick yourself!");
        } else if (name.equals("admin")) {
            output.writeUTF("Server: you can`t kick an admin");
        } else if (user.isModerator() && dataBase.getClients().get(name).isModerator()) {
            output.writeUTF("Server: you can`t kick the another moderator");
        } else if (!user.isModerator() && !user.isAdmin()) {
            output.writeUTF("Server: you are not a moderator or an admin!");
        } else {
            dataBase.getClients().get(name).setBanned(true);
            if (dataBase.getClientsLogged().containsKey(name)) {
                Socket moderator = dataBase.getClientsLogged().get(name);
                DataOutputStream outputModerator = new DataOutputStream(moderator.getOutputStream());
                output.writeUTF("Server: " + name + " was kicked!");
                outputModerator.writeUTF("Server: you have been kicked out of the server!");
                if (dataBase.getClientsLogged().containsKey(name)) {
                    dataBase.removeLoggedClient(name);
                }

            }
        }
    }

    @Override
    public void run() {
        try {
            output.writeUTF("Server: authorize or register");
            while (!isInterrupted()) {
                String msg = input.readUTF();
                try {
                    if (user.isBanned()) {
                        System.out.println("Client " + id + " kicked!");
                        dataBase.removeLoggedClient(user.getLogin());
                        output.writeUTF("Server: you are not in the chat!");
                        output.writeUTF("Server: you are banned!");
                        continue;
                    }
                } catch (NullPointerException ignored) {

                }

                if (!authorized) {
                    if (msg.startsWith("/")) {
                        String[] msgSplit = msg.split(" ");
                        switch (msgSplit[0]) {
                            case "/registration":
                                register(msgSplit[1], msgSplit[2]);
                                break;
                            case "/auth":
                                auth(msgSplit[1], msgSplit[2]);
                                break;
                            case "/exit":
                                Serializator.serialize(dataBase, "D:\\ChatOnline\\Online Chat\\task\\src\\chatcitizens.data");
                                user.setInChatWith(new Account("ABOOBOBOBOBOBOOBOB", "1231212312321"));
                                return;
                            case "/list":

                            case "/chat":
                                output.writeUTF("Server: you are not in the chat!");
                                break;
                            default:
                                output.writeUTF("Server: incorrect command!");
                        }
                    } else {
                        output.writeUTF("Server: you are not in the chat!");
                    }
                } else if (!inChat) {
                    if (msg.startsWith("/")) {
                        String[] msgSplit = msg.split(" ");
                        switch (msgSplit[0]) {
                            case "/list":
                                list();
                                break;
                            case "/chat":
                                startChat(msgSplit[1]);
                                break;
                            case "/grant":
                                grant(msgSplit[1]);
                                break;
                            case "/revoke":
                                revoke(msgSplit[1]);
                                break;
                            case "/kick":
                                kick(msgSplit[1]);
                                break;
                            case "/unread":
                                unread();
                                break;
                            case "/exit":
                                Serializator.serialize(dataBase, "D:\\ChatOnline\\Online Chat\\task\\src\\chatcitizens.data");
                                user.setInChatWith(new Account("ABOOBOBOBOBOBOOBOB", "1231212312321"));
                                dataBase.removeLoggedClient(user.getLogin());
                                return;
                            default:
                                output.writeUTF("Server: use /list command to choose a user to text!");
                        }
                    } else {
                        output.writeUTF("Server: use /list command to choose a user to text!");
                    }
                } else {
                    boolean left = false;
                    String[] split = msg.split(" ");
                    switch (split[0]) {
                        case "/list":
                            list();
                            continue;
                        case "/exit":
                            Serializator.serialize(dataBase, "D:\\ChatOnline\\Online Chat\\task\\src\\chatcitizens.data");
                            user.setInChatWith(new Account("ABOOBOBOBOBOBOOBOB", "1231212312321"));
                            interrupt();
                            left = true;
                            break;
                        case "/unread":
                            unread();
                            continue;
                        case "/stats":
                            stat();
                            continue;
                        case "/history":
                            history(split[1]);
                            continue;
                        case "/chat":
                            startChat(msg.split(" ")[1]);
                            continue;
                        case "/grant":
                            grant(msg.split(" ")[1]);
                            continue;
                        case "/revoke":
                            revoke(msg.split(" ")[1]);
                            continue;
                        case "/kick":
                            kick(msg.split(" ")[1]);
                            continue;
                    }


                    if (left) {
                        break;
                    }
                    System.out.println(user.getLogin() + " sent to " + chatClient.getLogin() + ": " + msg);
                    if (chatClient.getInChatWith().getLogin().equals(user.getLogin())) {
                        chatClient.addMessage(user, new Message(user.getLogin() + ": " + msg, true));
                        user.addMessage(chatClient, new Message(user.getLogin() + ": " + msg, true));
                        DataOutputStream outputToChatter = new DataOutputStream(dataBase.getClientsLogged().get(chatClient.getLogin()).getOutputStream());
                        output.writeUTF(user.getLogin() + ": " + msg);
                        outputToChatter.writeUTF(user.getLogin() + ": " + msg);
                    } else {
                        chatClient.addNewMessage(user, new Message(user.getLogin() + ": " + msg, false));
                        user.addMessage(chatClient, new Message(user.getLogin() + ": " + msg, false));
                        output.writeUTF(user.getLogin() + ": " + msg);
                    }
                    Serializator.serialize(dataBase, "D:\\ChatOnline\\Online Chat\\task\\src\\chatcitizens.data");
                }
            }
            System.out.println("Client " + id + " disconnected!");
            dataBase.removeLoggedClient(user.getLogin());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

