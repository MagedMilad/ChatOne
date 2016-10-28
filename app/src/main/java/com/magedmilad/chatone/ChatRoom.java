package com.magedmilad.chatone;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.firebase.ui.FirebaseListAdapter;
import com.firebase.ui.FirebaseRecyclerAdapter;
import com.magedmilad.chatone.Model.ChatMessage;
import com.magedmilad.chatone.Model.User;
import com.magedmilad.chatone.Utils.Constants;
import com.magedmilad.chatone.Utils.Utils;
import com.mikhaellopez.circularimageview.CircularImageView;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ChatRoom extends AppCompatActivity {

    private User currentUser;
    private String friendEmail;
    private Firebase mFirebaseRef;
    private EditText mMessageEdit;
    private FirebaseMessageRecyclerAdapter mAdapter;
    private String mCurrentUserEmail;
    private String mChatRoomId = "0";
    private Bitmap mFriendAvatar;
    private Bitmap mCurrentAvatar;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAdapter.cleanup();
    }

    private RecyclerView mListView;
    private FirebaseListAdapter<ChatMessage> mListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (Firebase.getDefaultConfig().isPersistenceEnabled() == false) {
            Firebase.getDefaultConfig().setPersistenceEnabled(true);
            Firebase.setAndroidContext(this);
        }
        mCurrentUserEmail =(String) getIntent().getStringExtra(Constants.INTENT_EXTRA_CURRENT_USER_EMAIL);
        currentUser = (User) getIntent().getSerializableExtra(Constants.INTENT_EXTRA_CURRENT_USER);
        friendEmail = (String) getIntent().getStringExtra(Constants.INTENT_EXTRA_FRIEND_EMAIL);
        mFriendAvatar = getIntent().getParcelableExtra(Constants.INTENT_EXTRA_FRIEND_IMAGE);
        if(mFriendAvatar==null){
            Drawable myDrawable = getResources().getDrawable(R.drawable.global_avatar);
            mFriendAvatar = ((BitmapDrawable) myDrawable).getBitmap();
        }

        byte[] imageAsBytes = Base64.decode(currentUser.getAvatar().getBytes(), Base64.DEFAULT);
        mCurrentAvatar =  BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length);



        if (friendEmail != null) {
            int idx = 0;
            for (String name : currentUser.getFriends()) {
                String [] friend = Utils.split(name);
                String email = Utils.decriptEmail(friend[0]);
                if (email.equals(friendEmail)) {
                    mChatRoomId =  currentUser.getChatRoomId().get(idx);
                    break;
                }
                idx++;
            }
        }

        mFirebaseRef = new Firebase(Constants.CHAT_URL).child(mChatRoomId);

        mMessageEdit = (EditText) findViewById(R.id.message_text);
        mListView = (RecyclerView) findViewById(R.id.chat_listview);
        mListView.setHasFixedSize(true);
        mListView.setLayoutManager(new LinearLayoutManager(this));
        ((CircularImageView)findViewById(R.id.send_circular_image_view)).setImageBitmap(mCurrentAvatar);




        mFirebaseRef.keepSynced(true);

        ChildEventListener listener = mFirebaseRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                ChatMessage newMessage = dataSnapshot.getValue(ChatMessage.class);

                if (!newMessage.isNotified() && !mCurrentUserEmail.equals(newMessage.getSenderEmail())) {
                    Utils.showNotification(ChatRoom.this, createNewMessageIntent(), "New Messeage From " + newMessage.getName(), newMessage.getMessage(), mChatRoomId.hashCode());

                    Map<String, Object> notifiedMessage = new HashMap<String, Object>();
                    notifiedMessage.put("notified", "true");
                    dataSnapshot.getRef().updateChildren(notifiedMessage);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }

            private Intent createNewMessageIntent() {
                Intent intent = new Intent(ChatRoom.this, ChatRoom.class);
                intent.putExtra(Constants.INTENT_EXTRA_CURRENT_USER, currentUser);
                intent.putExtra(Constants.INTENT_EXTRA_FRIEND_EMAIL, friendEmail);
                intent.putExtra(Constants.INTENT_EXTRA_CURRENT_USER_EMAIL, mCurrentUserEmail);
                if(!friendEmail.equals(Constants.GLOBAL_EMAIL)) {
                    intent.putExtra(Constants.INTENT_EXTRA_FRIEND_IMAGE,  mFriendAvatar);
                }
                return intent;
            }
        });


        mAdapter = new FirebaseMessageRecyclerAdapter<ChatMessage, ChatHolder>(ChatMessage.class,  ChatHolder.class, mFirebaseRef,mCurrentUserEmail) {
            @Override
            protected void populateViewHolder(ChatHolder chatHolder, ChatMessage chatMessage, int i) {
                if(i == Constants.SENDER_LAYOUT_TYPE)
                    chatHolder.setAvatar(mCurrentAvatar);
                else
                    chatHolder.setAvatar(mFriendAvatar);
                chatHolder.setMessage(chatMessage.getMessage());
            }


        };
        mListView.setAdapter(mAdapter);
    }

    public void onSendButtonClick(View v) {
        String message = mMessageEdit.getText().toString();
        if (message.equals(""))
            return;
        mFirebaseRef.push().setValue(new ChatMessage(currentUser.getUserName(), message , mCurrentUserEmail , false));
        mMessageEdit.setText("");
    }

}