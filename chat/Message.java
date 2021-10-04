package chat;

import java.io.Serializable;

public class Message implements Serializable {
    private String text;
    private boolean showed;

    public Message(String text, boolean showed) {
        this.text = text;
        this.showed = showed;
    }

    public String getText() {
        return text;
    }

    public void setShowed(boolean showed) {
        this.showed = showed;
    }

    public boolean isShowed() {
        return showed;
    }
}
