package com.magedmilad.chatone;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.magedmilad.chatone.Model.GroupChat;
import com.magedmilad.chatone.Model.User;
import com.magedmilad.chatone.Utils.Constants;
import com.magedmilad.chatone.Utils.Utils;
import com.magedmilad.chatone.Utils.ViewPagerAdapter;
import com.magedmilad.chatone.login.LoginActivity;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    User currentUser;
    String mCurrentUserEmail;

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


        ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
        setupViewPager(viewPager);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.getTabAt(0).setIcon(R.drawable.all_friends);
        //TODO: change to actual icon
        tabLayout.getTabAt(1).setIcon(R.drawable.all_friends);
        tabLayout.getTabAt(2).setIcon(R.drawable.list);

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

        FloatingActionButton startGroupChat = (FloatingActionButton) findViewById(R.id.start_group_chat);
        startGroupChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(
                        MainActivity.this);
                LayoutInflater inflater = MainActivity.this.getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.group_chat_dialog, null);
                ListView friendsList = (ListView) dialogView.findViewById(R.id.friends_list_view);
                DatabaseReference ref = Utils.getUser(mCurrentUserEmail).child("friends");
                final ArrayList<String> selected = new ArrayList<>();
                selected.add(mCurrentUserEmail);
                ref.keepSynced(true);
                ArrayList<String> list = currentUser.getFriends();
                if(list.contains(Constants.GLOBAL_EMAIL)){
                    list.remove(Constants.GLOBAL_EMAIL);
                }
                if(list.contains(mCurrentUserEmail)){
                    list.remove(mCurrentUserEmail);
                }
                ArrayAdapter lAdapter = new ArrayAdapter<String>(MainActivity.this, R.layout.group_chat_friend_layout, list){
                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        View v = convertView;
                        if (v == null) {
                            LayoutInflater vi;
                            vi = LayoutInflater.from(getContext());
                            v = vi.inflate(R.layout.group_chat_friend_layout, null);
                        }
                        final int pos = position;
                        Utils.setUserView(MainActivity.this, getItem(position), v);
                        CheckBox chosenFriend = (CheckBox) v.findViewById(R.id.chosen_friend);
                        chosenFriend.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                if(isChecked)
                                    selected.add(getItem(pos));
                                else
                                    selected.remove(getItem(pos));
                            }
                        });
                        return v;
                    }
                };
                friendsList.setAdapter(lAdapter);
                builder.setView(dialogView);
                builder.setTitle("Choose friends");
                builder.setPositiveButton("Create Group chat", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AlertDialog dlg = (AlertDialog) dialog;
                        EditText chatNameEditText = (EditText) dlg.findViewById(R.id.chat_name);
                        String chatName = chatNameEditText.getText().toString();
                        if(chatName.isEmpty()){
                            Utils.showErrorToast(MainActivity.this, "chat name can't be empty");
                            return;
                        }
                        if(selected.size() < 3){
                            Utils.showErrorToast(MainActivity.this, "select at least 2 friends");
                            return;
                        }
                        startGroupChatAction(chatName, selected);
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

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new ChatListFragment());
        adapter.addFragment(new GroupChatListFragment());
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

    private void startGroupChatAction(String chatName, ArrayList<String> emails) {
        DatabaseReference groupChat = Utils.getGroupChats();
        DatabaseReference newChatRoom = groupChat.push();
        newChatRoom.setValue(new GroupChat(chatName, emails));
        final String roomKey = newChatRoom.getKey();
        for(int i=0;i<emails.size();i++){

            final String friendEmail = emails.get(i);
            Utils.getUser(friendEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    User friend = dataSnapshot.getValue(User.class);
                    if (friend == null) {
                        Toast.makeText(MainActivity.this, "Error : this Email isn't Registered", Toast.LENGTH_LONG).show();
                        return;
                    }
                    friend.getGroupChatRoomId().add(roomKey);
                    Utils.getUser(friendEmail).setValue(friend);
                }

                @Override
                public void onCancelled(DatabaseError firebaseError) {
                    Toast.makeText(MainActivity.this, "Error : this Email isn't Registered", Toast.LENGTH_LONG).show();
                }
            });
        }
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
}
