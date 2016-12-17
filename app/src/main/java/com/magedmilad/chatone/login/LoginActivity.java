package com.magedmilad.chatone.login;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.magedmilad.chatone.MainActivity;
import com.magedmilad.chatone.R;
import com.magedmilad.chatone.Utils.SaveUserTask;
import com.magedmilad.chatone.Utils.Utils;

public class LoginActivity extends AppCompatActivity {

    private EditText mUserEmailEditText, mPasswordEditText;
    private String mUserEmail, mPassword;
    private ProgressDialog mProgressDialog;
    private CallbackManager mCallbackManager;
    private LoginButton mLoginButtonFacebook;
    private SignInButton mLoginButtonGoogle;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private GoogleApiClient mGoogleApiClient;
    private int RC_SIGN_IN = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mUserEmailEditText = (EditText) findViewById(R.id.input_email);
        mPasswordEditText = (EditText) findViewById(R.id.input_password);
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("Loading");
        mProgressDialog.setMessage("Logging to your Account");
        mProgressDialog.setCancelable(false);
        /////////////////////////////////
        mCallbackManager = CallbackManager.Factory.create();
        mLoginButtonFacebook = (LoginButton) findViewById(R.id.login_button_facebook);
        mLoginButtonFacebook.setReadPermissions("email", "public_profile");
        mLoginButtonFacebook.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d("auth", "onSuccess");

                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d("auth", "onCancel");

                Utils.showErrorToast(LoginActivity.this, "The Request has been cancelled");
            }

            @Override
            public void onError(FacebookException error) {
                Log.d("auth", "onError");

                Utils.showErrorToast(LoginActivity.this, "The Request has been failed");
            }
        });
        if(AccessToken.getCurrentAccessToken() != null){
            LoginManager.getInstance().logOut();
        }
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                final FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    mProgressDialog.show();
                    // User is signed in
                    Log.d("auth====>", user.getDisplayName() + " " + user.getEmail() + " " + user.getPhotoUrl());
                    Utils.showErrorToast(LoginActivity.this, user.getDisplayName() + " " + user.getEmail() + " " + user.getPhotoUrl());

//                    String email = user.getEmail();
//                    for (UserInfo userInfo : user.getProviderData()) {
//                        if (email == null && userInfo.getEmail() != null) {
//                            email = userInfo.getEmail();
//
//                            break;
//                        }
//                    }

//                    Log.d("auth", "email is : " + email);
//                    Utils.showErrorToast(LoginActivity.this, email);



                    if (user.getEmail() != null) {
                        Utils.getDatabase().child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if(dataSnapshot.hasChild(Utils.encriptEmail(user.getEmail()))){
                                    Log.d("auth","user exists");
                                    mProgressDialog.dismiss();
                                    Intent intent = new Intent(getBaseContext(), MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                                else{
                                    Log.d("auth","doesn't user exists");
                                    new SaveUserTask(Utils.encriptEmail(user.getEmail()), user.getDisplayName(), LoginActivity.this, mProgressDialog,SaveUserTask.EXTERNAL_URL).execute(user.getPhotoUrl());
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                //// TODO: 12/16/16  show error msh 3arf eh el saraha
                                mProgressDialog.dismiss();

                            }
                        });

                    }else{
                        mProgressDialog.dismiss();
                    }

                }
            }
        };

        Log.d("auth", getString(R.string.default_web_client_id));
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Log.d("auth", "fail google");

                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        mLoginButtonGoogle = (SignInButton) findViewById(R.id.login_button_google);
        mLoginButtonGoogle.setSize(SignInButton.SIZE_STANDARD);
        mLoginButtonGoogle.setScopes(gso.getScopeArray());
        mLoginButtonGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInGoogle();
            }
        });
    }

    private void signInGoogle() {
        Log.d("auth", "signInGoogle");
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        } else {
            mCallbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d("auth", "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            GoogleSignInAccount acct = result.getSignInAccount();
            Log.d("auth", acct.getDisplayName() + " " + acct.getEmail());
            Utils.showErrorToast(LoginActivity.this, acct.getDisplayName() + " " + acct.getEmail());
            firebaseAuthWithGoogle(acct);

        } else {
            Log.d("auth", "failed google :(" + result.getStatus());
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d("auth", "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d("auth", "signInWithCredential:onComplete:" + task.isSuccessful());

                        if (!task.isSuccessful()) {
                            Log.w("auth", "signInWithCredential", task.getException());
                            Utils.showErrorToast(LoginActivity.this, task.getException().getMessage());
                        }

                    }
                });
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
                        finish();
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

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    private void handleFacebookAccessToken(AccessToken token) {
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d("auth", "hereererer");
                        if (!task.isSuccessful()) {
                            Utils.showErrorToast(LoginActivity.this, "The Request has been cancelled");
                            Log.d("auth", "The Request has been cancelled");
                            Log.d("auth", task.getException().getMessage());
                            Utils.showErrorToast(LoginActivity.this, task.getException().getMessage());

                        }

                    }
                });
    }

}
