package com.magedmilad.chatone;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.firebase.ui.FirebaseListAdapter;
import com.github.clans.fab.FloatingActionButton;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.magedmilad.chatone.Model.User;
import com.magedmilad.chatone.Utils.Constants;
import com.magedmilad.chatone.Utils.Utils;
import com.magedmilad.chatone.login.LoginActivity;
import com.mikhaellopez.circularimageview.CircularImageView;


public class MainActivity extends AppCompatActivity {

    User currentUser;
    String mCurrentUserEmail;
    Firebase mFirebaseRef;
    ListView mchatRooms;
    FirebaseListAdapter chatRoomAdapter;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mchatRooms = (ListView) findViewById(R.id.chat_room_listview);
        if(!FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this);
        }

        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);

        if (Firebase.getDefaultConfig().isPersistenceEnabled() == false) {
            Firebase.getDefaultConfig().setPersistenceEnabled(true);
            Firebase.setAndroidContext(this);
        }
        mFirebaseRef = new Firebase(Constants.BASE_URL);
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
        FloatingActionButton logOutButton = (FloatingActionButton) findViewById(R.id.log_out_button);
        logOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mFirebaseRef.unauth();
                Intent intent = new Intent(getBaseContext(), LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

//        mAuth = FirebaseAuth.getInstance();
//        mAuthListener = new FirebaseAuth.AuthStateListener() {
//            @Override
//            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
//                FirebaseUser user = firebaseAuth.getCurrentUser();
//                Log.d("main", user.getDisplayName() + " " + user.getEmail());
//            }};

        AuthData authData = mFirebaseRef.getAuth();
        if (authData != null) {
            mCurrentUserEmail = authData.getProviderData().get("email").toString();
            Firebase users = new Firebase(Constants.USERS_URL);

            users.child(Utils.encriptEmail(mCurrentUserEmail)).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    currentUser = snapshot.getValue(User.class);

                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {
                    Utils.showErrorToast(MainActivity.this, "Error : this Email and Password isn't Registered");
                }
            });

            Firebase ref = new Firebase(Constants.USERS_URL);
            ref = ref.child(Utils.encriptEmail(mCurrentUserEmail)).child("friends");
            ref.keepSynced(true);

            chatRoomAdapter = new FirebaseListAdapter<String>(this, String.class, R.layout.chat_room_layout, ref) {

                @Override
                protected void populateView(View view, String s, int i) {
                    String[] friend = Utils.split(s);
                    ((TextView) view.findViewById(R.id.friend_email_text_view)).setText(Utils.decriptEmail(friend[0]));
                    ((TextView) view.findViewById(R.id.friend_name_text_view)).setText(friend[1]);
                    if (Utils.decriptEmail(s.substring(0, s.indexOf("#"))).equals(Constants.GLOBAL_EMAIL)) {
                        Drawable myDrawable = getResources().getDrawable(R.drawable.global_avatar);
                        Bitmap bm = ((BitmapDrawable) myDrawable).getBitmap();
                        ((CircularImageView) view.findViewById(R.id.friend_circular_image_view)).setImageBitmap(bm);
                    } else {
                        String base64Image = friend[2];
                        byte[] imageAsBytes = Base64.decode(base64Image.getBytes(), Base64.DEFAULT);
                        Bitmap bm = BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length);
                        ((CircularImageView) view.findViewById(R.id.friend_circular_image_view)).setImageBitmap(bm);
                    }
                }
            };


            mchatRooms.setAdapter(chatRoomAdapter);

            mchatRooms.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent = new Intent(getBaseContext(), ChatRoom.class);
                    intent.putExtra(Constants.INTENT_EXTRA_CURRENT_USER, currentUser);
                    TextView friendEmail = (TextView) view.findViewById(R.id.friend_email_text_view);
                    intent.putExtra(Constants.INTENT_EXTRA_FRIEND_EMAIL, friendEmail.getText().toString());
                    intent.putExtra(Constants.INTENT_EXTRA_CURRENT_USER_EMAIL, mCurrentUserEmail);
                    CircularImageView friendAvatar = (CircularImageView) view.findViewById(R.id.friend_circular_image_view);
                    if(!friendEmail.getText().toString().equals(Constants.GLOBAL_EMAIL)) {
                        intent.putExtra(Constants.INTENT_EXTRA_FRIEND_IMAGE, ((BitmapDrawable) friendAvatar.getDrawable()).getBitmap());
                    }
                    startActivity(intent);
                }
            });
        } else {
            Intent intent = new Intent(getBaseContext(), LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (chatRoomAdapter != null)
            chatRoomAdapter.cleanup();

    }


    private void addFriendAction(String email) {
        Firebase users = new Firebase(Constants.USERS_URL);
        final String newEmail = Utils.encriptEmail(email);
        users.child(newEmail).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User friend = dataSnapshot.getValue(User.class);
                if (friend == null) {
                    Toast.makeText(MainActivity.this, "Error : this Email isn't Registered", Toast.LENGTH_LONG).show();
                    return;
                }

                Firebase chat = new Firebase(Constants.CHAT_URL);
                Firebase newChatRoom = chat.push();
                String roomKey = newChatRoom.getKey();
                currentUser.getFriends().add(newEmail + "#" + friend.getUserName() + "#" + friend.getAvatar());
                friend.getFriends().add(Utils.encriptEmail(mCurrentUserEmail) + "#" + currentUser.getUserName() + "#" + currentUser.getAvatar());
                currentUser.getChatRoomId().add(roomKey);
                friend.getChatRoomId().add(roomKey);
                Firebase users = new Firebase(Constants.USERS_URL);
                users.child(Utils.encriptEmail(Utils.encriptEmail(mCurrentUserEmail))).setValue(currentUser);
                users.child(Utils.encriptEmail(newEmail)).setValue(friend);

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Toast.makeText(MainActivity.this, "Error : this Email isn't Registered", Toast.LENGTH_LONG).show();

            }
        });

    }


}
