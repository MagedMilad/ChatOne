package com.magedmilad.chatone.Model;

/**
 * Created by magedmilad on 6/10/16.
 */
public class ChatMessage {


    private String name;
    private String message;
    private String senderEmail;
    private boolean notified;


    public ChatMessage() {
    }

    public ChatMessage(String name, String message, String senderEmail, boolean notified) {
        this.name = name;
        this.message = message;
        this.senderEmail = senderEmail;
        this.notified = notified;
    }

    public String getName() {
        return name;
    }

    public String getMessage() {
        return message;
    }

    public String getSenderEmail() {
        return senderEmail;
    }


    public boolean isNotified() {
        return notified;
    }
}
