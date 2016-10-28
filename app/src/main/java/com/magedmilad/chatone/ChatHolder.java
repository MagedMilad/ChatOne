package com.magedmilad.chatone;

import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.mikhaellopez.circularimageview.CircularImageView;

/**
 * Created by magedmilad on 6/14/16.
 */
public class ChatHolder extends RecyclerView.ViewHolder {
    View mView;

    public ChatHolder(View itemView) {
        super(itemView);
        mView = itemView;
    }

    public void setMessage(String text) {
        TextView field = (TextView) mView.findViewById(R.id.message_text_view);
        field.setText(text);
    }

    public void setAvatar(Bitmap bm) {
        CircularImageView field = (CircularImageView) mView.findViewById(R.id.message_circular_image_view);
        field.setImageBitmap(bm);
    }
}