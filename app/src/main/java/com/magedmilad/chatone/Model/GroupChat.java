package com.magedmilad.chatone.Model;

import java.util.ArrayList;

/**
 * Created by mina on 12/16/16.
 */

public class GroupChat {
    private String name;
    private ArrayList<String> emails;

    public GroupChat(){
        emails = new ArrayList<>();
    }

    public GroupChat(String name, ArrayList<String> emails){
        this.name = name;
        this.emails = emails;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<String> getEmails() {
        return emails;
    }

    public void setEmails(ArrayList<String> emails) {
        this.emails = emails;
    }

}
