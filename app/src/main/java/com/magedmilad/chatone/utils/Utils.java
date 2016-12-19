package com.magedmilad.chatone.utils;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
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
import com.magedmilad.chatone.DetailsFragment;
import com.magedmilad.chatone.model.GroupChat;
import com.magedmilad.chatone.model.User;
import com.magedmilad.chatone.R;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.InputStream;

public class Utils {
    private static FirebaseDatabase mDatabase;

    public static void showErrorToast(Context context,String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    public static String encriptEmail(String email) {

        return email.replace(".", ",");
    }

    public static String decriptEmail(String email) {

        return email.replace(",", ".");
    }

    public static boolean isFriend(User user , String email){
        if("chat-one@firebase.com".equals(email))
            return true;
        String email_ = encriptEmail(email);
        for(String s : user.getFriends()){
            if(s!=null && s.equals(email_)){
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

    public static void setGroupChatView(final FragmentActivity context, final GroupChat chat, final View view){

        ((TextView) view.findViewById(R.id.friend_name_text_view)).setText(chat.getName());
        String names = "";
        for(int i=0;i<chat.getEmails().size();i++){
            names+=chat.getEmails().get(i);
            if(i != chat.getEmails().size()-1){
                names+=", ";
            }
        }
        if(names.length() > 20){
            names = names.substring(0, 20)+"...";
        }
        ((TextView) view.findViewById(R.id.status_text_view)).setText(names);
        ImageView iv = (ImageView) view.findViewById(R.id.friend_circular_image_view);
        new SetCombinedImageTask(context, iv).execute(chat.getEmails().toArray(new String[chat.getEmails().size()]));
    }

    public static void setUserImageView(final AppCompatActivity context, final String email, final ImageView view){
        getUser(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                Picasso.with(context).load(decriptEmail(user.getAvatarUri())).into(view);
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DetailsFragment modal = DetailsFragment.newInstance(email);
                        modal.setStyle(DialogFragment.STYLE_NORMAL, 0);
                        modal.show(context.getSupportFragmentManager(), "");
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {
            }
        });
    }

    public static DatabaseReference getDatabase() {
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

    public static DatabaseReference getGroupChat(String chatRoomId){
        return getDatabase().child("groupChat").child(chatRoomId);
    }

    public static DatabaseReference getChats(){
        return getDatabase().child("chat");
    }

    public static DatabaseReference getGroupChats(){
        return getDatabase().child("groupChat");
    }


    private static Bitmap reduceSize(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();
        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }
    public static Bitmap getResizedBitmap(InputStream input) throws IOException {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 8;
        Bitmap ret = reduceSize(BitmapFactory.decodeStream(input, null, options), 500);
        input.close();
        return ret;
    }
}