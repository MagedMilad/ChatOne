//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.magedmilad.chatone;

import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.magedmilad.chatone.FirebaseArray.OnChangedListener;
import com.magedmilad.chatone.model.GroupChat;
import com.magedmilad.chatone.utils.Utils;

public class FirebaseGroupChatListAdapter extends BaseAdapter {
    protected int mLayout;
    protected FragmentActivity mActivity;
    private FirebaseArray mSnapshots;

    public FirebaseGroupChatListAdapter(FragmentActivity activity, int modelLayout, Query ref) {
        this.mLayout = modelLayout;
        this.mActivity = activity;
        this.mSnapshots = new FirebaseArray(ref);
        this.mSnapshots.setOnChangedListener(new OnChangedListener() {
            public void onChanged(EventType type, int index, int oldIndex) {
                FirebaseGroupChatListAdapter.this.notifyDataSetChanged();
            }
        });
    }

    public FirebaseGroupChatListAdapter(FragmentActivity activity, int modelLayout, DatabaseReference ref) {
        this(activity, modelLayout, (Query)ref);
    }

    public void cleanup() {
        this.mSnapshots.cleanup();
    }

    public int getCount() {
        return this.mSnapshots.getCount();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }


    public DatabaseReference getRef(int position) {
        return this.mSnapshots.getItem(position).getRef();
    }

    public long getItemId(int i) {
        return (long)this.mSnapshots.getItem(i).getKey().hashCode();
    }

    public View getView(int position, View view, ViewGroup viewGroup) {
        if(view == null) {
            view = this.mActivity.getLayoutInflater().inflate(this.mLayout, viewGroup, false);
        }

        final View v = view;
        String chatRoomID = mSnapshots.getItem(position).getValue(String.class);
        Utils.getGroupChat(chatRoomID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                GroupChat chat = dataSnapshot.getValue(GroupChat.class);
                Utils.setGroupChatView(mActivity, chat, v);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //not needed for now
            }
        });
        return view;
    }


}
