package com.magedmilad.chatone;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.magedmilad.chatone.model.ChatMessage;
import com.magedmilad.chatone.model.User;
import com.magedmilad.chatone.utils.Constants;
import com.magedmilad.chatone.utils.Utils;

import java.util.HashMap;
import java.util.Map;

public class NotificationService extends Service {



    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("notify","started");
        final String mCurrentUserEmail = intent.getStringExtra(Constants.INTENT_EXTRA_CURRENT_USER_EMAIL);
        final User currentUser = (User) intent.getSerializableExtra(Constants.INTENT_EXTRA_CURRENT_USER);
        for (int i = 1; i < currentUser.getChatRoomId().size(); i++) {
            DatabaseReference mFirebaseRef = Utils.getChat(currentUser.getChatRoomId().get(i));
            mFirebaseRef.keepSynced(true);
            final int finalI = i;
            mFirebaseRef.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    //TODO :notification
                    ChatMessage newMessage = dataSnapshot.getValue(ChatMessage.class);
                    if (!newMessage.isNotified() && !mCurrentUserEmail.equals(newMessage.getSenderEmail())) {
                        Utils.showNotification(getApplicationContext(), createNewMessageIntent(), "New Messeage From " + newMessage.getName(), newMessage.getMessage(), currentUser.getChatRoomId().get(finalI).hashCode());
                        Map<String, Object> notifiedMessage = new HashMap<>();
                        notifiedMessage.put("notified", true);
                        dataSnapshot.getRef().updateChildren(notifiedMessage);
                    }
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    // not needed for now

                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    // not needed for now

                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }                // not needed for now


                @Override
                public void onCancelled(DatabaseError firebaseError) {
                    // not needed for now

                }

                private Intent createNewMessageIntent() {
                    Intent intent = new Intent(getApplicationContext(), ChatRoom.class);
                    intent.putExtra(Constants.INTENT_EXTRA_FRIEND_EMAIL, currentUser.getFriends().get(finalI));
                    intent.putExtra(Constants.INTENT_EXTRA_CURRENT_USER, currentUser);
                    intent.putExtra(Constants.INTENT_EXTRA_CURRENT_USER_EMAIL, mCurrentUserEmail);
                    return intent;
                }
            });

        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d("notify","distoried");
        super.onDestroy();
    }
}
