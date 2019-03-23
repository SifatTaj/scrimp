package com.scrimpapp.scrimp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserProfileActivity extends AppCompatActivity {

    CircleImageView dpImage;

    String dpUrl = "https://firebasestorage.googleapis.com/v0/b/scrimp-33075.appspot.com/o/profilephoto1.jpg?alt=media&token=5d9d959f-79df-4116-92c2-d7c9307dd90d";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        dpImage = findViewById(R.id.profileImage);

        new DpLoader(getApplicationContext(), dpImage).execute(dpUrl);
        }

}

class DpLoader extends AsyncTask<String, Void, Bitmap> {

    final Context context;
    final CircleImageView profileImage;

    DpLoader(Context context, CircleImageView profileImage) {
        this.context = context;
        this.profileImage = profileImage;
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
            Toast.makeText(context, "Could not load profile image", Toast.LENGTH_LONG).show();
            return null;
        }
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);
        profileImage.setImageBitmap(bitmap);
    }
}


