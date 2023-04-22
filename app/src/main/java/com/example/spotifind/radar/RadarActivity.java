package com.example.spotifind.radar;



import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.example.spotifind.LocalUser;
import com.example.spotifind.NavigationBarListener;
import com.example.spotifind.R;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RadarActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener, GoogleMap.OnCameraMoveListener, GoogleMap.OnInfoWindowClickListener{
    //private BottomNavigationView menuBar;
    private BottomNavigationView navBar;
    private GoogleMap mMap;
    private LocationManager locationManager;
    private LatLng lastKnownLatLng;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private double minLatitude, maxLatitude;
    private double minLongitude, maxLongitude;

    private List<LocalUser> nearUsers;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private String userid;

    private ActivityResultLauncher<String[]> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
                @Override
                public void onActivityResult(Map<String, Boolean> result) {
                    if (result.containsValue(false)) {
                        // Permiso denegado
                        Toast.makeText(RadarActivity.this, "Permiso denegado", Toast.LENGTH_SHORT).show();
                    } else {
                        // Permiso concedido
                        Toast.makeText(RadarActivity.this, "Permiso concedido", Toast.LENGTH_SHORT).show();
                        // Hacer algo con la ubicación
                        startLocationUpdates();
                    }
                }
            });

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_radar);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        nearUsers = new ArrayList<>();

        // Comprobar permisos
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // El permiso no está concedido
            requestPermissionLauncher.launch(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION});
        } else {
            // El permiso ya está concedido
            // Hacer algo con la ubicación
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        }

        navBar = findViewById(R.id.navbar);
        navBar.setSelectedItemId(R.id.radar);
        NavigationBarListener navigationBarListener = new NavigationBarListener(this,this.userid);
        navBar.setOnNavigationItemSelectedListener(navigationBarListener);
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
        getNearUsers(1000);
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
        saveUserLocation(lastKnownLatLng);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastKnownLatLng, 8f));
        Toast.makeText(this, "Location changed", Toast.LENGTH_SHORT).show();

        this.minLatitude = location.getLatitude() - 0.5; this.maxLatitude = location.getLatitude() + 0.5;
        this.minLongitude = location.getLongitude() - 0.5; this.maxLongitude = location.getLongitude() + 0.5;
        MarkerOptions markerOptions = new MarkerOptions().position(new LatLng(this.minLatitude, location.getLongitude()));
        mMap.addMarker(markerOptions);
        markerOptions.position(new LatLng(this.maxLatitude, location.getLongitude()));
        mMap.addMarker(markerOptions);
        markerOptions.position(new LatLng(location.getLatitude(), this.minLongitude));
        mMap.addMarker(markerOptions);
        markerOptions.position(new LatLng(location.getLatitude(), this.maxLongitude));
        mMap.addMarker(markerOptions);
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

    private void addMarkers(List<LocalUser> nearUsers) {
        for (LocalUser user : nearUsers) {
            LatLng userLatLng = new LatLng(user.getLocation().getLatitude(), user.getLocation().getLongitude()
            );
            if(userLatLng!=null) {
                Marker marker = mMap.addMarker(new MarkerOptions()
                        .position(userLatLng)
                        .title(user.getUsername()));
                marker.setTag(user);
            }
        }
    }
    public void getNearUsers(double radiusInMeters) {
        DatabaseReference userLocationsRef = FirebaseDatabase.getInstance().getReference("userLocations");
        GeoFire geoFire = new GeoFire(userLocationsRef);

        if(lastKnownLatLng!=null) {
            GeoQuery geoQuery = geoFire.queryAtLocation(
                    new GeoLocation(lastKnownLatLng.latitude, lastKnownLatLng.longitude),
                    radiusInMeters / 1000 // Convertir metros a kilómetros
            );
            geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
                @Override
                public void onKeyEntered(String key, GeoLocation location) {
                    // key es el uid del usuario cercano
                    // Obtén la información adicional del usuario desde el nodo "users" y añádelo a nearUsers
                    getUserInfoAndAddToNearUsers(key);

                }

                @Override
                public void onKeyExited(String key) {
                    // Un usuario salió del área de búsqueda
                    removeUserFromNearUsers(key);

                }

                @Override
                public void onKeyMoved(String key, GeoLocation location) {
                    //La ubicación de un usuario cercano se actualizó
                    updateUserLocationInNearUsers(key, lastKnownLatLng);

                }

                @Override
                public void onGeoQueryReady() {
                    addMarkers(nearUsers);
                }

                @Override
                public void onGeoQueryError(DatabaseError error) {
                    Log.e("LocalUser", "Error en la consulta geoespacial", error.toException());
                }
            });
        }
    }

    private void getUserInfoAndAddToNearUsers(String uid) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        usersRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                LocalUser user = dataSnapshot.getValue(LocalUser.class);
                if (user != null) {
                    // Agrega el usuario a la lista de usuarios cercanos
                    nearUsers.add(user);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("LocalUser", "Error al obtener información de usuario", databaseError.toException());
            }
        });
    }


    private void removeUserFromNearUsers(String key) {
        for (LocalUser user : nearUsers) {
            if (user.getUid().equals(key)) {
                nearUsers.remove(user);
                return;
            }
        }
    }

    private void updateUserLocationInNearUsers(String key, LatLng location) {
        for (LocalUser user : nearUsers) {
            if (user.getUid().equals(key)) {
                Location _location = new Location("");
                _location.setLatitude(location.latitude);
                _location.setLongitude(location.longitude);
                user.setLocation(_location);
                return;
            }
        }
    }


    @Override
    public void onInfoWindowClick(Marker marker) {
        LocalUser localUser = (LocalUser) marker.getTag();
        if (localUser != null) {
            String song = localUser.getLastPlayedSong().toString();
            Toast.makeText(this, "Canción escuchada: " + song, Toast.LENGTH_SHORT).show();
        }
    }

    private void saveUserLocation(LatLng latLng) {
        if (currentUser != null) {
            String userId = currentUser.getUid(); // Obtén el ID del usuario actual
            DatabaseReference userLocationsRef = FirebaseDatabase.getInstance().getReference("userLocations");
            Map<String, Object> locationData = new HashMap<>();
            locationData.put("latitude", latLng.latitude);
            locationData.put("longitude", latLng.longitude);
            userLocationsRef.child(userId).child("location").setValue(locationData);
            Log.i("RadarActivity", "Ubicación del usuario guardada");
        } else {
            // El usuario no está autenticado, maneja el caso de error aquí
            Log.e("RadarActivity", "Usuario no autenticado");
        }
    }

}
