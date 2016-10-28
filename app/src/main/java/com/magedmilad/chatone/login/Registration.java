package com.magedmilad.chatone.login;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.magedmilad.chatone.MainActivity;
import com.magedmilad.chatone.Model.User;
import com.magedmilad.chatone.R;
import com.magedmilad.chatone.Utils.Constants;
import com.magedmilad.chatone.Utils.Utils;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;


public class Registration extends AppCompatActivity {

    private String mUserName, mUserEmail, mPassword;
    private Firebase mFirebaseRef;
    private EditText mUserNameEditText, mUserEmailEditText, mPasswordEditText;
    private ProgressDialog mProgressDialog;
    private CircularImageView mAvatarImage;
    private Uri mAvatarUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mFirebaseRef = new Firebase(Constants.BASE_URL);
        mUserNameEditText = (EditText) findViewById(R.id.input_name);
        mUserEmailEditText = (EditText) findViewById(R.id.input_email);
        mPasswordEditText = (EditText) findViewById(R.id.input_password);
        mAvatarImage = (CircularImageView) findViewById(R.id.avater_circular_image_view);
        mAvatarImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Your Avatar"), Constants.GET_FROM_GALLERY);

            }
        });
        Firebase.setAndroidContext(this);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("Loading");
        mProgressDialog.setMessage("Creating your Account");
        mProgressDialog.setCancelable(false);

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.GET_FROM_GALLERY && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            mAvatarImage.setImageBitmap(BitmapFactory.decodeFile(picturePath));
            mAvatarUri = Uri.fromFile(new File(picturePath));

        }
    }



    public void onSignupButtonClick(View v) {

        mUserName = mUserNameEditText.getText().toString();
        mUserEmail = mUserEmailEditText.getText().toString();
        mPassword = mPasswordEditText.getText().toString();

        if (!isUserNameValid(mUserName) || !isEmailValid(mUserEmail))
            return;

        mProgressDialog.show();

        mFirebaseRef.createUser(mUserEmail, mPassword, new Firebase.ResultHandler() {
            @Override
            public void onSuccess() {
                mFirebaseRef.authWithPassword(mUserEmail, mPassword,
                        new Firebase.AuthResultHandler() {
                            @Override
                            public void onAuthenticated(AuthData authData) {
                                Firebase users = mFirebaseRef.child("users");
                                ArrayList<String> friends = new ArrayList<String>();
                                ArrayList<String> chatRoomId = new ArrayList<String>();
                                friends.add(Constants.GLOBAL_EMAIL+"#Global");
                                chatRoomId.add("0");
                                User currentUser = new User(mUserName, friends, chatRoomId, encodeImage(mAvatarUri));
                                String newEmail = Utils.encriptEmail(mUserEmail);
                                users.child(newEmail).setValue(currentUser);
                                Intent intent = new Intent(getBaseContext(), MainActivity.class);
                                mProgressDialog.dismiss();
                                startActivity(intent);
                                finish();
                            }

                            @Override
                            public void onAuthenticationError(FirebaseError error) {
                                Utils.showErrorToast(Registration.this,error.toString());
                                mProgressDialog.dismiss();
                            }
                        });
            }

            @Override
            public void onError(FirebaseError firebaseError) {
                mProgressDialog.dismiss();
            }
        });

    }


    private boolean isEmailValid(String email) {
        boolean isGoodEmail =
                (email != null && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches());
        if (!isGoodEmail) {

            mUserEmailEditText.setFocusable(true);
            mUserEmailEditText.setError("This Email isn't Valid");
            return false;
        }
        return isGoodEmail;
    }

    private boolean isUserNameValid(String userName) {
        if (userName.equals("")) {
            mUserNameEditText.setFocusable(true);
            mUserNameEditText.setError("User Name Can't be Empty");
            return false;
        }
        return true;
    }


    private String encodeImage(Uri uri) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 7;
        Bitmap bitmap = null;
        if(uri!= null) {
            bitmap = BitmapFactory.decodeFile(uri.getPath(), options);
        }
        else{
            bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.default_avatar, options);
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] bytes = baos.toByteArray();
        String base64Image = Base64.encodeToString(bytes, Base64.DEFAULT);
        return base64Image;

    }

}
