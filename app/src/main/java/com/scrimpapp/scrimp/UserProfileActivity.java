package com.scrimpapp.scrimp;

import com.scrimpapp.scrimp.util.DpLoader;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.scrimpapp.scrimp.util.NetworkImageLoader;

import javax.annotation.Nullable;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserProfileActivity extends AppCompatActivity {

    CircleImageView dpImage;
    ImageView idImage;

    FirebaseAuth mAuth;
    FirebaseFirestore mFirebaseFirestore;

    TextView tvName, tvPhone, tvEmail, tvBracuId;
    Button btLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        dpImage = findViewById(R.id.profileImage);
        idImage = findViewById(R.id.idImage);

        tvName = findViewById(R.id.tvUserProfileName);
        tvPhone = findViewById(R.id.tvUserProfilePhone);
        tvEmail = findViewById(R.id.tvUserProfileEmail);
        tvBracuId = findViewById(R.id.tvUserProfileBracuId);
        btLogout = findViewById(R.id.btLogout);

        mAuth = FirebaseAuth.getInstance();
        mFirebaseFirestore = FirebaseFirestore.getInstance();

        Intent intent = getIntent();
        String userId = intent.getStringExtra("userId");

        String currentUserId = mAuth.getCurrentUser().getUid();

        if(!currentUserId.equals(userId)) {
            btLogout.setVisibility(View.GONE);
        }

        tvPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:0123456789"));
                startActivity(intent);
            }
        });

        btLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            }
        });

        DocumentReference userInfo = mFirebaseFirestore.collection("users").document(userId);

        userInfo.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {

                if (documentSnapshot != null) {
                    tvName.setText(documentSnapshot.getString("name"));
                    tvPhone.setText(documentSnapshot.getString("phone"));
                    tvEmail.setText(documentSnapshot.getString("email"));
                    tvBracuId.setText(documentSnapshot.getString("bracu_id"));

                    String dpUrl = documentSnapshot.getString("dp_url");
                    new DpLoader(getApplicationContext(), dpImage).execute(dpUrl);

                    String idUrl = documentSnapshot.getString("id_url");
                    new NetworkImageLoader(getApplicationContext(), idImage).execute(idUrl);

                }
            }
        });

    }
}


