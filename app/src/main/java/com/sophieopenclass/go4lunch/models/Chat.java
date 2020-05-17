package com.sophieopenclass.go4lunch.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Chat {
    private List<Message> messages;
    private Map<String, String> users;

    public Chat(ArrayList<Message> messages, Map<String, String> users) {
        this.messages = messages;
        this.users = users;
    }

    public Map<String, String> getUsers() {
        return users;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(ArrayList<Message> messages) {
        this.messages = messages;
    }

    public void setUsers(Map<String, String> users) {
        this.users = users;
    }
}
