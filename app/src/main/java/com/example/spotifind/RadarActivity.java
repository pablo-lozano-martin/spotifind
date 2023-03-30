package com.example.spotifind;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONException;

public class RadarActivity extends AppCompatActivity implements OnMapReadyCallback {
    private BottomNavigationView menuBar;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_radar);
        menuBar = findViewById(R.id.bottomNavigationView);
        menuBar.setSelectedItemId(R.id.radar);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /*@Override
    protected void startMap(boolean isRestore) {
        if (!isRestore) {
            getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(51.503186, -0.126446), 10));
        }*/

        /*mClusterManager = new ClusterManager<>(this, getMap());
        getMap().setOnCameraIdleListener(mClusterManager);

        // Add a custom InfoWindowAdapter by setting it to the MarkerManager.Collection object from
        // ClusterManager rather than from GoogleMap.setInfoWindowAdapter
        mClusterManager.getMarkerCollection().setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(@NonNull Marker marker) {
                final LayoutInflater inflater = LayoutInflater.from(ClusteringDemoActivity.this);
                final View view = inflater.inflate(R.layout.custom_info_window, null);
                final TextView textView = view.findViewById(R.id.textViewTitle);
                String text = (marker.getTitle() != null) ? marker.getTitle() : "Cluster Item";
                textView.setText(text);
                return view;
            }

            @Override
            public View getInfoContents(@NonNull Marker marker) {
                return null;
            }
        });
        mClusterManager.setOnClusterItemInfoWindowLongClickListener(marker ->
                Toast.makeText(ClusteringDemoActivity.this,
                        "Info window clicked.",
                        Toast.LENGTH_SHORT).show());
        mClusterManager.setOnClusterItemInfoWindowLongClickListener(marker ->
                Toast.makeText(ClusteringDemoActivity.this,
                        "Info window long pressed.",
                        Toast.LENGTH_SHORT).show());

        try {
            readItems();
        } catch (JSONException e) {
            Toast.makeText(this, "Problem reading list of markers.", Toast.LENGTH_LONG).show();
        }*/
    //}

    @Override
    public void onMapReady(GoogleMap googleMap) {
        GoogleMap map = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng fdi = new LatLng(40.452757315351604, -3.733414742431177);
        map.addMarker(new MarkerOptions()
                .position(fdi)
                .title("Marker in FDI"));
        map.setMinZoomPreference(15);
        map.moveCamera(CameraUpdateFactory.newLatLng(fdi));
    }
}
