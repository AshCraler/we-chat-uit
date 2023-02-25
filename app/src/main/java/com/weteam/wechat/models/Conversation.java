package com.weteam.wechat.models;

import java.util.ArrayList;

public class Conversation {
    private ArrayList<String> messages;

    public Conversation(){}

    public Conversation(ArrayList<String> messages){
        this.messages = messages;
    }

    public ArrayList<String> getMessages() {
        return messages;
    }
}
