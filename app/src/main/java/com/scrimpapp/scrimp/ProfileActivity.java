package com.scrimpapp.scrimp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import javax.annotation.Nullable;

public class ProfileActivity extends AppCompatActivity {

    TextView tvUserId;
    TextView tvName;
    TextView tvPhone;
    TextView tvBracuId;

    private String userId;

    private FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDB;

    @Override
    protected void onStart() {
        super.onStart();
        if(mAuth.getCurrentUser() == null) {
            startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
        }
    }

    private void setUserOnline(String userId) {
        mFirestore.collection("users").document(userId).update("online", true);
        mFirebaseDB.getReference("status/" + userId).setValue("online");
        mFirebaseDB.getReference("/status/" + userId).onDisconnect().setValue("offline");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        tvName = findViewById(R.id.tvName);
        tvUserId = findViewById(R.id.tvUserId);
        tvPhone = findViewById(R.id.tvPhone);
        tvBracuId = findViewById(R.id.tvBracuId);

        Intent i = getIntent();
        userId = i.getStringExtra("userId");

        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDB = FirebaseDatabase.getInstance();

        setUserOnline(userId);

        DocumentReference docRef = mFirestore.collection("users").document(userId);
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                String userId = documentSnapshot.getString("email");
                String name = documentSnapshot.getString("name");
                String phone = documentSnapshot.getString("phone");
                String bracuId = documentSnapshot.getString("bracu_id");
                tvUserId.setText(userId);
                tvName.setText(name);
                tvPhone.setText(phone);
                tvBracuId.setText(bracuId);
            }
        });


    }
}
