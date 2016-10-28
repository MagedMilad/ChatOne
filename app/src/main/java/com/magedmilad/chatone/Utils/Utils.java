package com.magedmilad.chatone.Utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v7.app.NotificationCompat;
import android.util.Base64;
import android.widget.ImageView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.magedmilad.chatone.Model.User;
import com.magedmilad.chatone.R;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

/**
 * Created by magedmilad on 6/13/16.
 */
public class Utils {

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
            String userEmail = s.substring(0,s.indexOf("#"));
            if(userEmail.equals(email)){
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

    public static String[] split(String friend){
        return friend.split("#");
    }


}
