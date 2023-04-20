import static com.spotify.sdk.android.auth.LoginActivity.REQUEST_CODE;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.example.spotifind.FirebaseService;
import com.example.spotifind.FriendlistFragment;
import com.example.spotifind.LocalUser;
import com.example.spotifind.ProfileFragment;
import com.example.spotifind.R;
import com.example.spotifind.RadarFragment;
import com.example.spotifind.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;

import com.spotify.protocol.client.Subscription;
import com.spotify.protocol.types.PlayerState;
import com.spotify.protocol.types.Track;

import com.spotify.sdk.android.auth.AuthorizationClient;
import com.spotify.sdk.android.auth.AuthorizationRequest;
import com.spotify.sdk.android.auth.AuthorizationResponse;

import java.util.concurrent.atomic.AtomicReference;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    private FirebaseAuth mAuth;

    private FirebaseService firebaseService;

    private static final String CLIENT_ID = "824f2fd7d9d14c38a7945ba2f7bb9c60";
    private static final String SECRET_CLIENT_ID = "3d1d551e6f0b4bd8b98aedf17d75f426";
    private static final String REDIRECT_URI = "com.example.spotifind://callback";
    private SpotifyAppRemote mSpotifyAppRemote;
    private String mAccessToken;

    public LocalUser localUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot()); // Se debe llamar al método getRoot() de ActivityMainBinding en lugar de pasarlo como parámetro a setContentView()

        mAuth = FirebaseAuth.getInstance();
        firebaseService = new FirebaseService();

        // Se instancia la clase LocalUser y se asegura que no sea nula antes de llamar a sus métodos
        localUser = new LocalUser();
        if (mSpotifyAppRemote != null) {
            localUser.setSpoifyAppRemote(mSpotifyAppRemote);
        }
        if (mAuth != null) {
            localUser.setFirebaseCredentials(mAuth);
        }

        // Se llama al método startAuth() para iniciar el proceso de autenticación de Spotify
        authUser();

        //background service which listens to PlayerState when the song change
        if (localUser != null) {
            firebaseService.startRealtimeUpdates(localUser.getUid());
        }

        showSplashScreen();
    }

    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();

        // Se instancia la clase LocalUser y se asegura que no sea nula antes de llamar a sus métodos
        localUser = new LocalUser();
        if (mSpotifyAppRemote != null) {
            localUser.setSpoifyAppRemote(mSpotifyAppRemote);
        }
        if (mAuth != null) {
            localUser.setFirebaseCredentials(mAuth);
        }

        if (currentUser != null) {
            currentUser.reload();

        } else {
            authUser();
            showSplashScreen();
        }

        //background service which listens to PlayerState when the song change
        if (localUser != null) {
            firebaseService.startRealtimeUpdates(localUser.getUid());
        }
    }

    private void authUser() {
            // Se crea una instancia de AuthorizationRequest con los datos de nuestra aplicación y la URI de redireccionamiento
        AuthorizationRequest.Builder builder = new AuthorizationRequest.Builder(
                CLIENT_ID,
                AuthorizationResponse.Type.TOKEN,
                REDIRECT_URI
        );
        builder.setScopes(new String[]{"user-read-private", "playlist-read", "user-library-read", "user-read-currently-playing", "user-read-playback-state", "user-modify-playback-state"});
        AuthorizationRequest request = builder.build();

        // Se llama al método AuthorizationClient.openLoginActivity() para iniciar la actividad de autenticación de Spotify
        AuthorizationClient.openLoginActivity(this, REQUEST_CODE, request);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        // Si el código de resultado es el esperado y la respuesta no es nula, se extrae el token de acceso y se almacena en una variable
        if (requestCode == REQUEST_CODE) {
            AuthorizationResponse response = AuthorizationClient.getResponse(resultCode, intent);
            if (response.getType() == AuthorizationResponse.Type.TOKEN) {
                mAccessToken = response.getAccessToken();

                // Se crea una instancia de ConnectionParams con el token de acceso y la URI de redireccionamiento
                ConnectionParams connectionParams = new ConnectionParams.Builder(CLIENT_ID)
                        .setRedirectUri(REDIRECT_URI)
                        .build();

                // Se conecta con la API de Spotify mediante la clase SpotifyAppRemote y se almacena en una variable
                SpotifyAppRemote.connect(this, connectionParams, new Connector.ConnectionListener() {

                    @Override
                    public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                        mSpotifyAppRemote = spotifyAppRemote;
                        localUser.setSpoifyAppRemote(mSpotifyAppRemote);
                        localUser.setSpotitoken(mAccessToken);
                        Log.d("MainActivity", "Connected! Yay!");

                        // Se suscribe al evento PlayerState para obtener información sobre el estado del reproductor de Spotify
                        mSpotifyAppRemote.getPlayerApi().subscribeToPlayerState().setEventCallback(playerState -> {
                            final Track track = playerState.track;
                            if (track != null) {
                                // Se actualiza la vista con la información del estado del reproductor
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
            }
        }
    }

    private void showSplashScreen() {
        setContentView(R.layout.splash_screen);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

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
        });

        // Oculta la pantalla de Splash Screen cuando se haya cargado la actividad principal

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
}