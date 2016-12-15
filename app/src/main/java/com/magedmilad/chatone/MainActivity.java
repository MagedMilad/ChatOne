package com.magedmilad.chatone;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.magedmilad.chatone.Model.ChatMessage;
import com.magedmilad.chatone.Model.User;
import com.magedmilad.chatone.Utils.Constants;
import com.magedmilad.chatone.Utils.Utils;
import com.magedmilad.chatone.Utils.ViewPagerAdapter;
import com.magedmilad.chatone.login.LoginActivity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class MainActivity extends AppCompatActivity {

    User currentUser;
    String mCurrentUserEmail;
    //TODO : tabView
    private TabLayout tabLayout;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FirebaseApp.initializeApp(this);

        FirebaseAuth authData = FirebaseAuth.getInstance();
        if (authData.getCurrentUser() == null) {
            Intent intent = new Intent(getBaseContext(), LoginActivity.class);
            startActivity(intent);
            finish();
        } else {
            mCurrentUserEmail = authData.getCurrentUser().getEmail();
            Utils.getUser(mCurrentUserEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    currentUser = snapshot.getValue(User.class);
                    if(!isMyServiceRunning(NotificationService.class)){
                        Intent intent = new Intent(getBaseContext(), NotificationService.class);
                        intent.putExtra(Constants.INTENT_EXTRA_CURRENT_USER, currentUser);
                        intent.putExtra(Constants.INTENT_EXTRA_CURRENT_USER_EMAIL, mCurrentUserEmail);

                        startService(intent);
                    }

                }

                @Override
                public void onCancelled(DatabaseError firebaseError) {
                    Utils.showErrorToast(MainActivity.this, "Error : this Email and Password isn't Registered");
                }
            });
        }

        //TODO : tabView
        viewPager = (ViewPager) findViewById(R.id.view_pager);
        setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.getTabAt(0).setIcon(R.drawable.all_friends);
        tabLayout.getTabAt(1).setIcon(R.drawable.list);

        FloatingActionButton addFriendButton = (FloatingActionButton) findViewById(R.id.add_friend_button);
        addFriendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(
                        MainActivity.this);
                LayoutInflater inflater = MainActivity.this.getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.add_friend_dialog, null);
                builder.setView(dialogView);
                builder.setTitle("Enter your friend email");
                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AlertDialog dlg = (AlertDialog) dialog;
                        String email = ((TextView) dlg.findViewById(R.id.add_friend_edit_text)).getText().toString();
                        if (Utils.isFriend(currentUser, email)) {
                            Utils.showErrorToast(MainActivity.this, "You are Already Friend with " + email);
                            return;
                        }
                        addFriendAction(email);
                    }
                }).setNegativeButton("Cancel", null);

                AlertDialog dialog = builder.create();
                dialog.show();


            }
        });

        FloatingActionButton changeStatusButton = (FloatingActionButton) findViewById(R.id.change_status_button);
        changeStatusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(
                        MainActivity.this);
                LayoutInflater inflater = MainActivity.this.getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.add_friend_dialog, null);
                ((EditText) dialogView.findViewById(R.id.add_friend_edit_text)).setHint("status");
                builder.setView(dialogView);
                builder.setTitle("Enter new status message");
                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AlertDialog dlg = (AlertDialog) dialog;
                        String status = ((TextView) dlg.findViewById(R.id.add_friend_edit_text)).getText().toString();
//                        if(status.isEmpty()){
//                            //TODO : empty status
//                            return ;
//                        }
                        Utils.getUser(mCurrentUserEmail).child("status").setValue(status);
                    }
                }).setNegativeButton("Cancel", null);

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        FloatingActionButton logOutButton = (FloatingActionButton) findViewById(R.id.log_out_button);
        logOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopService(new Intent(getBaseContext(), NotificationService.class));
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(MainActivity.this.getBaseContext(), LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }

    //TODO : tabView
    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new ChatListFragment());
        adapter.addFragment(DetailsFragment.newInstance(mCurrentUserEmail));
        viewPager.setAdapter(adapter);
    }

    private void addFriendAction(String email) {
        final String newEmail = Utils.encriptEmail(email);
        Utils.getUser(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User friend = dataSnapshot.getValue(User.class);
                if (friend == null) {
                    Toast.makeText(MainActivity.this, "Error : this Email isn't Registered", Toast.LENGTH_LONG).show();
                    return;
                }

                DatabaseReference chat = Utils.getChats();
                DatabaseReference newChatRoom = chat.push();
                String roomKey = newChatRoom.getKey();
                currentUser.getFriends().add(newEmail);
                friend.getFriends().add(Utils.encriptEmail(mCurrentUserEmail));
                currentUser.getChatRoomId().add(roomKey);
                friend.getChatRoomId().add(roomKey);
                Utils.getUser(mCurrentUserEmail).setValue(currentUser);
                Utils.getUser(newEmail).setValue(friend);
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {
                Toast.makeText(MainActivity.this, "Error : this Email isn't Registered", Toast.LENGTH_LONG).show();

            }
        });

    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

//    private void notification() {
//        for (int i = 1; i < currentUser.getChatRoomId().size(); i++) {
//            DatabaseReference mFirebaseRef = Utils.getChat(currentUser.getChatRoomId().get(i));
//            mFirebaseRef.keepSynced(true);
//            final int finalI = i;
//            mFirebaseRef.addChildEventListener(new ChildEventListener() {
//                @Override
//                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
//                    //TODO :notification
//                    ChatMessage newMessage = dataSnapshot.getValue(ChatMessage.class);
//                    if (!newMessage.isNotified() && !mCurrentUserEmail.equals(newMessage.getSenderEmail())) {
//                        Utils.showNotification(MainActivity.this, createNewMessageIntent(), "New Messeage From " + newMessage.getName(), newMessage.getMessage(), currentUser.getChatRoomId().get(finalI).hashCode());
//                        Map<String, Object> notifiedMessage = new HashMap<>();
//                        notifiedMessage.put("notified", true);
//                        dataSnapshot.getRef().updateChildren(notifiedMessage);
//                    }
//                }
//
//                @Override
//                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
//
//                }
//
//                @Override
//                public void onChildRemoved(DataSnapshot dataSnapshot) {
//
//                }
//
//                @Override
//                public void onChildMoved(DataSnapshot dataSnapshot, String s) {
//
//                }
//
//                @Override
//                public void onCancelled(DatabaseError firebaseError) {
//
//                }
//
//                private Intent createNewMessageIntent() {
//                    Intent intent = new Intent(MainActivity.this, ChatRoom.class);
//                    intent.putExtra(Constants.INTENT_EXTRA_FRIEND_EMAIL, currentUser.getFriends().get(finalI));
//                    intent.putExtra(Constants.INTENT_EXTRA_CURRENT_USER, currentUser);
//                    intent.putExtra(Constants.INTENT_EXTRA_CURRENT_USER_EMAIL, mCurrentUserEmail);
//                    return intent;
//                }
//            });
//
//        }
//    }

}
