package com.magedmilad.chatone;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.magedmilad.chatone.Model.User;
import com.magedmilad.chatone.Utils.Constants;
import com.magedmilad.chatone.Utils.Utils;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

public class DetailsFragment extends DialogFragment {

    private String email;
    private User currentUser;

    private TextView userName;
    private TextView userEmail;
    private TextView userStatus;
    private CircularImageView avatar;

    public static DetailsFragment newInstance(String email) {
        DetailsFragment fragment = new DetailsFragment();
        Bundle args = new Bundle();
        args.putString(Constants.INTENT_EXTRA_CURRENT_USER_EMAIL, email);
        fragment.setArguments(args);
        return fragment;
    }

    public DetailsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getArguments() != null) {
            email = getArguments().getString(Constants.INTENT_EXTRA_CURRENT_USER_EMAIL);
            FirebaseApp.initializeApp(getActivity());
            DatabaseReference user = Utils.getUser(email);

            user.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    currentUser = snapshot.getValue(User.class);
                    if(currentUser == null){
                        return ;
                    }
                    userStatus.setText(currentUser.getStatus());
                    userName.setText(currentUser.getUserName());
                    userEmail.setText(email);
                    Picasso.with(getActivity()).load(currentUser.getAvatarUri()).into(avatar);
                }

                @Override
                public void onCancelled(DatabaseError firebaseError) {

                }
            });
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_details, container, false);

        userName = (TextView) view.findViewById(R.id.user_name);
        userEmail = (TextView) view.findViewById(R.id.email);
        avatar = (CircularImageView) view.findViewById(R.id.avatar);
        userStatus = (TextView) view.findViewById(R.id.status);
        return view;
    }
}
