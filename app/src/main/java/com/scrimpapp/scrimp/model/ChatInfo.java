package com.scrimpapp.scrimp.model;

public class ChatInfo {
    public String msg;
    public String userId;
    public String name;
    public boolean sender;

    public ChatInfo(String msg, String userId, String name, boolean sender) {
        this.msg = msg;
        this.userId = userId;
        this.name = name;
        this.sender = sender;
    }
}
