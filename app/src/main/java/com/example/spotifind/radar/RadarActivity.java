package com.example.spotifind.radar;



import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.util.Pair;
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
import com.firebase.geofire.LocationCallback;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class RadarActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener, GoogleMap.OnCameraMoveListener, GoogleMap.OnInfoWindowClickListener{
    //private BottomNavigationView menuBar;
    private BottomNavigationView navBar;
    private GoogleMap mMap;
    private LocationManager locationManager;
    private LatLng lastKnownLatLng;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private double minLatitude, maxLatitude;
    private double minLongitude, maxLongitude;

    private List<Pair<LatLng, String>> nearUsers;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private String userid;

    private Marker userMarker;

    private HashMap<String, Marker> marcadoresUsuarios;

    private AtomicInteger usersToProcess;

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
                        startLocationUpdates();
                    }
                }
            });

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_radar);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        userid = getIntent().getStringExtra("user_id");
        usersToProcess = new AtomicInteger(0);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        nearUsers = new ArrayList<>();
        marcadoresUsuarios = new HashMap<>();
        getUserLocation(userid,new LocationListener() {
            private double maxLongitude = 100;
            private double maxLatitude = 100;
            private double minLongitude = -100;
            private double minLatitude= -100;


            @Override
            public void onLocationReceived(LatLng location) {
            }

            @Override
            public void onLocationChanged(Location location) {
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {}

            @Override
            public void onProviderEnabled(String provider) {}

            @Override
            public void onProviderDisabled(String provider) {}
        });

        // Comprobar permisos
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
            // Verificar si la localización está activada
            boolean isLocationEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (!isLocationEnabled) {
                showLocationSettingsDialog();
            }

            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);

            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

            // Request location updates
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, this);
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

        // Comprobar permisos
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // El permiso no está concedido
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
        } else {
            // El permiso ya ha sido concedido
            startLocationUpdates();
        }

        // Establecer un OnCameraMoveListener para detectar movimientos en el mapa
        mMap.setOnCameraMoveListener(this);

        // Establecer un OnMarkerClickListener para mostrar el título del marcador con el nombre de la canción
        mMap.setOnMarkerClickListener(marker -> {
            String userId = (String) marker.getTag(); // Obtener el ID del usuario desde el tag del marcador
            if (userId != null) {
                // Obtener la información del usuario desde Firebase usando el ID del usuario
                DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
                userRef.addValueEventListener(new ValueEventListener() { // Cambiado a addListenerForSingleValueEvent
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        LocalUser user =  dataSnapshot.getValue(LocalUser.class);
                        if (user != null) {
                            // Mostrar la canción del usuario como título del marcador
                            String userSong = user.getLastPlayedSong().name;
                            mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter(RadarActivity.this, user));
                            marker.showInfoWindow(); // Mostrar la ventana de información personalizada

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e("LocalUser", "Error al obtener información de usuario", databaseError.toException());
                    }
                });
            }
            else {
                Log.e("LocalUser", "El ID del usuario es nulo");
            }
            return false; // Esto permite que el evento de clic también se propague al mapa
        });
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
        // Actualiza la última ubicación conocida y mueve la cámara hacia ella
        lastKnownLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        setLastKnownLatLng(lastKnownLatLng);
        Toast.makeText(this, "Ubicación cambiada", Toast.LENGTH_SHORT).show();
        this.minLatitude = location.getLatitude() - 0.5; this.maxLatitude = location.getLatitude() + 0.5;
        this.minLongitude = location.getLongitude() - 0.5; this.maxLongitude = location.getLongitude() + 0.5;
        // Mover la cámara hacia la nueva ubicación
        if (mMap != null) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lastKnownLatLng, 15));
            getNearUsers(3000);
        }

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

    private void addMarkers(List<Pair<LatLng, String>> nearUsers) {
        // Limpia todos los marcadores antiguos del mapa
        mMap.clear();
        AtomicInteger usersProcessed = new AtomicInteger(0);
        AtomicReference<Double> nearestUserDistance = new AtomicReference<>(Double.MAX_VALUE);
        AtomicReference<LatLng> nearestUserLatLng = new AtomicReference<>(null);

        for (Pair<LatLng, String> userPair : nearUsers) {

            if (!userPair.second.equals(userid)) {
                LatLng userLatLng = userPair.first;
                String userId = userPair.second;

                // Eliminar el marcador anterior del usuario, si existe
                if (marcadoresUsuarios.containsKey(userId)) {
                    Marker marcadorAnterior = marcadoresUsuarios.get(userId);
                    if (marcadorAnterior != null) {
                        marcadorAnterior.remove();
                    }
                    marcadoresUsuarios.remove(userId);
                }

                // Agregar el nuevo marcador y almacenarlo en el HashMap
                Marker marker = mMap.addMarker(new MarkerOptions()
                        .position(userLatLng)
                        .visible(true)); // No se establece el título, pero el marcador es visible
                marker.setTag(userId); // Asociar el ID del usuario con el marcador usando setTag()
                marcadoresUsuarios.put(userId, marker);

                // Actualiza la ubicación del usuario más cercano
                float[] results = new float[1];
                Location.distanceBetween(mMap.getCameraPosition().target.latitude, mMap.getCameraPosition().target.longitude,
                        userLatLng.latitude, userLatLng.longitude, results);
                if (results[0] < nearestUserDistance.get()) {
                    nearestUserLatLng.set(userLatLng);
                    nearestUserDistance.set((double) results[0]);
                }
            }

            // Verifica si se han procesado todos los usuarios
            if (usersProcessed.incrementAndGet() == nearUsers.size()) {
                // Mueve la cámara a la ubicación del usuario más cercano
                if (nearestUserLatLng.get() != null) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(nearestUserLatLng.get(), 15));
                }
            }
        }
    }


    public void getNearUsers(double radiusInMeters) {
        DatabaseReference userLocationsRef = FirebaseDatabase.getInstance().getReference("userLocations");
        GeoFire geoFire = new GeoFire(userLocationsRef);
        getUserLocation(userid, new LocationListener() {
            @Override
            public void onLocationReceived(LatLng location) {
                // Obtener la última ubicación conocida y realizar la búsqueda de usuarios cercanos
                LatLng lastKnownLatLng = location;
                GeoQuery geoQuery = geoFire.queryAtLocation(
                        new GeoLocation(lastKnownLatLng.latitude, lastKnownLatLng.longitude),
                        radiusInMeters / 1000 // Convertir metros a kilómetros
                );
                Log.i("getNearUsers", "Realizando consulta con radio: " + radiusInMeters);
                geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
                    @Override
                    public void onKeyEntered(String key, GeoLocation location) {
                        // key es el uid del usuario cercano
                        Log.i("getNearUsers", "Usuario cercano encontrado con clave: " + key);
                        usersToProcess.incrementAndGet();
                        // Obtén la información adicional del usuario desde el nodo "users" y añádelo a nearUsers
                        getUserInfoAndAddToNearUsers(key);
                    }

                    @Override
                    public void onKeyExited(String key) {
                        // Un usuario salió del área de búsqueda
                        Log.i("getNearUsers", "Usuario salió del área de búsqueda: " + key);
                        removeUserFromNearUsers(key);
                    }

                    @Override
                    public void onKeyMoved(String key, GeoLocation location) {
                        // La ubicación de un usuario cercano se actualizó
                        Log.i("getNearUsers", "Usuario movido con clave: " + key);
                        updateUserLocationInNearUsers(key, lastKnownLatLng);
                    }

                    @Override
                    public void onGeoQueryReady() {
                        Log.i("getNearUsers", "Consulta Geo finalizada");
                        if (usersToProcess.get() == 0) {
                            // Llamar a addMarkers sólo cuando se hayan cargado todos los usuarios en nearUsers
                            // Cargar el mapa sólo después de haber llamado a addMarkers
                            Log.i("getNearUsers", "Mapa cargado");
                        }
                    }

                    @Override
                    public void onGeoQueryError(DatabaseError error) {
                        Log.e("getNearUsers", "Error en la consulta geoespacial", error.toException());
                    }
                });
            }

            @Override
            public void onLocationChanged(Location location) {}

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {}

            @Override
            public void onProviderEnabled(String provider) {}

            @Override
            public void onProviderDisabled(String provider) {}
        });
    }
    private void getUserInfoAndAddToNearUsers(String uid) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("userLocations");
        DatabaseReference userLocationRef = usersRef.child(uid).child("l");

        userLocationRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Double lat = dataSnapshot.child("0").getValue(Double.class);
                    Double lon = dataSnapshot.child("1").getValue(Double.class);

                    LatLng userLatLng = new LatLng(lat, lon);

                    // Agregar el par ID de usuario y LatLng a la lista nearUsers
                    nearUsers.add(new Pair<>(userLatLng,uid));

                    // Decrementar el contador de remainingUsers
                    int remaining = usersToProcess.decrementAndGet();
                    if (remaining == 0) {
                        // Llamar a addMarkers sólo cuando se hayan cargado todos los usuarios en nearUsers
                        addMarkers(nearUsers);
                        // Cargar el mapa sólo después de haber llamado a addMarkers
                        mMap.setOnMapLoadedCallback(() -> Log.i("getNearUsers", "Mapa cargado"));
                    }
                } else {
                    Log.e("LocalUser", "El usuario no existe en la base de datos");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("LocalUser", "Error al obtener la ubicación del usuario", databaseError.toException());
            }
        });
    }private void removeUserFromNearUsers(String key) {
        Iterator<Pair<LatLng, String>> iterator = nearUsers.iterator();
        while (iterator.hasNext()) {
            Pair<LatLng,String> userPair = iterator.next();
            String userId = userPair.second;
            if (userId.equals(key)) {
                iterator.remove();
                return;
            }
        }
    }private void updateUserLocationInNearUsers(String key, LatLng location) {
        for (Pair<LatLng, String> userPair : nearUsers) {
            String userId = userPair.second;
            if (userId.equals(key)) {
                LatLng newLocation = new LatLng(location.latitude, location.longitude);
                int index = nearUsers.indexOf(userPair);
                nearUsers.set(index, new Pair<>(newLocation,userId));
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
    public interface LocationListener {
        void onLocationChanged(Location location);

        void onLocationReceived(LatLng location);

        // Implementar los otros métodos de LocationListener si es necesario
        void onStatusChanged(String provider, int status, Bundle extras);

        void onProviderEnabled(String provider);

        void onProviderDisabled(String provider);
    }
    private void getUserLocation(String userId, final LocationListener listener) {
        DatabaseReference userLocationRef = FirebaseDatabase.getInstance().getReference("userLocations");
        GeoFire geoFire = new GeoFire(userLocationRef);

        geoFire.getLocation(userId, new LocationCallback() {
            @Override
            public void onLocationResult(String key, GeoLocation location) {
                if (location != null) {
                    LatLng latLng = new LatLng(location.latitude, location.longitude);
                    listener.onLocationReceived(latLng);
                } else {
                    Log.e("RadarActivity", "No se pudo obtener la ubicación del usuario");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("RadarActivity", "Error al obtener la ubicación del usuario", databaseError.toException());
            }
        });
    }
    public void setLastKnownLatLng(LatLng latLng) {

            DatabaseReference userLocationsRef = FirebaseDatabase.getInstance().getReference("userLocations");
            GeoFire geoFire = new GeoFire(userLocationsRef);

            // Guardar la ubicación en el formato que GeoFire espera
            geoFire.setLocation(userid, new GeoLocation(latLng.latitude, latLng.longitude), new GeoFire.CompletionListener() {
                @Override
                public void onComplete(String key, DatabaseError error) {
                    if (error != null) {
                        Log.e("RadarActivity", "Error al guardar la ubicación en Firebase", error.toException());
                    } else {
                        Log.i("RadarActivity", "Ubicación del usuario guardada en Firebase");
                    }
                }
            });

        this.lastKnownLatLng = latLng;
    }

    private void showLocationSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Localización desactivada");
        builder.setMessage("Por favor, activa la localización para usar esta función.");
        builder.setPositiveButton("Configuración de localización", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }
}
