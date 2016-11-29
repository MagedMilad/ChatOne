package com.magedmilad.chatone;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.magedmilad.chatone.Model.ChatMessage;
import com.magedmilad.chatone.Model.User;
import com.magedmilad.chatone.Utils.Constants;
import com.magedmilad.chatone.Utils.Utils;

import java.util.HashMap;
import java.util.Map;

public class ChatRoom extends AppCompatActivity {

    private User currentUser;
    private String friendEmail;
    private DatabaseReference mFirebaseRef;
    private EditText mMessageEdit;
    private FirebaseMessageRecyclerAdapter mAdapter;
    private String mCurrentUserEmail;
    private String mChatRoomId = "0";

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAdapter.cleanup();
    }

    private RecyclerView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FirebaseApp.initializeApp(this);

        mCurrentUserEmail = getIntent().getStringExtra(Constants.INTENT_EXTRA_CURRENT_USER_EMAIL);
        currentUser = (User) getIntent().getSerializableExtra(Constants.INTENT_EXTRA_CURRENT_USER);
        friendEmail = getIntent().getStringExtra(Constants.INTENT_EXTRA_FRIEND_EMAIL);
        if (friendEmail != null) {
            int idx = 0;
            for (String email : currentUser.getFriends()) {
                if (email.equals(friendEmail)) {
                    mChatRoomId =  currentUser.getChatRoomId().get(idx);
                    break;
                }
                idx++;
            }
        }

        mFirebaseRef = Utils.getChat(mChatRoomId);
        mMessageEdit = (EditText) findViewById(R.id.message_text);
        mListView = (RecyclerView) findViewById(R.id.chat_listview);
        mListView.setHasFixedSize(true);
        mListView.setLayoutManager(new LinearLayoutManager(this));
//        Utils.setUserView(ChatRoom.this, Utils.encriptEmail(mCurrentUserEmail), findViewById(R.id.send_circular_image_view));
        Utils.setUserImageView(ChatRoom.this, Utils.encriptEmail(mCurrentUserEmail), (ImageView)findViewById(R.id.send_circular_image_view));

        mFirebaseRef.keepSynced(true);

        mFirebaseRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                ChatMessage newMessage = dataSnapshot.getValue(ChatMessage.class);
                if (!newMessage.isNotified() && !mCurrentUserEmail.equals(newMessage.getSenderEmail())) {
                    Utils.showNotification(ChatRoom.this, createNewMessageIntent(), "New Messeage From " + newMessage.getName(), newMessage.getMessage(), mChatRoomId.hashCode());
                    Map<String, Object> notifiedMessage = new HashMap<String, Object>();
                    notifiedMessage.put("notified", true);
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
            public void onCancelled(DatabaseError firebaseError) {

            }

            private Intent createNewMessageIntent() {
                Intent intent = new Intent(ChatRoom.this, ChatRoom.class);
                intent.putExtra(Constants.INTENT_EXTRA_FRIEND_EMAIL, friendEmail);
                intent.putExtra(Constants.INTENT_EXTRA_CURRENT_USER, currentUser);
                intent.putExtra(Constants.INTENT_EXTRA_CURRENT_USER_EMAIL, mCurrentUserEmail);
                return intent;
            }
        });


        mAdapter = new FirebaseMessageRecyclerAdapter<ChatMessage, ChatHolder>(ChatMessage.class,  ChatHolder.class, mFirebaseRef,mCurrentUserEmail) {
            @Override
            protected void populateViewHolder(ChatHolder chatHolder, ChatMessage chatMessage, int i) {
                if(i == Constants.SENDER_LAYOUT_TYPE)
                    Utils.setUserImageView(ChatRoom.this, Utils.encriptEmail(mCurrentUserEmail), chatHolder.getImageView());
                else
                    Utils.setUserImageView(ChatRoom.this, Utils.encriptEmail(friendEmail), chatHolder.getImageView());
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