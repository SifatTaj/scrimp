package com.scrimpapp.scrimp.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class NetworkImageLoader extends AsyncTask<String, Void, Bitmap> {

    final Context context;
    final ImageView imageView;

    public NetworkImageLoader(Context context, ImageView profileImage) {
        this.context = context;
        this.imageView = profileImage;
    }

    @Override
    protected Bitmap doInBackground(String... src) {
        try {
            URL url = new URL(src[0]);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
//            Toast.makeText(context, "Could not load profile image", Toast.LENGTH_LONG).show();
            return null;
        }
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);
        imageView.setImageBitmap(bitmap);
    }
}
