package com.scrimpapp.scrimp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

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

    private FirebaseFirestore firebaseFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        tvName = findViewById(R.id.tvName);
        tvUserId = findViewById(R.id.tvUserId);
        tvPhone = findViewById(R.id.tvPhone);
        tvBracuId = findViewById(R.id.tvBracuId);

        firebaseFirestore = FirebaseFirestore.getInstance();

        DocumentReference docRef = firebaseFirestore.collection("profiles").document("SyVdRWh5VhjkscU1J5yO");
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                String userId = documentSnapshot.getString("id");
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
