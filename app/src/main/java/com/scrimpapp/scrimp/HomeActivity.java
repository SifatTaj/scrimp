package com.scrimpapp.scrimp;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import de.hdodenhof.circleimageview.CircleImageView;

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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.scrimpapp.scrimp.util.DpLoader;
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
    private CircleImageView homeDP;

    private LottieAnimationView rippleAnimation;
    private LottieAnimationView searchingAnimation;

    public static FirebaseFirestore mFirestore;
    public static FirebaseDatabase mFirebaseDB;
    public static FirebaseAuth mAuth;

    public static String userDisplayName;

    public static List<String> matchesList;
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
        searchingAnimation = findViewById(R.id.search_animation);
        homeDP = findViewById(R.id.homeDP);

        matchesList = new ArrayList<>();
        nameList = new ArrayList<>();
        idList = new ArrayList<>();
        imgList = new ArrayList();

        lobbyListAdapter = new LobbyListAdapter(this, nameList, idList, imgList);

        mFirestore = FirebaseFirestore.getInstance();
        mFirebaseDB = FirebaseDatabase.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(false)
                .build();
        mFirestore.setFirestoreSettings(settings);
        mAuth = FirebaseAuth.getInstance();

        userId = mAuth.getCurrentUser().getUid();

        mFirestore.collection("users").document(userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                String dpUrl = task.getResult().getString("dp_url");
                new DpLoader(getApplicationContext(), homeDP).execute(dpUrl);
            }
        });

        homeDP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), UserProfileActivity.class);
                intent.putExtra("userId", userId);
                startActivity(intent);
            }
        });

        tvMatches.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLobby();
            }
        });

        btSetDest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!searching) {
                    setUserOnline(userId);
                    tvMatches.setVisibility(View.VISIBLE);
                    Map<String, GeoPoint> dest = new HashMap();
                    dest.put("dest_geopoint", userDest);
                    mFirestore.collection("users").document(userId).update("dest_geopoint", userDest);
                    lowerLat = userDest.getLatitude() - .006;
                    higherLat = userDest.getLatitude() + .006;
                    lowerLng = userDest.getLongitude() - .006;
                    higherLng = userDest.getLongitude() + .006;
                    matchMaking();
                    searchingAnimation.setVisibility(View.VISIBLE);
                    rippleAnimation.setVisibility(View.VISIBLE);
                    rippleAnimation.playAnimation();
                    searchingAnimation.playAnimation();
                    searching = true;
                    btSetDest.setText("Cancel");
                }
                else {
                    searching = false;
                    btSetDest.setText("Set Destination");
                    rippleAnimation.cancelAnimation();
                    searchingAnimation.cancelAnimation();
                    searchingAnimation.setVisibility(View.GONE);
                    rippleAnimation.setVisibility(View.GONE);
                    matchesList.clear();
                    nameList.clear();
                    imgList.clear();
                    idList.clear();
                    setUserOffline(userId);
                    tvMatches.setVisibility(View.INVISIBLE);
                    tvMatches.setText("Searching");
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

    private void setUserOffline(String userId) {
        mFirestore.collection("users").document(userId).update("online", false);
        mFirebaseDB.getReference("status/" + userId).setValue("offline");
        mFirebaseDB.getReference("/status/" + userId).onDisconnect().setValue("offline");
    }

    public void showLobby() {
        ResultDialog exampleDialog = new ResultDialog();
        exampleDialog.show(getSupportFragmentManager(), "example dialog");
    }

    private void matchMaking() {
        mFirestore.collection("users").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                for(DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {

                    if((doc.getType() == DocumentChange.Type.ADDED | doc.getType() == DocumentChange.Type.MODIFIED) & searching & matchesList.size() <= 4) {

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

                                    if(doc.getDocument().getBoolean("online") & !matchesList.contains(doc.getDocument().getString("user_id"))) {
                                        matchesList.add(doc.getDocument().getString("user_id"));
                                        nameList.add(doc.getDocument().getString("name"));
                                        idList.add(doc.getDocument().getString("bracu_id"));
                                        imgList.add(doc.getDocument().getString("dp_url"));
                                        lobbyListAdapter.notifyDataSetChanged();
                                        tvMatches.setText(matchesList.size() != 0 ? matchesList.size() + " found" : "Searching");
                                        Map<String, Object> setMatch = new HashMap<>();
                                        setMatch.put("matched", true);
//                                        mFirestore.collection("users").document(doc.getDocument().getString("user_id")).set(setMatch, SetOptions.merge());
                                        showLobby();
                                    }
                                    else if (!doc.getDocument().getBoolean("online")){
                                        String offlineId = doc.getDocument().getString("user_id");
                                        if(matchesList.contains(offlineId)) {
                                            matchesList.remove(offlineId);
                                            nameList.remove(doc.getDocument().getString("name"));
                                            idList.remove(doc.getDocument().getString("bracu_id"));
                                            imgList.remove(doc.getDocument().getString("dp_url"));
                                            lobbyListAdapter.notifyDataSetChanged();
                                            tvMatches.setText(matchesList.size() != 0 ? matchesList.size() + " found" : "Searching");
                                        }
                                    }

                                }
                            }
                            else
                                userDisplayName = doc.getDocument().getString("name");
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
