package com.example.spotifind;

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

    private static final String CLIENT_ID = "824f2fd7d9d14c38a7945ba2f7bb9c60";
    private static final String SECRET_CLIENT_ID = "3d1d551e6f0b4bd8b98aedf17d75f426";
    private static final String REDIRECT_URI = "com.example.spotifind://callback";
    private static final int LOGIN_ACTIVITY_REQUEST_CODE = 1;

    private ActivityMainBinding mBinding;
    private FirebaseAuth mAuth;
    private FirebaseService mFirebaseService;
    private SpotifyAppRemote mSpotifyAppRemote;
    private static String mAccessToken;
    private LocalUser mLocalUser;

    private CompletableFuture<Void> mFutureUpdate;
    private ActivityResultLauncher<Intent> mLoginActivityResultLauncher;
    private boolean mIsCodeExecuted = false;
    private Context mContext;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        // Inicializa las variables aquí
        mAuth = FirebaseAuth.getInstance();
        mFirebaseService = new FirebaseService();
        mBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
        getSupportActionBar().hide();

        mLoginActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            guardarEstadoAutenticacion(true);
                            guardarAccessToken(data.getStringExtra("access_token"));
                            Log.i("Token",data.getStringExtra("access_token"));
                            guardarUserId(mAuth.getUid());
                            conectarSpotifyAppRemote();
                        } else {
                            Toast.makeText(MainActivity.this, "Error al obtener el token de acceso de Spotify.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // Si no se obtiene el token de acceso, manejar el error
                        Toast.makeText(MainActivity.this, "Error al obtener el token de acceso de Spotify.", Toast.LENGTH_SHORT).show();
                    }
                });

        final boolean firebaseAuth = mAuth.getCurrentUser() != null;
        final boolean spotifyAuth = obtenerEstadoAutenticacion();

        if (firebaseAuth && spotifyAuth) {
            mAuth.getCurrentUser().reload();
            mAccessToken = obtenerAccessToken();
            conectarSpotifyAppRemote();
        } else {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            mLoginActivityResultLauncher.launch(intent);
        }
    }

    @Override
    protected void onStart() {
        conectarSpotifyAppRemote();
        super.onStart();
    }

    // Muestra la pantalla de inicio
    private void showSplashScreen() {
        setContentView(R.layout.splash_screen);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                initializeLocalUser();
                obtenerAccessToken();
                Intent intent = new Intent(MainActivity.this, RadarActivity.class);
                intent.putExtra("user_id", obtenerUserId());
                startActivity(intent);
                finish();
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
        mAccessToken = obtenerAccessToken();
        mLocalUser = new LocalUser(this,obtenerUserId());
        mLocalUser.setSpoifyAppRemote(mSpotifyAppRemote);
        Intent intent = new Intent(MainActivity.this, RadarActivity.class);
        intent.putExtra("user_id", obtenerUserId());
        startActivity(intent);
        finish();
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

    // Conecta al usuario a la API de Spotify App Remote
    private void conectarSpotifyAppRemote() {
        String token = obtenerAccessToken();
        if (token != null) {
            ConnectionParams parametrosConexion = new ConnectionParams.Builder(CLIENT_ID)
                    .setRedirectUri(REDIRECT_URI)
                    .build();
            SpotifyAppRemote.connect(this, parametrosConexion, new Connector.ConnectionListener() {
                @Override
                public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                    mSpotifyAppRemote = spotifyAppRemote;
                    Log.d("MainActivity", "¡Conectado! ¡Genial!");
                    //mFirebaseService.startRealtimeUpdates(obtenerUserId());
                    showSplashScreen();
                    mSpotifyAppRemote.getPlayerApi().subscribeToPlayerState().setEventCallback(playerState -> {
                        final Track track = playerState.track;
                        if (track != null) {
                            // Haz algo con la información de la canción actual
                        }
                    });

                }

                @Override
                public void onFailure(Throwable error) {
                    Log.e("MainActivity", "Error al conectar con SpotifyAppRemote", error);
                }
            });
        }
    }

    public static String getToken(){
        return mAccessToken;
    }

    // Borra todas las preferencias compartidas al cerrar la aplicación
    @Override
    protected void onDestroy() {
        super.onDestroy();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
        CookieManager.getInstance().removeAllCookies(null);
        CookieManager.getInstance().flush();
        //abrirPaginaInicioSesionSpotify()
    }

    public SharedPreferences getSharedPreferences() {
        return getSharedPreferences("preferencias", MODE_PRIVATE);
    }

    private void abrirPaginaInicioSesionSpotify() {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://accounts.spotify.com/logout"));
        startActivity(intent);
    }

    public Context getContext() {
        return mContext;
    }
}
