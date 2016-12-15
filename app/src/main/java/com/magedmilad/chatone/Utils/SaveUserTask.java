package com.magedmilad.chatone.Utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
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
import java.util.ArrayList;


public class SaveUserTask extends AsyncTask<Uri, Void, Void> {
    private String mUserEmail;
    private String mUserName;
    private Activity mActivity;
    private ProgressDialog mProgressDialog;


    public SaveUserTask(String userEmail, String userName, Activity a,ProgressDialog progressDialog){
        mUserName = userName;
        mUserEmail = userEmail;
        mActivity = a;
        mProgressDialog = progressDialog;
    }


    protected Void doInBackground(Uri... urls) {
        if(urls[0] == null){
            setUser("https://firebasestorage.googleapis.com/v0/b/firebase-chat-one.appspot.com/o/photos%2Fdefault_avatar.png?alt=media&token=a9e9b040-c156-4ed5-8711-90d472e78b04");
            return null;
        }

        StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(Constants.STORAGE_BUCKET_URL);
        StorageReference photoRef = storageRef.child("photos")
                .child(urls[0].getLastPathSegment());

        //TODO :refactor
        Bitmap bitmap;
        try {
            bitmap = Utils.getResizedBitmap(mActivity.getContentResolver().openInputStream(urls[0]));
//            InputStream input = mActivity.getContentResolver().openInputStream(urls[0]);
//            bitmap = BitmapFactory.decodeStream(input);
//            input.close();
//            bitmap = MediaStore.Images.Media.getBitmap(mActivity.getContentResolver(), urls[0]);
        } catch (IOException e) {
            e.printStackTrace();
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