package com.magedmilad.chatone;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.magedmilad.chatone.model.User;
import com.magedmilad.chatone.utils.Constants;
import com.magedmilad.chatone.utils.Utils;

public class GroupChatListFragment extends Fragment {
    private  String mCurrentUserEmail;
    private FirebaseGroupChatListAdapter chatRoomAdapter;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(getActivity());


        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            mCurrentUserEmail = auth.getCurrentUser().getEmail();
            DatabaseReference ref = Utils.getUser(mCurrentUserEmail).child("groupChatRoomId");
            ref.keepSynced(true);
            chatRoomAdapter = new FirebaseGroupChatListAdapter(getActivity(), R.layout.chat_room_layout, ref);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_list, container, false);
        ListView  mGroupChatRooms = (ListView) view.findViewById(R.id.chat_room_listview);

        mGroupChatRooms.setAdapter(chatRoomAdapter);

        mGroupChatRooms.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                final int itemPos = position;
                Utils.getUser(mCurrentUserEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        User  currentUser = snapshot.getValue(User.class);
                        Intent intent = new Intent(getActivity().getBaseContext(), GroupChatRoom.class);
                        intent.putExtra(Constants.INTENT_EXTRA_CURRENT_USER, currentUser);
                        intent.putExtra(Constants.INTENT_EXTRA_GROUP_CHAT_ROOM_ID, currentUser.getGroupChatRoomId().get(itemPos));
                        intent.putExtra(Constants.INTENT_EXTRA_CURRENT_USER_EMAIL, mCurrentUserEmail);
                        startActivity(intent);
                    }

                    @Override
                    public void onCancelled(DatabaseError firebaseError) {
                        Utils.showErrorToast(getActivity(), "Error : this Email and Password isn't Registered");
                    }
                });


            }
        });
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (chatRoomAdapter != null)
            chatRoomAdapter.cleanup();
    }
}
