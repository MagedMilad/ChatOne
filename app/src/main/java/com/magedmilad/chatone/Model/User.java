package com.magedmilad.chatone.Model;

import android.widget.ArrayAdapter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by magedmilad on 6/10/16.
 */
public class User implements Serializable {


    private String userName;
    private ArrayList<String> friends = new ArrayList<String>();
    private ArrayList<String> chatRoomId = new ArrayList<String>();
    private String avatar;

    public User() {
    }

    public User(String userName, ArrayList<String> friends, ArrayList<String> chatRoomId, String avatar) {
        this.userName = userName;
        this.friends = friends;
        this.chatRoomId = chatRoomId;
        this.avatar = avatar;
    }

    public String getUserName() {
        return userName;
    }

    public ArrayList<String> getFriends() {
        return friends;
    }

    public ArrayList<String> getChatRoomId() {
        return chatRoomId;
    }

    public String getAvatar() {
        return avatar;
    }
}
