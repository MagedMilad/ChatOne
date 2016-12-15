package com.magedmilad.chatone.login;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
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
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.magedmilad.chatone.MainActivity;
import com.magedmilad.chatone.R;
import com.magedmilad.chatone.Utils.Utils;

public class LoginActivity extends AppCompatActivity {

    private EditText mUserEmailEditText, mPasswordEditText;
    private String mUserEmail, mPassword;
    private ProgressDialog mProgressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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

        if (mUserEmail.isEmpty()) {
            mUserEmailEditText.setError("enter your email");
            return;
        }
        if (mPassword.isEmpty()) {
            mPasswordEditText.setError("enter your password");
            return;
        }

        //to avoid crashing due to conflict of auth and database
        mUserEmail = mUserEmail.toLowerCase();

        mProgressDialog.show();

        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.signInWithEmailAndPassword(mUserEmail, mPassword)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        mProgressDialog.dismiss();
                        Intent intent = new Intent(getBaseContext(), MainActivity.class);
                        startActivity(intent);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                                          @Override
                                          public void onFailure(Exception e) {
                                              mProgressDialog.dismiss();
                                              if (e instanceof FirebaseAuthInvalidCredentialsException) {
                                                  Utils.showErrorToast(LoginActivity.this, "entered email/password is invalid");
                                              } else if (e instanceof FirebaseAuthInvalidUserException) {
                                                  mUserEmailEditText.setError("no user found with this email");
                                                  mUserEmailEditText.requestFocus();
                                              } else if (e instanceof FirebaseNetworkException) {
                                                  Utils.showErrorToast(LoginActivity.this, "network error can't connect to server");
                                              } else {
                                                  Utils.showErrorToast(LoginActivity.this, e.getMessage());
                                              }
                                          }
                                      }

                );
    }
}
