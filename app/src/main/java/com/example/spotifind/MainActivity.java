package com.example.spotifind;

import static android.content.ContentValues.TAG;
import static com.spotify.sdk.android.auth.AuthorizationResponse.Type.TOKEN;
import static com.spotify.sdk.android.auth.LoginActivity.REQUEST_CODE;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.webkit.CookieManager;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.util.ByteBufferUtil;
import com.example.spotifind.Autentication.LoginActivity;
import com.example.spotifind.firebase.FirebaseService;
import com.example.spotifind.databinding.ActivityMainBinding;
import com.example.spotifind.radar.RadarActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.android.appremote.api.error.CouldNotFindSpotifyApp;
import com.spotify.android.appremote.api.error.NotLoggedInException;
import com.spotify.android.appremote.api.error.UserNotAuthorizedException;
import com.spotify.protocol.types.Track;
import com.spotify.sdk.android.auth.AuthorizationClient;
import com.spotify.sdk.android.auth.AuthorizationRequest;
import com.spotify.sdk.android.auth.AuthorizationResponse;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class MainActivity extends AppCompatActivity {

    private static final String CLIENT_ID = "824f2fd7d9d14c38a7945ba2f7bb9c60";
    private static final String SECRET_CLIENT_ID = "3d1d551e6f0b4bd8b98aedf17d75f426";
    private static final String REDIRECT_URI = "com.example.spotifind://callback";
    private static final int LOGIN_ACTIVITY_REQUEST_CODE = 1;

    private ActivityMainBinding mBinding;
    private FirebaseAuth mAuth;
    public static FirebaseService mFirebaseService;
    private SpotifyAppRemote mSpotifyAppRemote;
    private static String mAccessToken;
    private LocalUser mLocalUser;

    private CompletableFuture<Void> mFutureUpdate;
    private ActivityResultLauncher<Intent> mLoginActivityResultLauncher;
    private boolean mIsCodeExecuted = false;
    private Context mContext;

    private final static ConnectionParams parametrosConexion = new ConnectionParams.Builder(CLIENT_ID)
            .setRedirectUri(REDIRECT_URI)
            .build();


    Connector.ConnectionListener mConnectionListener = new Connector.ConnectionListener() {
        @Override
        public void onConnected(SpotifyAppRemote spotifyAppRemote) {
            // Consultar la última canción reproducida en Firebase
            mSpotifyAppRemote = spotifyAppRemote;
            DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("users").child(obtenerUserId());
            // Suscribirse a los cambios en el estado del reproductor de Spotify
            mSpotifyAppRemote.getPlayerApi().subscribeToPlayerState().setEventCallback(playerState -> {
                final Track track = playerState.track;
                if (track != null) {
                    // Actualizar la última canción reproducida en Firebase
                    databaseRef.child("lastPlayedSong").setValue(track);
                    Log.d("LocalUser", "Última canción reproducida actualizada en Firebase");
                } else {
                    databaseRef.child("lastPlayedSong").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Track lastPlayedSong = snapshot.getValue(Track.class);
                            if (lastPlayedSong == null) {
                                // Reproducir una canción predeterminada en Spotify
                                mSpotifyAppRemote.getPlayerApi().play("spotify:track:6rqhFgbbKwnb9MLmUQDhG6");
                                Log.d("LocalUser", "Reproduciendo una canción predeterminada en Spotify");
                                mSpotifyAppRemote.getPlayerApi().subscribeToPlayerState().setEventCallback(playerState ->{
                                    databaseRef.child("lastPlayedSong").setValue(playerState.track);
                                    Log.d("LocalUser", "Guardando la canción predeterminada en Firebase");
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e("LocalUser", "Error al leer el valor de Firebase.", error.toException());
                        }
                    });
                }
            });
        }


        @Override
        public void onFailure(Throwable error) {
            if (error instanceof NotLoggedInException || error instanceof UserNotAuthorizedException) {

            } else if (error instanceof CouldNotFindSpotifyApp) {
                //
            }
        }
    };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        mAuth = FirebaseAuth.getInstance();
        FirebaseApp.initializeApp(this);
        mFirebaseService = new FirebaseService();
        mBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
        getSupportActionBar().hide();
        mLoginActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {

                        FirebaseMessaging.getInstance().getToken()
                                .addOnCompleteListener(new OnCompleteListener<String>() {
                                    @Override
                                    public void onComplete(@NonNull Task<String> task) {
                                        if (!task.isSuccessful()) {
                                            Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                                            return;
                                        }

                                        // Get new FCM registration token
                                        String token = task.getResult();
                                        // Save the token to SharedPreferences
                                        saveFcmTokenToFirebase(token);


                                        // Log and toast
                                        //String msg = getString(R.string.msg_token_fmt, token);
                                        // Log.d(TAG, msg);
                                        //Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                                    }
                                });
                        authUser();
                        guardarUserId(mAuth.getCurrentUser().getUid());
                        showSplashScreen();
                    } else {
                        // Handle the case when resultCode is not RESULT_OK
                    }
                });

        if (mAuth.getCurrentUser() != null) {
            mAuth.getCurrentUser().reload();
            mAccessToken = obtenerAccessToken();
            guardarUserId(mAuth.getCurrentUser().getUid());
            initializeLocalUser();
            showSplashScreen();
        } else {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            mLoginActivityResultLauncher.launch(intent);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        authUser();
        if(mSpotifyAppRemote==null)
            SpotifyAppRemote.connect(this, parametrosConexion, mConnectionListener);

    }



    @Override
    protected void onResume() {
        super.onResume();
        mAccessToken = obtenerAccessToken();
        SpotifyAppRemote.connect(this, parametrosConexion, mConnectionListener);
    }


    @Override
    protected void onStop() {
        super.onStop();
        guardarAccessToken(obtenerAccessToken());
        SpotifyAppRemote.connect(this, parametrosConexion, mConnectionListener);
        SpotifyAppRemote.disconnect(mSpotifyAppRemote);
    }


    // Muestra la pantalla de inicio
    private void showSplashScreen() {
        setContentView(R.layout.splash_screen);
        SpotifyAppRemote.connect(this, parametrosConexion, mConnectionListener);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(MainActivity.this, RadarActivity.class);
                intent.putExtra("user_id", mAuth.getUid());
                startActivity(intent);

            }
        }, 1000); // muestra la splash screen por 5 segundos antes de cargar la actividad principal
    }

    // Intercambia el fragment actual por otro
    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }

    @Override
    public void onBackPressed() {
        // No se permite volver atrás en la actividad principal
    }

    // Inicializa los datos del usuario local
    private void initializeLocalUser() {
        SpotifyAppRemote.connect(this, parametrosConexion, mConnectionListener);
        mLocalUser = new LocalUser(this);
    }

    // Guarda el token de acceso en las preferencias compartidas
    private void guardarAccessToken(String accessToken) {
        SharedPreferences sharedPreferences = getSharedPreferences("preferencias", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("access_token", accessToken);
        editor.apply();
    }

    // Obtiene el token de acceso de las preferencias compartidas
    public String obtenerAccessToken() {
        SharedPreferences sharedPreferences = getSharedPreferences("preferencias", MODE_PRIVATE);
        return sharedPreferences.getString("access_token", null);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // Guarda el estado de la aplicación que deseas conservar
        outState.putBoolean("isCodeExecuted", mIsCodeExecuted);
    }

    // Guarda el estado de autenticación en las preferencias compartidas
    private void guardarEstadoAutenticacion(boolean autenticado) {
        SharedPreferences sharedPreferences = getSharedPreferences("preferencias", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("autenticado", autenticado);
        editor.apply();
    }

    // Obtiene el estado de autenticación de las preferencias compartidas
    private boolean obtenerEstadoAutenticacion() {
        SharedPreferences sharedPreferences = getSharedPreferences("preferencias", MODE_PRIVATE);
        return sharedPreferences.getBoolean("autenticado", false);
    }

    // Guarda el ID de usuario en las preferencias compartidas
    private void guardarUserId(String userId) {
        SharedPreferences sharedPreferences = getSharedPreferences("preferencias", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("user_id", userId);
        editor.apply();
    }

    // Obtiene el ID de usuario de las preferencias compartidas
    private String obtenerUserId() {
        SharedPreferences sharedPreferences = getSharedPreferences("preferencias", MODE_PRIVATE);
        return sharedPreferences.getString("user_id", null);
    }

    public static String getToken() {
        return mAccessToken;
    }

    // Borra todas las preferencias compartidas al cerrar la aplicación

    public SharedPreferences getSharedPreferences() {
        return getSharedPreferences("preferencias", MODE_PRIVATE);
    }


    public Context getContext() {
        return mContext;
    }

    private void authUser() {
        // Se crea una instancia de AuthorizationRequest con los datos de nuestra aplicación y la URI de redireccionamiento
        AuthorizationRequest.Builder builder = new AuthorizationRequest.Builder(
                CLIENT_ID,
                TOKEN,
                REDIRECT_URI
        );
        builder.setScopes(new String[]{"user-read-private", "playlist-read", "user-library-read", "user-read-currently-playing", "user-read-playback-state", "user-modify-playback-state", "user-top-read"});
        AuthorizationRequest request = builder.build();

        // Se llama al método AuthorizationClient.openLoginActivity() para iniciar la actividad de autenticación de Spotify
        AuthorizationClient.openLoginActivity(this, REQUEST_CODE, request);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE) {
            AuthorizationResponse response = AuthorizationClient.getResponse(resultCode, intent);

            switch (response.getType()) {
                // Response was successful and contains auth token
                case TOKEN:
                    guardarAccessToken(response.getAccessToken());
                    guardarEstadoAutenticacion(true);
                    SpotifyAppRemote.connect(this, parametrosConexion, mConnectionListener);
                    break;

                // Auth flow returned an error
                case ERROR:
                    // Handle error response
                    break;

                // Most likely auth flow was cancelled
                default:
                    // Handle other cases
            }
        }
    }
    private void saveFcmTokenToFirebase(String token) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            DatabaseReference tokenRef = FirebaseDatabase.getInstance().getReference("users")
                    .child(currentUser.getUid())
                    .child("fcmToken");
            tokenRef.setValue(token);
        }
    }
}

