package com.magedmilad.chatone;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.magedmilad.chatone.Model.ChatMessage;
import com.magedmilad.chatone.Model.User;
import com.magedmilad.chatone.Utils.Constants;
import com.magedmilad.chatone.Utils.Utils;

public class ChatRoom extends AppCompatActivity {

    private User currentUser;
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
        String friendEmail = getIntent().getStringExtra(Constants.INTENT_EXTRA_FRIEND_EMAIL);
        if (friendEmail != null) {
            int idx = 0;
            for (String email : currentUser.getFriends()) {
                if (email!= null && email.equals(friendEmail)) {
                    mChatRoomId =  currentUser.getChatRoomId().get(idx);
                    break;
                }
                idx++;
            }
        }

        Utils.getUser(friendEmail).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User friend = dataSnapshot.getValue(User.class);
                ChatRoom.this.setTitle(friend.getUserName());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        mFirebaseRef = Utils.getChat(mChatRoomId);
        mMessageEdit = (EditText) findViewById(R.id.message_text);
        mListView = (RecyclerView) findViewById(R.id.chat_listview);
        mListView.setHasFixedSize(true);
        mListView.setLayoutManager(new LinearLayoutManager(this));
        mFirebaseRef.keepSynced(true);
        

        mFirebaseRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                mListView.smoothScrollToPosition(mAdapter.getItemCount());
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
        });


        mAdapter = new FirebaseMessageRecyclerAdapter<ChatMessage, ChatHolder>(ChatMessage.class,  ChatHolder.class, mFirebaseRef,mCurrentUserEmail) {
            @Override
            protected void populateViewHolder(ChatHolder chatHolder, ChatMessage chatMessage, int i) {
                if(i != Constants.SENDER_LAYOUT_TYPE)
                    Utils.setUserImageView(ChatRoom.this, Utils.encriptEmail(chatMessage.getSenderEmail()), chatHolder.getImageView());

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