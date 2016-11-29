package com.magedmilad.chatone.Utils;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v7.app.NotificationCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.magedmilad.chatone.Model.User;
import com.magedmilad.chatone.R;
import com.squareup.picasso.Picasso;


public class Utils {
    private static FirebaseDatabase mDatabase;

    public static void showErrorToast(Context context,String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    public static String encriptEmail(String email) {
        email = email.replace(".", ",");
        return email;
    }

    public static String decriptEmail(String email) {
        email = email.replace(",", ".");
        return email;
    }

    public static boolean isFriend(User user , String email){
        if(email.equals("chat-one@firebase.con"))
            return true;
        email = encriptEmail(email);
        for(String s : user.getFriends()){
            if(s.equals(email)){
                return true;
            }
        }
        return false;
    }

    public static  void showNotification(Context c , Intent i , String title , String Content , int mode) {
        PendingIntent pi = PendingIntent.getActivity(c, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(c);
        mBuilder.setContentTitle(title)
                .setContentText(Content)
                .setContentIntent(pi)
                .setAutoCancel(true);

        if(mode == 0){
            mBuilder.setSmallIcon(R.drawable.ic_person_pin_black_24dp);
        }
        else{
            mBuilder.setSmallIcon(R.drawable.ic_local_post_office_black_24dp);
        }

        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        mBuilder.setSound(alarmSound);

        Notification notification = mBuilder.build();
        NotificationManager notificationManager = (NotificationManager) c.getSystemService(c.NOTIFICATION_SERVICE);
        notificationManager.notify(0, notification);
    }

    public static void setUserView(final Activity context, final String email, final View view){
        getUser(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                ImageView iv = (ImageView) view.findViewById(R.id.friend_circular_image_view);
                Picasso.with(context).load(decriptEmail(user.getAvatarUri())).into(iv);
                ((TextView) view.findViewById(R.id.status_text_view)).setText(user.getStatus());
                ((TextView) view.findViewById(R.id.friend_name_text_view)).setText(user.getUserName());
            }
            @Override
            public void onCancelled(DatabaseError firebaseError) {
            }
        });

    }

    public static void setUserImageView(final Activity context, final String email, final ImageView view){
        getUser(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                Picasso.with(context).load(decriptEmail(user.getAvatarUri())).into(view);
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {
            }
        });
    }

    private static DatabaseReference getDatabase() {
        if (mDatabase == null) {
            mDatabase = FirebaseDatabase.getInstance();
            mDatabase.setPersistenceEnabled(true);
        }
        return mDatabase.getReference();
    }

    public static DatabaseReference getUser(String email){
        return getDatabase().child("users").child(encriptEmail(email));
    }


    public static DatabaseReference getChat(String chatRoomId){
        return getDatabase().child("chat").child(chatRoomId);
    }

    public static DatabaseReference getChats(){
        return getDatabase().child("chat");
    }




}
