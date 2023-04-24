package com.example.spotifind.Autentication;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.CookieManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.spotifind.R;
import com.spotify.sdk.android.auth.AuthorizationClient;
import com.spotify.sdk.android.auth.AuthorizationRequest;
import com.spotify.sdk.android.auth.AuthorizationResponse;
import com.spotify.sdk.android.auth.*;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {
    // Declarar las variables correspondientes a los campos de texto y los botones
    private EditText mEmailField;
    private EditText mPasswordField;
    private Button mLoginButton;
    private Button mSignUpButton;

    private FirebaseAuth mAuth;

    // Constantes para la autenticación de Spotify
    private static final String CLIENT_ID = "824f2fd7d9d14c38a7945ba2f7bb9c60";
    private static final String SECRET_CLIENT_ID = "3d1d551e6f0b4bd8b98aedf17d75f426";
    private static final String REDIRECT_URI = "com.example.spotifind://callback";
    private static final int AUTH_TOKEN_REQUEST_CODE = 0x10;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Inicializar Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        // Inicializar la interfaz de usuario
        setInterface();
    }

    private void setInterface() {
        // Obtener las referencias a los campos de texto y los botones en el layout
        setContentView(R.layout.login);
        mEmailField = findViewById(R.id.etEmail);
        mPasswordField = findViewById(R.id.etPass);
        mLoginButton = findViewById(R.id.btnLogin);
        mSignUpButton = findViewById(R.id.btnSignUpRedirect);
        // Establecer el listener para el botón de login
        mLoginButton.setOnClickListener(v -> {
            String email = mEmailField.getText().toString();
            String password = mPasswordField.getText().toString();
            signIn(email, password);
        });

        mSignUpButton.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
            startActivity(intent);
        });

    }

    private void signIn(String email, String password) {
        // Verificar que los campos de correo electrónico y contraseña no estén vacíos
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Por favor ingrese correo electrónico", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Por favor ingrese contraseña", Toast.LENGTH_SHORT).show();
            return;
        }

        // Iniciar sesión con correo electrónico y contraseña en Firebase Authentication
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Inicio de sesión exitoso en Firebase
                        Log.d(TAG, "signInWithEmail:success");
                        iniciarAutenticacionSpotify();
                    } else {
                        // Si el inicio de sesión falla, mostrar un mensaje al usuario
                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                        Toast.makeText(LoginActivity.this, "Error en la autenticación.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void iniciarAutenticacionSpotify() {
        AuthorizationRequest.Builder builder = new AuthorizationRequest.Builder(CLIENT_ID, AuthorizationResponse.Type.TOKEN, Uri.parse(REDIRECT_URI).toString());
        builder.setScopes(new String[]{"user-read-email","user-read-private", "playlist-read", "user-library-read", "user-read-currently-playing", "user-read-playback-state", "user-modify-playback-state","user-top-read"});
        builder.setShowDialog(true);
        AuthorizationRequest request = builder.build();
        AuthorizationClient.openLoginActivity(this, AUTH_TOKEN_REQUEST_CODE, request);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Manejar el resultado de la autenticación de Spotify
        if (requestCode == AUTH_TOKEN_REQUEST_CODE) {
            AuthorizationResponse response = AuthorizationClient.getResponse(resultCode, data);
            if (response.getType() == AuthorizationResponse.Type.TOKEN) {
                // Autenticación exitosa de Spotify
                Log.d(TAG, "onActivityResult: Autenticación de Spotify exitosa");
                Toast.makeText(LoginActivity.this, "¡Inicio de sesión exitoso!", Toast.LENGTH_SHORT).show();
                Intent resultIntent = new Intent();
                resultIntent.putExtra("access_token", response.getAccessToken());
                // Establecer el resultado antes de finalizar la actividad
                setResult(RESULT_OK, resultIntent);
                finish(); // finalizar la actividad actual
            } else {
                // Si la autenticación de Spotify falla, mostrar un mensaje al usuario
                Log.w(TAG, "onActivityResult: Autenticación de Spotify fallida");
                Toast.makeText(LoginActivity.this, "Autenticación de Spotify fallida.", Toast.LENGTH_SHORT).show();
            }
        }
    }

}

