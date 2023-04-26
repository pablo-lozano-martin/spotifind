package com.example.spotifind.radar;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.example.spotifind.LocalUser;

import android.Manifest;

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
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.spotify.protocol.types.Track;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class RadarActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener, GoogleMap.OnCameraMoveListener, GoogleMap.OnInfoWindowClickListener {
    //private BottomNavigationView menuBar;
    private BottomNavigationView navBar;
    private GoogleMap mMap;
    private LocationManager locationManager;
    private LatLng lastKnownLatLng;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private List<Pair<LatLng, String>> nearUsers;
    private HashMap<String, Marker> marcadoresUsuarios;

    private String userid;

    private AtomicInteger usersToProcess;

    private CustomInfoWindowAdapter customInfoWindowAdapter;
    private boolean fijarCamara, artificialCamMove;

    private Map<String, CustomInfoWindowAdapter> infoWindowAdapterCache = new HashMap<>();
    SharedPreferences sharedPref;

    private Context mContext;
    private FloatingActionButton centrarFAB;

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

    private void initializeMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Solicitar actualizaciones de ubicación
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // para manejar el caso en que el usuario conceda el permiso. Consulte la documentación
            // para ActivityCompat#requestPermissions para obtener más detalles.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 3, this);
    }



    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_radar);
        sharedPref = getSharedPreferences("preferencias", MODE_PRIVATE);
        usersToProcess = new AtomicInteger(0);
        nearUsers = new ArrayList<>();
        marcadoresUsuarios = new HashMap<>();
        this.fijarCamara = true;
        this.artificialCamMove = false;
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        getUserLocation(sharedPref.getString("user_id", ""), new LocationListener() {
            private double maxLongitude = 100;
            private double maxLatitude = 100;
            private double minLongitude = -100;
            private double minLatitude = -100;

            @Override
            public void onLocationReceived(LatLng location) {
            }

            @Override
            public void onLocationChanged(Location location) {
                lastKnownLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                setLastKnownLatLng(lastKnownLatLng);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
            }
        });

        // Comprobar permisos
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            boolean isLocationEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (!isLocationEnabled) {
                showLocationSettingsDialog();
            }

            initializeMap();
        }

        centrarFAB = findViewById(R.id.fab_center_location);

        // Agregar el listener al botón
        centrarFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                artificialCamMove = true;
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastKnownLatLng, 15));
                artificialCamMove = false;
                fijarCamara = true;
            }
        });
        centrarFAB.setEnabled(false);

        navBar = findViewById(R.id.navbar);
        navBar.setSelectedItemId(R.id.radar);
        NavigationBarListener navigationBarListener = new NavigationBarListener(this, sharedPref.getString("user_id", ""));
        navBar.setOnNavigationItemSelectedListener(navigationBarListener);
    }

    // Manejar la respuesta de la solicitud de permisos
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso concedido
                Toast.makeText(this, "Permiso concedido", Toast.LENGTH_SHORT).show();
                // Hacer algo con la ubicación
                startLocationUpdates();
            } else {
                // Permiso denegado
                Toast.makeText(this, "Permiso denegado", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Comprobar permisos
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // El permiso no está concedido
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
        } else {
            // El permiso ya ha sido concedido
            startLocationUpdates();
        }

        // Establecer un OnCameraMoveListener para detectar movimientos en
        mMap.setOnCameraMoveListener(this);
        mMap.setOnMarkerClickListener(marker -> {
            String userId = (String) marker.getTag();
            DatabaseReference lastPlayedSongRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("lastPlayedSong");
            lastPlayedSongRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Track track = dataSnapshot.getValue(Track.class);
                    if (userId != null) {
                        if (customInfoWindowAdapter != null && customInfoWindowAdapter.isAdded()) {
                            customInfoWindowAdapter.dismiss();
                        }
                        customInfoWindowAdapter = new CustomInfoWindowAdapter(userId, marker);
                        customInfoWindowAdapter.updateData(track);
                        customInfoWindowAdapter.show(getSupportFragmentManager(), "customInfoWindow");
                        if (customInfoWindowAdapter.isClosed()) {
                            lastPlayedSongRef.removeEventListener(this);
                        }
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("LocalUser", "Error al obtener información de usuario", databaseError.toException());
                }
            });
            return false;
        });
    }

    // Iniciar actualizaciones de ubicación
    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    3000, 3, this);
            if (lastKnownLatLng != null)
                lastKnownLatLng = new LatLng(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLatitude(), locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLongitude());
        } else {
            Toast.makeText(this, "No hay permisos de ubicación...", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        // Actualiza la última ubicación conocida y mueve la cámara hacia ella
        lastKnownLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        setLastKnownLatLng(lastKnownLatLng);

        // Mover la cámara hacia la nueva ubicación
        if (mMap != null) {
            if(fijarCamara) {
                artificialCamMove = true;
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastKnownLatLng, 15));
                artificialCamMove = false;
                fijarCamara = true;
                Log.e("fijarCamara", "true");
            }

            getNearUsers(20000);
        }
    }

    @Override
    public void onProviderEnabled(String provider) {}

    @Override
    public void onProviderDisabled(String provider) {}

    @Override
    public void onCameraMove() {
        if (!artificialCamMove) {
            fijarCamara = false;
            Toast.makeText(this, "Moviendo cámara con los dedos", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Moviendo cámara artificialmente", Toast.LENGTH_SHORT).show();
        }
    }
    private void addMarkers(List<Pair<LatLng, String>> nearUsers) {
        // Limpia todos los marcadores antiguos del mapa
        mMap.clear();
        AtomicInteger usersProcessed = new AtomicInteger(0);
        AtomicReference<Double> nearestUserDistance = new AtomicReference<>(Double.MAX_VALUE);
        AtomicReference<LatLng> nearestUserLatLng = new AtomicReference<>(null);

        for (Pair<LatLng, String> userPair : nearUsers) {

            //if (!userPair.second.equals(sharedPref.getString("user_id", ""))) {
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
                    .visible(true));
            marker.setTag(userId);
            marcadoresUsuarios.put(userId, marker);
            /*DatabaseReference lastPlayedSongRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("lastPlayedSong");
            ImageView songImage = new ImageView(this);
            lastPlayedSongRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Track track = dataSnapshot.getValue(Track.class);
                    String rawImageUri = track.imageUri.raw;
                    String imageUrl = "https://i.scdn.co/image/" + rawImageUri.replace("spotify:image:", "");
                    Picasso picasso = Picasso.get();
                    picasso.load(imageUrl).fetch();
                    picasso.load(imageUrl).priority(Picasso.Priority.HIGH).into(songImage);
                    songImage.setDrawingCacheEnabled(true);
                    songImage.buildDrawingCache();
                    Bitmap bitmap = songImage.getDrawingCache();
                    Marker marker = mMap.addMarker(new MarkerOptions()
                            .position(userLatLng)
                            .icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                            .visible(true));
                    marker.setTag(userId);
                    marcadoresUsuarios.put(userId, marker);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("LocalUser", "Error al obtener información de usuario", databaseError.toException());
                }
            });*/

            // Actualiza la ubicación del usuario más cercano
                float[] results = new float[1];
                Location.distanceBetween(mMap.getCameraPosition().target.latitude, mMap.getCameraPosition().target.longitude,
                        userLatLng.latitude, userLatLng.longitude, results);
                if (results[0] < nearestUserDistance.get()) {
                    nearestUserLatLng.set(userLatLng);
                    nearestUserDistance.set((double) results[0]);
                }
          // }

            // Verifica si se han procesado todos los usuarios
            if (usersProcessed.incrementAndGet() == nearUsers.size()) {
                // Mueve la cámara a la ubicación del usuario más cercano
                if (nearestUserLatLng.get() != null && fijarCamara) {
                    artificialCamMove = true;
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(nearestUserLatLng.get(), 15));
                    artificialCamMove = false;
                    fijarCamara = true;
                }
            }
        }

        centrarFAB.setEnabled(true);
    }


    public void getNearUsers(double radiusInMeters) {
        DatabaseReference userLocationsRef = FirebaseDatabase.getInstance().getReference("userLocations");
        GeoFire geoFire = new GeoFire(userLocationsRef);
        getUserLocation(this.sharedPref.getString("user_id", ""), new LocationListener() {
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
            geoFire.setLocation(sharedPref.getString("user_id", ""), new GeoLocation(latLng.latitude, latLng.longitude), new GeoFire.CompletionListener() {
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

    @Override
    public void onBackPressed() {
        // No hacer nada para evitar que el botón de retroceso sea utilizado
    }

}
