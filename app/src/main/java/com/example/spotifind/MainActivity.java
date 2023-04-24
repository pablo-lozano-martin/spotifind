package com.example.spotifind;

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

import com.example.spotifind.Autentication.LoginActivity;
import com.example.spotifind.firebase.FirebaseService;
import com.example.spotifind.databinding.ActivityMainBinding;
import com.example.spotifind.radar.RadarActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

import java.util.concurrent.CompletableFuture;

public class MainActivity extends AppCompatActivity {

    private static final String CLIENT_ID = "824f2fd7d9d14c38a7945ba2f7bb9c60";
    private static final String SECRET_CLIENT_ID = "3d1d551e6f0b4bd8b98aedf17d75f426";
    private static final String REDIRECT_URI = "com.example.spotifind://callback";
    private static final int LOGIN_ACTIVITY_REQUEST_CODE = 1;

    private ActivityMainBinding mBinding;
    private FirebaseAuth mAuth;
    private FirebaseService mFirebaseService;
    public static SpotifyAppRemote mSpotifyAppRemote;
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
            mSpotifyAppRemote = spotifyAppRemote;
        }

        @Override
        public void onFailure(Throwable error) {
            if (error instanceof NotLoggedInException || error instanceof UserNotAuthorizedException) {

            } else if (error instanceof CouldNotFindSpotifyApp) {
               //
            }
        }
    };

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
                        authUser();
                        showSplashScreen();
                    }
                    else {
                    }
                });

        if (mAuth.getCurrentUser()!=null) {
            mAuth.getCurrentUser().reload();
            mAccessToken = obtenerAccessToken();
            showSplashScreen();
        } else {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            mLoginActivityResultLauncher.launch(intent);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        SpotifyAppRemote.disconnect(mSpotifyAppRemote);
        SpotifyAppRemote.connect(this, parametrosConexion, mConnectionListener);

    }

        @Override
        protected void onStop() {
            super.onStop();
            SpotifyAppRemote.disconnect(mSpotifyAppRemote);
    }


    // Muestra la pantalla de inicio
    private void showSplashScreen() {
        setContentView(R.layout.splash_screen);
        SpotifyAppRemote.connect(this, parametrosConexion, mConnectionListener);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                initializeLocalUser();
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
        mLocalUser = new LocalUser(this,mAuth.getUid(),mAccessToken);
        mLocalUser.setSpoifyAppRemote();
        Intent intent = new Intent(MainActivity.this, RadarActivity.class);
        intent.putExtra("user_id", mAuth.getUid());
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

    public static String getToken(){
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
        builder.setScopes(new String[]{"user-read-private", "playlist-read", "user-library-read", "user-read-currently-playing", "user-read-playback-state", "user-modify-playback-state","user-top-read"});
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
}
