package com.example.spotifind.profile;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.spotifind.NavigationBarListener;
import com.example.spotifind.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ProfileActivity extends AppCompatActivity {

    private BottomNavigationView navBar;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_activity);

        uid = getIntent().getStringExtra("user_id");
        boolean fromNotification = getIntent().getBooleanExtra("from_notification", false);

        navBar = findViewById(R.id.navbar);
        navBar.setSelectedItemId(R.id.profile);
        NavigationBarListener navigationBarListener = new NavigationBarListener(this, uid);
        navBar.setOnNavigationItemSelectedListener(navigationBarListener);

        // Agrega el fragmento al contenedor en profile_activity.xml
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        // Verificar si el uid es el mismo que el uid del usuario actual utilizando SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("preferencias", MODE_PRIVATE);
        String currentUserUid = sharedPreferences.getString("user_uid", "");

        if (!fromNotification || uid == null || uid.isEmpty()) {
            uid = currentUserUid;
        }

        // Utiliza ProfileFragment en lugar de InternalProfileFragment y PublicProfileFragment
        boolean isPrivateProfile = currentUserUid.equals(uid);

        fragmentTransaction.add(R.id.fragment_container, ProfileFragment.newInstance(isPrivateProfile, uid));
        fragmentTransaction.commit();
    }
}
