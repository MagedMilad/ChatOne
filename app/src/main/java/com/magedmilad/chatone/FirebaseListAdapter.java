//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.magedmilad.chatone;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.magedmilad.chatone.FirebaseArray.OnChangedListener;

public abstract class FirebaseListAdapter<T> extends BaseAdapter {
    private final Class<T> mModelClass;
    protected int mLayout;
    protected Activity mActivity;
    private   FirebaseArray mSnapshots;

    public FirebaseListAdapter(Activity activity, Class<T> modelClass, int modelLayout, Query ref) {
        this.mModelClass = modelClass;
        this.mLayout = modelLayout;
        this.mActivity = activity;
        this.mSnapshots = new FirebaseArray(ref);
        this.mSnapshots.setOnChangedListener(new OnChangedListener() {
            public void onChanged(EventType type, int index, int oldIndex) {
                FirebaseListAdapter.this.notifyDataSetChanged();
            }
        });
    }

    public FirebaseListAdapter(Activity activity, Class<T> modelClass, int modelLayout, DatabaseReference ref) {
        this(activity, modelClass, modelLayout, (Query)ref);
    }

    public void cleanup() {
        this.mSnapshots.cleanup();
    }

    public int getCount() {
        return this.mSnapshots.getCount();
    }

    public T getItem(int position) {
        return this.parseSnapshot(this.mSnapshots.getItem(position));
    }

    protected T parseSnapshot(DataSnapshot snapshot) {
        return snapshot.getValue(this.mModelClass);
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

        T model = this.getItem(position);
        this.populateView(view, model, position);
        return view;
    }

    protected abstract void populateView(View var1, T var2, int var3);
}
