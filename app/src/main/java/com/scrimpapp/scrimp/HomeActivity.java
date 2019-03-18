package com.scrimpapp.scrimp;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

public class HomeActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Marker marker;
    private TextView tvLatLng, tvMatches;
    private FirebaseFirestore mFirestore;
    private List<String> matchesList;
    private Button btSetDest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        tvLatLng = findViewById(R.id.tvLatLng);
        tvMatches = findViewById(R.id.tvNoOfMatches);
        btSetDest = findViewById(R.id.btSetDest);

        matchesList = new ArrayList<>();

        mFirestore = FirebaseFirestore.getInstance();

        mFirestore.collection("users").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                for(DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {

                  if(doc.getType() == DocumentChange.Type.ADDED | doc.getType() == DocumentChange.Type.MODIFIED) {

                      try {
                          if(!doc.getDocument().getString("user_id").equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                              if(doc.getDocument().getString("destination").equals("uttara")) {

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

        btSetDest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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
//                marker.setPosition(markerPosition);//to center in map
                tvLatLng.setText("" + markerPosition.latitude + ", " + markerPosition.longitude);
            }
        });
    }
}
