package com.example.spotifind;

import static com.spotify.sdk.android.auth.LoginActivity.REQUEST_CODE;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.example.spotifind.Autentication.LoginActivity;
import com.example.spotifind.firebase.FirebaseService;
import com.example.spotifind.databinding.ActivityMainBinding;
import com.example.spotifind.radar.RadarActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;

import com.spotify.protocol.types.Track;

import com.spotify.sdk.android.auth.AuthorizationClient;
import com.spotify.sdk.android.auth.AuthorizationRequest;
import com.spotify.sdk.android.auth.AuthorizationResponse;

import java.util.concurrent.CompletableFuture;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    private FirebaseAuth mAuth;

    private FirebaseService firebaseService;

    private static final String CLIENT_ID = "824f2fd7d9d14c38a7945ba2f7bb9c60";
    private static final String SECRET_CLIENT_ID = "3d1d551e6f0b4bd8b98aedf17d75f426";
    private static final String REDIRECT_URI = "com.example.spotifind://callback";
    private SpotifyAppRemote mSpotifyAppRemote;
    public static String mAccessToken;

    private LocalUser localUser;
    private CompletableFuture<Void> futureUpdate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        firebaseService= new FirebaseService();
        getSupportActionBar().hide();
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(R.layout.activity_main);
    }

    public void onStart() {
        super.onStart();
        mAuth = FirebaseAuth.getInstance();
        firebaseService= new FirebaseService();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            currentUser.reload();
            autenticarUsuario();
        } else {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        }
    }

    private void showSplashScreen(LocalUser localUser) {
        setContentView(R.layout.splash_screen);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(MainActivity.this, RadarActivity.class);
                intent.putExtra("user_id", localUser.getUid());
                startActivity(intent);
                finish();
            }
        }, 1000); // muestra la splash screen por 5 segundos antes de cargar la actividad principal
        /*binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(R.layout.activity_main);
        replaceFragment(new RadarFragment()); //Comienza con el fragment del radar al iniciar la app
        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()){
                case R.id.friendlist:
                    replaceFragment(new FriendlistFragment());
                    break;
                case R.id.radar:
                    replaceFragment(new RadarFragment());
                    break;
                case R.id.profile:
                    replaceFragment(new ProfileFragment());
                    break;
            }
            return true;
        });*/
    }



    private void autenticarUsuario() {
        // Se crea una instancia de AuthorizationRequest con los datos de nuestra aplicación y la URI de redireccionamiento
        AuthorizationRequest.Builder constructor = new AuthorizationRequest.Builder(
                CLIENT_ID,
                AuthorizationResponse.Type.TOKEN,
                REDIRECT_URI
        );
        constructor.setScopes(new String[]{"user-read-private", "playlist-read", "user-library-read", "user-read-currently-playing", "user-read-playback-state", "user-modify-playback-state","user-top-read"});
        AuthorizationRequest solicitud = constructor.build();

        // Se llama al método AuthorizationClient.openLoginActivity() para iniciar la actividad de autenticación de Spotify
        AuthorizationClient.openLoginActivity(this, REQUEST_CODE, solicitud);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        // Verifica si el resultado proviene de la actividad correcta
        if (requestCode == REQUEST_CODE) {
            AuthorizationResponse respuesta = AuthorizationClient.getResponse(resultCode, intent);

            switch (respuesta.getType()) {
                // La respuesta fue exitosa y contiene el token de autenticación
                case TOKEN:
                    mAccessToken = respuesta.getAccessToken();
                    Log.d("MainActivity", "token:" + mAccessToken);
                    // Se crea una instancia de ConnectionParams con el token de acceso y la URI de redireccionamiento
                    ConnectionParams parametrosConexion = new ConnectionParams.Builder(CLIENT_ID)
                            .setRedirectUri(REDIRECT_URI)
                            .build();

                    // Se conecta con la API de Spotify mediante la clase SpotifyAppRemote y se almacena en una variable
                    SpotifyAppRemote.connect(this, parametrosConexion, new Connector.ConnectionListener() {

                        @Override
                        public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                            mSpotifyAppRemote = spotifyAppRemote;
                            Log.d("MainActivity", "¡Conectado! ¡Genial!");
                            mAccessToken = respuesta.getAccessToken();
                            Log.d("Token:", mAccessToken);
                            initializeLocalUser();
                            firebaseService.startRealtimeUpdates(localUser.getUid());
                            // Se suscribe al evento PlayerState para obtener información sobre el estado del reproductor de Spotify
                            mSpotifyAppRemote.getPlayerApi().subscribeToPlayerState().setEventCallback(playerState -> {
                                final Track track = playerState.track;
                                if (track != null) {
                                    //binding.currentSongTitle.setText(track.name);
                                    //binding.currentSongArtist.setText(track.artist.name);
                                }
                            });
                        }

                        @Override
                        public void onFailure(Throwable throwable) {
                            Log.e("MainActivity", throwable.getMessage(), throwable);
                        }
                    });
                    break;

                // El flujo de autenticación devolvió un error
                case ERROR:
                    Log.e("MainActivity:", "error:"+Log.ERROR);
                    break;

                // Lo más probable es que el flujo de autenticación se haya cancelado
                default:
            }
        }
    }



    private void replaceFragment(Fragment fragment){ //Intercambia el fragment actual por otro
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }

    @Override
    public void onBackPressed() {
        // No se permite volver atrás en la actividad principal
    }

    private void initializeLocalUser() {
        localUser = new LocalUser(this,mAuth);
        localUser.setSpoifyAppRemote(mSpotifyAppRemote);
        showSplashScreen(localUser);
        // Llama a los métodos que requieren el token de acceso para inicializar los datos del usuario
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAccessToken = null;
        FirebaseAuth.getInstance().signOut();
    }

}