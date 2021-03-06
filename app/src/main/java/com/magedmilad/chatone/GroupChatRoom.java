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
import com.magedmilad.chatone.Model.GroupChat;
import com.magedmilad.chatone.Model.User;
import com.magedmilad.chatone.Utils.Constants;
import com.magedmilad.chatone.Utils.Utils;

public class GroupChatRoom extends AppCompatActivity {

    private User currentUser;
    private DatabaseReference mMesssagesRef;
    private DatabaseReference mDetailsRef;
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
        mChatRoomId = getIntent().getStringExtra(Constants.INTENT_EXTRA_GROUP_CHAT_ROOM_ID);

        mDetailsRef = Utils.getGroupChat(mChatRoomId);
        mMesssagesRef = Utils.getChat(mChatRoomId);
        mMessageEdit = (EditText) findViewById(R.id.message_text);
        mListView = (RecyclerView) findViewById(R.id.chat_listview);
        mListView.setHasFixedSize(true);
        mListView.setLayoutManager(new LinearLayoutManager(this));

        mDetailsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                GroupChat chat = dataSnapshot.getValue(GroupChat.class);
                GroupChatRoom.this.setTitle(chat.getName());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        mMesssagesRef.keepSynced(true);
        mMesssagesRef.addChildEventListener(new ChildEventListener() {
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


        mAdapter = new FirebaseMessageRecyclerAdapter<ChatMessage, ChatHolder>(ChatMessage.class,  ChatHolder.class, mMesssagesRef,mCurrentUserEmail) {
            int prev = 0;
            @Override
            protected void populateViewHolder(ChatHolder chatHolder, ChatMessage chatMessage, int i) {
                if(i != Constants.SENDER_LAYOUT_TYPE)
                  Utils.setUserImageView(GroupChatRoom.this, Utils.encriptEmail(chatMessage.getSenderEmail()), chatHolder.getImageView());

                chatHolder.setMessage(chatMessage.getMessage());
                //TODO :handle first entry case
                if(mAdapter != null && prev != mAdapter.getItemCount()) {
                    prev = mAdapter.getItemCount();
                    mListView.scrollToPosition(mAdapter.getItemCount());
                }
            }
        };
        mListView.setAdapter(mAdapter);
    }

    public void onSendButtonClick(View v) {
        String message = mMessageEdit.getText().toString();
        if (message.equals(""))
            return;
        mMesssagesRef.push().setValue(new ChatMessage(currentUser.getUserName(), message , mCurrentUserEmail , false));
        mMessageEdit.setText("");
    }
}