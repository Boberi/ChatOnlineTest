package chat;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class Account implements Serializable {
    private final String login;
    private final String password;
    private boolean banned = false;
    private final boolean admin;
    private boolean moderator = false;
    private Map<Account, ArrayList<Message>> incomingMessages = new LinkedHashMap<>();
    private Map<Account, ArrayList<Message>> newIncomingMessages = new LinkedHashMap<>();

    public Map<Account, ArrayList<Message>> getIncomingMessages() {
        return incomingMessages;
    }

    public Map<Account, ArrayList<Message>> getNewIncomingMessages() {
        return newIncomingMessages;
    }

    public void addNewMessage(Account chatter, Message message) {
        newIncomingMessages.get(chatter).add(message);
    }

    public void addMessage(Account chatter, Message message) {
        incomingMessages.get(chatter).add(message);
    }

    public void setModerator(boolean moderator) {
        this.moderator = moderator;
    }

    public boolean isModerator() {
        return moderator;
    }

    private transient Account inChatWith;


    public Account(String login, String password) {
        this.login = login;
        this.password = password;
        this.admin = login.equals("admin");
    }

    public boolean isAdmin() {
        return admin;
    }

    public boolean isBanned() {
        return banned;
    }

    public void setBanned(boolean banned) {
        this.banned = banned;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public void setInChatWith(Account inChatWith) {
        this.inChatWith = inChatWith;
    }

    public Account getInChatWith() {
        if (inChatWith == null) {
            return new Account("kakakakakakakkakakakasdasdas", "12312312");
        }
        return inChatWith;
    }
}
