package org.junior;

import java.io.Serializable;

public class Message implements Serializable {

    private Account from;
    private String to;
    private String text;

    public Message(Account from, String to, String text) {
        this.from = from;
        this.to = to;
        this.text = text;
    }


    public String getAuthorName() {
        return from.getName();
    }

    public String getAuthorPassword() {
        return from.getPassword();
    }

    public String getTargetName() {
        return to;
    }

    public String getMessage() {
        return text;
    }

    public void setMessage(String text) {
        this.text = text;
    }

    public boolean isPrivate() {
        return to == null ? false : !to.isEmpty();
    }

    @Override
    public String toString() {
        return "Message{" +
                "from=" + from +
                ", to='" + to + '\'' +
                ", text='" + text + '\'' +
                '}';
    }
}
