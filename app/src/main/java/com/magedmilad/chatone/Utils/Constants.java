package com.magedmilad.chatone.Utils;

/**
 * Created by magedmilad on 6/13/16.
 */
public class Constants {
    public static final int SENDER_LAYOUT_TYPE=0;
    public static final int RECEIVER_LAYOUT_TYPE=1;
    public static final int GET_FROM_GALLERY=2;

    public static final String BASE_URL = "https://chat-one.firebaseio.com";
    public static final String USERS_URL = BASE_URL + "/users";
    public static final String CHAT_URL = BASE_URL + "/chat";
    public static final String IMAGES_URL = BASE_URL + "/images";

    public static final String STORAGE_BUCKET_URL = "gs://firebase-chat-one.appspot.com";

    public static final String GLOBAL_EMAIL = "chat-one@firebase.com";

    public static final String INTENT_EXTRA_CURRENT_USER = "current_user";
    public static final String INTENT_EXTRA_FRIEND_EMAIL = "friend_email";
    public static final String INTENT_EXTRA_CURRENT_USER_EMAIL = "current_user_email";


}