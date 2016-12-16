package com.magedmilad.chatone.Utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.magedmilad.chatone.MainActivity;
import com.magedmilad.chatone.Model.User;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;


public class SaveUserTask extends AsyncTask<Uri, Void, Void> {
    public static int FILE_URL = 0;
    public static int EXTERNAL_URL = 1;

    private String mUserEmail;
    private String mUserName;
    private Activity mActivity;
    private ProgressDialog mProgressDialog;
    private int mType;



    public SaveUserTask(String userEmail, String userName, Activity a,ProgressDialog progressDialog, int type){
        mUserName = userName;
        mUserEmail = userEmail;
        mActivity = a;
        mProgressDialog = progressDialog;
        mType = type;
    }


    protected Void doInBackground(Uri... urls) {
        if(urls[0] == null){
            setUser("https://firebasestorage.googleapis.com/v0/b/firebase-chat-one.appspot.com/o/photos%2Fdefault_avatar.png?alt=media&token=a9e9b040-c156-4ed5-8711-90d472e78b04");
            return null;
        }

        StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(Constants.STORAGE_BUCKET_URL);
        StorageReference photoRef = storageRef.child("photos")
                .child(urls[0].getLastPathSegment());

        Bitmap bitmap =null;
        if(mType == FILE_URL){
            try {
                bitmap = Utils.getResizedBitmap(mActivity.getContentResolver().openInputStream(urls[0]));
//                bitmap = Utils.getResizedBitmap(new URL(urls[0].toString()).openStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if(mType == EXTERNAL_URL){
            try {
                bitmap = BitmapFactory.decodeStream(new URL(urls[0].toString()).openStream());
            } catch (IOException e) {
                e.printStackTrace();

            }
        }

        if(bitmap == null){
            bitmap = Bitmap.createBitmap(null);
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask ut = photoRef.putBytes(data);
        ut.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(com.google.firebase.storage.UploadTask.TaskSnapshot taskSnapshot) {
                Uri downloadLink = taskSnapshot.getMetadata().getDownloadUrl();
                String link = downloadLink.toString();
                setUser(link);
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                    }
                });
        while(ut.isInProgress());
        return null;
    }

    private void setUser(String link) {
        ArrayList<String> friends = new ArrayList<>();
        ArrayList<String> chatRoomId = new ArrayList<>();
        chatRoomId.add("0");
        friends.add(Constants.GLOBAL_EMAIL);
        User currentUser = new User(mUserName, friends, chatRoomId, link);
        Utils.getUser(mUserEmail).setValue(currentUser);
    }

    protected void onPostExecute(Void result) {
        mProgressDialog.dismiss();
        Intent intent = new Intent(mActivity, MainActivity.class);
        mActivity.startActivity(intent);
        mActivity.finish();
    }
}