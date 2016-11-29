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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.magedmilad.chatone.MainActivity;
import com.magedmilad.chatone.R;

public class LoginActivity extends AppCompatActivity {

    private EditText mUserEmailEditText , mPasswordEditText;
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
                          //TODO : complete error checking
                          mProgressDialog.dismiss();
//                          switch (e.getMessage().getCode()) {
//                              case DatabaseError.INVALID_EMAIL:
//                              case FirebaseError.USER_DOES_NOT_EXIST:
//                                  mUserEmailEditText.setError("There was an error with your email");
//                                  break;
//                              case FirebaseError.INVALID_PASSWORD:
//                                  mPasswordEditText.setError(error.getMessage());
//                                  break;
//                              case FirebaseError.NETWORK_ERROR:
//                                  Utils.showErrorToast(LoginActivity.this, "Failed to Sign in Not Network");
//                                  break;
//                              default:
//                                  Utils.showErrorToast(LoginActivity.this, error.toString());
//                          }
                      }
                  }

                );
    }
    }
