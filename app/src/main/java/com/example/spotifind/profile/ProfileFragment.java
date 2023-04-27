package com.example.spotifind.profile;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.example.spotifind.LocalUser;
import com.example.spotifind.R;

public class ProfileFragment extends Fragment {

    private static final String ARG_IS_PRIVATE_PROFILE = "isPrivateProfile";

    private boolean isPrivateProfile;

    public ProfileFragment() {
        // Constructor vacío requerido
    }

    public static ProfileFragment newInstance(boolean isPrivateProfile) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_IS_PRIVATE_PROFILE, isPrivateProfile);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            isPrivateProfile = getArguments().getBoolean(ARG_IS_PRIVATE_PROFILE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        LocalUser user = new LocalUser(getContext());
        setInterface(view, user);
        return view;
    }

    private void setInterface(View view, LocalUser user) {
        // Aquí, actualiza tus referencias de elementos de la vista usando "view" en lugar de "this" o "getContext()"
        // Por ejemplo: textNickname = view.findViewById(R.id.textNickname);

        ImageButton editButton = view.findViewById(R.id.buttonEdit);

        if (isPrivateProfile) {
            editButton.setVisibility(View.VISIBLE);
        } else {
            editButton.setVisibility(View.GONE);
        }
    }
}
