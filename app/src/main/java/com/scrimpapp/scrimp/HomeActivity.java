package com.scrimpapp.scrimp;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

public class HomeActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Marker marker;
    private TextView tvLatLng, tvMatches;
    private Button btSetDest;
    private LottieAnimationView rippleAnimation;

    private FirebaseFirestore mFirestore;
    private FirebaseDatabase mFirebaseDB;
    private FirebaseAuth mAuth;
    private List<String> matchesList;


    private String userId;
    private GeoPoint userDest;

    private double lowerLat = 0.0;
    private double lowerLng = 0.0;
    private double higherLat = 0.0;
    private double higherLng = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        tvLatLng = findViewById(R.id.tvLatLng);
        tvMatches = findViewById(R.id.tvNoOfMatches);
        btSetDest = findViewById(R.id.btSetDest);
        rippleAnimation = findViewById(R.id.ripple_animation);

        matchesList = new ArrayList<>();

        mFirestore = FirebaseFirestore.getInstance();
        mFirebaseDB = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();

        userId = mAuth.getCurrentUser().getUid();

        btSetDest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setUserOnline(userId);
                Map<String, GeoPoint> dest = new HashMap();
                dest.put("dest_geopoint", userDest);
                mFirestore.collection("users").document(userId).update("dest_geopoint", userDest);
                lowerLat = userDest.getLatitude() - .003;
                higherLat = userDest.getLatitude() + .003;
                lowerLng = userDest.getLongitude() - .003;
                higherLng = userDest.getLongitude() + .003;
                matchMaking();
                rippleAnimation.setVisibility(View.VISIBLE);
                rippleAnimation.playAnimation();
            }
        });

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void setUserOnline(String userId) {
        mFirestore.collection("users").document(userId).update("online", true);
        mFirebaseDB.getReference("status/" + userId).setValue("online");
        mFirebaseDB.getReference("/status/" + userId).onDisconnect().setValue("offline");
    }

    private void matchMaking() {
        mFirestore.collection("users").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                for(DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {

                    if(doc.getType() == DocumentChange.Type.ADDED | doc.getType() == DocumentChange.Type.MODIFIED) {

                        try {
                            if(!doc.getDocument().getString("user_id").equals(userId)) {

                                GeoPoint matchDest = doc.getDocument().getGeoPoint("dest_geopoint");
                                Log.d("match lat", "" + matchDest.getLatitude());
                                Log.d("match lng", "" + matchDest.getLongitude());

                                Log.d("high lat", "" + higherLat);
                                Log.d("high lng", "" + higherLng);

                                Log.d("Low lat", "" + lowerLat);
                                Log.d("Low lng", "" + lowerLng);

                                if((matchDest.getLatitude() < higherLat & matchDest.getLatitude() > lowerLat) & (matchDest.getLongitude() < higherLng & matchDest.getLongitude() > lowerLng)) {

                                    if(doc.getDocument().getBoolean("online")) {
                                        matchesList.add(doc.getDocument().getString("user_id"));
                                        tvMatches.setText(matchesList.size() + " found");
                                    }
                                    else if (!doc.getDocument().getBoolean("online")){
                                        if(matchesList.contains(doc.getDocument().getString("user_id"))) {
                                            matchesList.remove(doc.getDocument().getString("user_id"));
                                            tvMatches.setText(matchesList.size() + " found");
                                        }
                                    }

                                }
                            }
                        } catch (NullPointerException ne) {
                            ne.printStackTrace();
                        }

                    }

                }
            }
        });
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Add a marker in Sydney and move the camera
        final LatLng bracu = new LatLng(23.780281, 90.407151);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(bracu, 15F));
//        marker = mMap.addMarker(new MarkerOptions().position(bracu));
        mMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
                LatLng markerPosition = mMap.getCameraPosition().target;
                userDest = new GeoPoint(markerPosition.latitude, markerPosition.longitude);
                tvLatLng.setText("" + markerPosition.latitude + ", " + markerPosition.longitude);
            }
        });
    }
}
