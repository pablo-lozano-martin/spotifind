package com.example.spotifind;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import org.json.JSONException;

import java.util.Map;

public class RadarActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener, GoogleMap.OnCameraMoveListener {
    private BottomNavigationView menuBar;
    private GoogleMap mMap;
    private LocationManager locationManager;
    private LatLng lastKnownLatLng;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private ActivityResultLauncher<String[]> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
                @Override
                public void onActivityResult(Map<String, Boolean> result) {
                    if (result.containsValue(false)) {
                        // Permission denied
                        Toast.makeText(RadarActivity.this, "Permission denied", Toast.LENGTH_SHORT).show();
                    } else {
                        // Permission granted
                        Toast.makeText(RadarActivity.this, "Permission granted", Toast.LENGTH_SHORT).show();
                        // Do something with the location
                    }
                }
            });
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getSupportActionBar().hide();
        setContentView(R.layout.activity_radar);

        // Check for permission
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            // Permission has already been granted
            // Do something with the location
            // Get the LocationManager
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);

            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        }

        // Alternatively, you can use the following code to request permission:
        /*
        requestPermissionLauncher.launch(new String[]{Manifest.permission.ACCESS_FINE_LOCATION});
         */

        menuBar = findViewById(R.id.bottomNavigationView);
        menuBar.setSelectedItemId(R.id.radar);
        menuBar.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.friendlist:
                        /*Intent intent = new Intent(RadarActivity.this, FriendListActivity.class);
                        startActivity(intent);
                        finish();*/
                        Log.d("RadarActivity", "FrindList selected");
                        break;
                    case R.id.profile:
                        /*Intent intent = new Intent(RadarActivity.this, ProfileActivity.class);
                        startActivity(intent);
                        finish();*/Log.d("RadarActivity", "Profile selected");
                        break;
                    default:
                        Log.d("RadarActivity", "Radar selected");
                        break;
                }
                return true;
            }
        });
    }

    // Handle the permission request response
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
                // Do something with the location
                startLocationUpdates();
            } else {
                // Permission denied
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        /*GoogleMap map = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng fdi = new LatLng(40.452757315351604, -3.733414742431177);
        map.addMarker(new MarkerOptions()
                .position(fdi)
                .title("Marker in FDI"));
        map.setMinZoomPreference(15);
        map.moveCamera(CameraUpdateFactory.newLatLng(fdi));*/

        mMap = googleMap;

        // Check for permission
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
        } else {
            // Permission has already been granted
            startLocationUpdates();
        }

        // Set an OnCameraMoveListener to detect map movements
        mMap.setOnCameraMoveListener(this);
    }

    // Start location updates
    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    5000, 10, this);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        // Update the last known location and move the camera to it
        lastKnownLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastKnownLatLng, 16f));
        Toast.makeText(this, "Location changed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}

    @Override
    public void onProviderEnabled(String provider) {}

    @Override
    public void onProviderDisabled(String provider) {}

    @Override
    public void onCameraMove() {
        // Update the last known location when the map moves
        lastKnownLatLng = mMap.getCameraPosition().target;
        Toast.makeText(this, "Moving cámera", Toast.LENGTH_SHORT).show();
    }
}
