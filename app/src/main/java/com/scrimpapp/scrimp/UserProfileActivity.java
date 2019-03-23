package com.scrimpapp.scrimp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.annotation.Nullable;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserProfileActivity extends AppCompatActivity {

    CircleImageView dpImage;
    String dpUrl;

    FirebaseAuth mAuth;
    FirebaseFirestore mFirebaseFirestore;

    TextView tvName, tvPhone, tvEmail, tvBracuId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        dpImage = findViewById(R.id.profileImage);

        tvName = findViewById(R.id.tvUserProfileName);
        tvPhone = findViewById(R.id.tvUserProfilePhone);
        tvEmail = findViewById(R.id.tvUserProfileEmail);
        tvBracuId = findViewById(R.id.tvUserProfileBracuId);

        mAuth = FirebaseAuth.getInstance();
        mFirebaseFirestore = FirebaseFirestore.getInstance();

        String userId = mAuth.getCurrentUser().getUid();

        DocumentReference userInfo = mFirebaseFirestore.collection("users").document(userId);

        userInfo.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {

                if (documentSnapshot != null) {
                    tvName.setText(documentSnapshot.getString("name"));
                    tvPhone.setText(documentSnapshot.getString("phone"));
                    tvEmail.setText(documentSnapshot.getString("email"));
                    tvBracuId.setText(documentSnapshot.getString("bracu_id"));

                    dpUrl = documentSnapshot.getString("dp_url");
                    new DpLoader(getApplicationContext(), dpImage).execute(dpUrl);

                }
            }
        });

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
//            Toast.makeText(context, "Could not load profile image", Toast.LENGTH_LONG).show();
            return null;
        }
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);
        profileImage.setImageBitmap(bitmap);
    }
}


