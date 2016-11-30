package com.magedmilad.chatone.login;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RestrictTo;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserInfo;
import com.magedmilad.chatone.MainActivity;
import com.magedmilad.chatone.Model.User;
import com.magedmilad.chatone.R;
import com.magedmilad.chatone.Utils.Constants;
import com.magedmilad.chatone.Utils.Utils;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class LoginActivity extends AppCompatActivity {

    private Firebase mFirebaseRef;
    private EditText mUserEmailEditText, mPasswordEditText;
    private String mUserEmail, mPassword;
    private ProgressDialog mProgressDialog;
    CallbackManager mCallbackManager;
    LoginButton mLoginButtonFacebook;
    SignInButton mLoginButtonGoogle;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    GoogleApiClient mGoogleApiClient;
    int RC_SIGN_IN = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (!FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this);

        }
        Firebase.setAndroidContext(this);
        mFirebaseRef = new Firebase(Constants.BASE_URL);
        mUserEmailEditText = (EditText) findViewById(R.id.input_email);
        mPasswordEditText = (EditText) findViewById(R.id.input_password);
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("Loading");
        mProgressDialog.setMessage("Logging to your Account");
        mProgressDialog.setCancelable(false);

        // ...
        FacebookSdk.sdkInitialize(getApplicationContext());
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
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                final FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d("auth====>", user.getDisplayName() + " " + user.getEmail());
                    Utils.showErrorToast(LoginActivity.this, user.getDisplayName() + " " + user.getEmail());

                    String email = user.getEmail();
                    for (UserInfo userInfo : user.getProviderData()) {
                        if (email == null && userInfo.getEmail() != null) {
                            email = userInfo.getEmail();

                            break;
                        }
                    }

                    Log.d("auth", "email is : " + email);
                    if (email != null) {
                        Firebase users = new Firebase(Constants.USERS_URL);
                        users.keepSynced(true);
                        final String finalEmail = email;
                        users.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot snapshot) {
                                Log.d("auth", "called hhhhhhhhhh");
                                if (!snapshot.hasChild(finalEmail)) {
                                    Firebase users = mFirebaseRef.child("users");
                                    ArrayList<String> friends = new ArrayList<String>();
                                    ArrayList<String> chatRoomId = new ArrayList<String>();
                                    friends.add(Constants.GLOBAL_EMAIL + "#Global");
                                    chatRoomId.add("0");
                                    User currentUser = new User(user.getDisplayName(), friends, chatRoomId, encodeImage(null));
                                    String newEmail = Utils.encriptEmail(finalEmail);
                                    users.child(newEmail).setValue(currentUser);
                                }
                            }

                            @Override
                            public void onCancelled(FirebaseError firebaseError) {
                                Utils.showErrorToast(LoginActivity.this, "The Request has been cancelled");
                            }
                        });

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
        mLoginButtonGoogle.setOnClickListener(new OnClickListener() {
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
//                        else{
//
//                            Intent intent = new Intent(getBaseContext(), MainActivity.class);
//                            startActivity(intent);
//                        }
                    }
                });
    }


    public void onRegistrationButtonClick(View v) {
        Intent intent = new Intent(getBaseContext(), Registration.class);
        startActivity(intent);
    }


    public void onLoginButtonClick(View v) {
        Log.w("auth", "here in login");

        mUserEmail = mUserEmailEditText.getText().toString();
        mPassword = mPasswordEditText.getText().toString();

        mProgressDialog.show();

        mAuth.signInWithEmailAndPassword(mUserEmail, mPassword)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d("auth", String.valueOf(task.isSuccessful()));


                        if (!task.isSuccessful()) {
                            Log.w("auth", "no");
                            Utils.showErrorToast(LoginActivity.this, task.getException().getMessage());
                        }
//                        else {
//                            Log.w("auth", "yes");
//                            Intent intent = new Intent(getBaseContext(), MainActivity.class);
//                            startActivity(intent);
//                        }
                        mProgressDialog.dismiss();

                        // ...
                    }
                });

//        mFirebaseRef.authWithPassword(mUserEmail, mPassword,
//                new Firebase.AuthResultHandler() {
//                    @Override
//                    public void onAuthenticated(AuthData authData) {
//                        Firebase users = new Firebase(Constants.USERS_URL);
//                        String newEmail = Utils.encriptEmail(mUserEmail);
//                        mProgressDialog.dismiss();
//                        users.child(newEmail).addListenerForSingleValueEvent(new ValueEventListener() {
//                            @Override
//                            public void onDataChange(DataSnapshot snapshot) {
//                                User currentUser = snapshot.getValue(User.class);
//                                Intent intent = new Intent(getBaseContext(), MainActivity.class);
//                                startActivity(intent);
//                            }
//
//                            @Override
//                            public void onCancelled(FirebaseError firebaseError) {
//                                Utils.showErrorToast(LoginActivity.this, "The Request has been cancelled");
//                                mProgressDialog.dismiss();
//                            }
//                        });
//                    }
//
//                    @Override
//                    public void onAuthenticationError(FirebaseError error) {
//                        mProgressDialog.dismiss();
//                        switch (error.getCode()) {
//                            case FirebaseError.INVALID_EMAIL:
//                            case FirebaseError.USER_DOES_NOT_EXIST:
//                                mUserEmailEditText.setError("There was an error with your email");
//                                break;
//                            case FirebaseError.INVALID_PASSWORD:
//                                mPasswordEditText.setError(error.getMessage());
//                                break;
//                            case FirebaseError.NETWORK_ERROR:
//                                Utils.showErrorToast(LoginActivity.this, "Failed to Sign in Not Network");
//                                break;
//                            default:
//                                Utils.showErrorToast(LoginActivity.this, error.toString());
//                        }
//                    }
//                });


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
//                        else {
////                            task.getResult();
//                            Utils.showErrorToast(LoginActivity.this, "Tsssssssssd");
//                            Log.d("auth", "Tsssssssssd");
//
//                            Intent intent = new Intent(getBaseContext(), MainActivity.class);
//                            startActivity(intent);
//
//                        }
                    }
                });
    }

    private String encodeImage(Uri uri) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 7;
        Bitmap bitmap = null;
        if (uri != null) {
            bitmap = BitmapFactory.decodeFile(uri.getPath(), options);
        } else {
            bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.default_avatar, options);
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] bytes = baos.toByteArray();
        String base64Image = Base64.encodeToString(bytes, Base64.DEFAULT);
        return base64Image;

    }


}
