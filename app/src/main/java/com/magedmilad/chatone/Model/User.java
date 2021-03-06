package com.magedmilad.chatone.Model;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by magedmilad on 6/10/16.
 */
public class User implements Serializable {


    private String userName;
    private ArrayList<String> friends = new ArrayList<>();
    private ArrayList<String> chatRoomId = new ArrayList<>();
    private ArrayList<String> groupChatRoomId = new ArrayList<>();
    private String status;
    private String avatarUri;

    public User() {
    }

    public User(String userName, ArrayList<String> friends, ArrayList<String> chatRoomId, ArrayList<String> groupChatRoomId, String avatarUri) {
        this.userName = userName;
        this.friends = friends;
        this.chatRoomId = chatRoomId;
        this.status = "hey there, i'm using OneChat!";
        this.avatarUri = avatarUri;
        this.groupChatRoomId = groupChatRoomId;
    }

    public User(String userName, ArrayList<String> friends, ArrayList<String> chatRoomId, ArrayList<String> groupChatRoomId, String avatarUri, String status) {
        this(userName, friends, chatRoomId, groupChatRoomId, avatarUri);
        this.status = status;
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

    public String getStatus() {
        return status;
    }

    public String getAvatarUri() {
        return avatarUri;
    }

    public ArrayList<String> getGroupChatRoomId() {
        return groupChatRoomId;
    }
}
