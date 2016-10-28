package com.magedmilad.chatone.login;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.magedmilad.chatone.MainActivity;
import com.magedmilad.chatone.R;
import com.magedmilad.chatone.Model.User;
import com.magedmilad.chatone.Utils.Constants;
import com.magedmilad.chatone.Utils.Utils;

import java.util.ArrayList;

public class LoginActivity extends AppCompatActivity {

    private Firebase mFirebaseRef;
    private EditText mUserEmailEditText , mPasswordEditText;
    private String mUserEmail, mPassword;
    private ProgressDialog mProgressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Firebase.setAndroidContext(this);
        mFirebaseRef = new Firebase(Constants.BASE_URL);
        mUserEmailEditText = (EditText) findViewById(R.id.input_email);
        mPasswordEditText = (EditText) findViewById(R.id.input_password);
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("Loading");
        mProgressDialog.setMessage("Logging to your Account");
        mProgressDialog.setCancelable(false);
    }


    public void onRegistrationButtonClick(View v) {
        Intent intent = new Intent(getBaseContext(), Registration.class);
        startActivity(intent);
    }


    public void onLoginButtonClick(View v) {

        mUserEmail = mUserEmailEditText.getText().toString();
        mPassword = mPasswordEditText.getText().toString();

        mProgressDialog.show();

        mFirebaseRef.authWithPassword(mUserEmail, mPassword,
                new Firebase.AuthResultHandler() {
                    @Override
                    public void onAuthenticated(AuthData authData) {
                        Firebase users = new Firebase(Constants.USERS_URL);
                        String newEmail = Utils.encriptEmail(mUserEmail);
                        mProgressDialog.dismiss();
                        users.child(newEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot snapshot) {
                                User currentUser = snapshot.getValue(User.class);
                                Intent intent = new Intent(getBaseContext(), MainActivity.class);
                                startActivity(intent);
                            }

                            @Override
                            public void onCancelled(FirebaseError firebaseError) {
                                Utils.showErrorToast(LoginActivity.this,"The Request has been cancelled");
                                mProgressDialog.dismiss();
                            }
                        });
                    }

                    @Override
                    public void onAuthenticationError(FirebaseError error) {
                        mProgressDialog.dismiss();
                        switch (error.getCode()) {
                            case FirebaseError.INVALID_EMAIL:
                            case FirebaseError.USER_DOES_NOT_EXIST:
                                mUserEmailEditText.setError("There was an error with your email");
                                break;
                            case FirebaseError.INVALID_PASSWORD:
                                mPasswordEditText.setError(error.getMessage());
                                break;
                            case FirebaseError.NETWORK_ERROR:
                                Utils.showErrorToast(LoginActivity.this,"Failed to Sign in Not Network");
                                break;
                            default:
                                Utils.showErrorToast(LoginActivity.this,error.toString());
                        }
                    }
                });


    }


}
