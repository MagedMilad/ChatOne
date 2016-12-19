package com.magedmilad.chatone.login;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.magedmilad.chatone.Model.User;
import com.magedmilad.chatone.R;
import com.magedmilad.chatone.Utils.Constants;
import com.magedmilad.chatone.Utils.SaveUserTask;
import com.magedmilad.chatone.Utils.Utils;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.io.File;
import java.util.ArrayList;


public class Registration extends AppCompatActivity {

    private String mUserName, mUserEmail, mPassword;
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

        mUserNameEditText = (EditText) findViewById(R.id.input_name);
        mUserEmailEditText = (EditText) findViewById(R.id.input_email);
        mPasswordEditText = (EditText) findViewById(R.id.input_password);
        mAvatarImage = (CircularImageView) findViewById(R.id.avater_circular_image_view);
        mAvatarImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, Constants.GET_FROM_GALLERY);
            }
        });
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

        if (!isUserNameValid(mUserName) || !isEmailValid(mUserEmail) || !isPasswordValid(mPassword))
            return;

        //to avoid crashing due to conflict of auth and database
        mUserEmail = mUserEmail.toLowerCase();

        mProgressDialog.show();

        final FirebaseAuth auth = FirebaseAuth.getInstance();

        auth.createUserWithEmailAndPassword(mUserEmail, mPassword)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        addGlobalFriend();
                        auth.signInWithEmailAndPassword(mUserEmail, mPassword)
                                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                    @Override
                                    public void onSuccess(AuthResult authResult) {
                                        new SaveUserTask(mUserEmail, mUserName, Registration.this, mProgressDialog,SaveUserTask.FILE_URL).execute(mAvatarUri);
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(Exception e) {
                                        if(e instanceof FirebaseNetworkException){
                                            Utils.showErrorToast(Registration.this, "network error can't connect to server");
                                        }
                                        else{
                                            Utils.showErrorToast(Registration.this, "cant login, try again later");
                                        }
                                        mProgressDialog.dismiss();
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        if(e instanceof FirebaseAuthWeakPasswordException){
                            mPasswordEditText.requestFocus();
                            mPasswordEditText.setError("weak password, password should be at least 6 characters");

                        }
                        else if(e instanceof FirebaseAuthUserCollisionException){
                            mUserEmailEditText.requestFocus();
                            mUserEmailEditText.setError("this email is already registered");

                        }
                        else if(e instanceof FirebaseAuthInvalidCredentialsException){
                            mUserEmailEditText.requestFocus();
                            mUserEmailEditText.setError("this email is invalid");

                        }
                        else if(e instanceof FirebaseNetworkException){
                            Utils.showErrorToast(Registration.this, "network error can't connect to server");
                        }
                        else{
                            Utils.showErrorToast(Registration.this, "cant register, try again later");
                        }
                        mProgressDialog.dismiss();
                    }
                });
    }

    private boolean isEmailValid(String email) {
        if(email == null){
            //error can't happen
            return false;
        }

        if(email.isEmpty()){
            mUserEmailEditText.requestFocus();
            mUserEmailEditText.setError("user email can't be empty");
            return false;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mUserEmailEditText.requestFocus();
            mUserEmailEditText.setError("this email is invalid");
            return false;
        }
        return true;
    }

    private boolean isUserNameValid(String userName) {
        if (userName.isEmpty()) {
            mUserNameEditText.requestFocus();
            mUserNameEditText.setError("user name can't be empty");
            return false;
        }
        return true;
    }

    private boolean isPasswordValid(String password) {
        if (password.isEmpty()) {
            mPasswordEditText.requestFocus();
            mPasswordEditText.setError("password can't be Empty");
            return false;
        }
        return true;
    }




    private void addGlobalFriend() {
        final DatabaseReference user =  Utils.getUser(Constants.GLOBAL_EMAIL);
        user.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User global = dataSnapshot.getValue(User.class);
                if (global == null) {
                    String link = "https://firebasestorage.googleapis.com/v0/b/firebase-chat-one.appspot.com/o/network-icon-1910.png?alt=media&token=fbcf3113-fda9-4c43-b70a-9054350c4b31";
                    ArrayList<String> chatRoomId = new ArrayList<>();
                    chatRoomId.add("0");
                    user.setValue(new User("Global", new ArrayList<String>(), chatRoomId, new ArrayList<String>(), link, "Meet new Friends here"));
                }
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {
                Utils.showErrorToast(Registration.this, firebaseError.getMessage());
            }
        });
    }

}
