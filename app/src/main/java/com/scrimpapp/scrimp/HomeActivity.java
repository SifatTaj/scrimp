package com.scrimpapp.scrimp;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
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
import com.scrimpapp.scrimp.util.LobbyListAdapter;
import com.scrimpapp.scrimp.util.ResultDialog;

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

    public List<String> matchesList;
    public List<String> nameList;
    public List<String> idList;
    public List<String> imgList;
    public static LobbyListAdapter lobbyListAdapter;


    private String userId;
    private GeoPoint userDest;
    private boolean searching = false;

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
        nameList = new ArrayList<>();
        idList = new ArrayList<>();
        imgList = new ArrayList();

        lobbyListAdapter = new LobbyListAdapter(this, nameList, idList, imgList);

        mFirestore = FirebaseFirestore.getInstance();
        mFirebaseDB = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();

        userId = mAuth.getCurrentUser().getUid();

        btSetDest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!searching) {
                    setUserOnline(userId);
                    Map<String, GeoPoint> dest = new HashMap();
                    dest.put("dest_geopoint", userDest);
                    mFirestore.collection("users").document(userId).update("dest_geopoint", userDest);
                    lowerLat = userDest.getLatitude() - .006;
                    higherLat = userDest.getLatitude() + .006;
                    lowerLng = userDest.getLongitude() - .006;
                    higherLng = userDest.getLongitude() + .006;
                    matchMaking();
                    rippleAnimation.setVisibility(View.VISIBLE);
                    rippleAnimation.playAnimation();
                    searching = true;
                    btSetDest.setText("Cancel");
                    openDialog();
                }
                else {
                    searching = false;
                    btSetDest.setText("Set Destination");
                    rippleAnimation.cancelAnimation();
                    rippleAnimation.setVisibility(View.GONE);
                    matchesList.clear();
                    tvMatches.setText("0 found");
                }
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

    public void openDialog() {
        ResultDialog exampleDialog = new ResultDialog();
        exampleDialog.show(getSupportFragmentManager(), "example dialog");
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
                                        nameList.add(doc.getDocument().getString("name"));
                                        idList.add(doc.getDocument().getString("bracu_id"));
                                        imgList.add(doc.getDocument().getString("dp_url"));
                                        lobbyListAdapter.notifyDataSetChanged();
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
