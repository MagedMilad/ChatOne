package com.magedmilad.chatone.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.widget.ImageView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.magedmilad.chatone.model.User;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by mina on 12/17/16.
 */

public class SetCombinedImageTask extends AsyncTask<String[], Void, ArrayList<Bitmap> > {

    private FragmentActivity mActivity;
    private ImageView mImageView;

    public SetCombinedImageTask(FragmentActivity activity, ImageView imageView){
        mActivity = activity;
        mImageView = imageView;
    }

    @Override
    protected ArrayList<Bitmap> doInBackground(String[]... params) {

        final ArrayList<String> links = new ArrayList<>();
        for(int i=0;i<3;i++) {
            Utils.getUser(params[0][i]).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    links.add(dataSnapshot.getValue(User.class).getAvatarUri());
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {}
            });
        }

        while(links.size() < 3);


        try {
            ArrayList<Bitmap> bitmaps = new ArrayList<>();
            for (int i = 0; i < links.size(); ++i) {
                bitmaps.add(Picasso.with(mActivity).load(links.get(i)).get());
            }
            return bitmaps;
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    protected void onPostExecute(ArrayList<Bitmap> bitmaps) {
        super.onPostExecute(bitmaps);
        if(bitmaps != null){
            mImageView.setImageBitmap(combine(bitmaps.get(0), bitmaps.get(1), bitmaps.get(2)));
        }
    }


    private Bitmap combine(Bitmap firstImage, Bitmap secondImage, Bitmap thirdImage){
        Bitmap result = Bitmap.createBitmap(firstImage.getWidth(), firstImage.getHeight(), firstImage.getConfig());
        Canvas canvas = new Canvas(result);
        canvas.drawBitmap(firstImage, 0, 0, null);
        canvas.drawBitmap(secondImage, firstImage.getWidth()/2, 0, null);
        canvas.drawBitmap(thirdImage, firstImage.getWidth()/2, firstImage.getHeight()/2, null);
        return result;
    }

}
