package com.example.thinglist;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class ProfileFragment extends Fragment {

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageButton btnBack              = view.findViewById(R.id.btnBack);
        LinearLayout rowProfileInfo      = view.findViewById(R.id.rowProfileInfo);
        LinearLayout rowSecurity         = view.findViewById(R.id.rowSecurity);
        LinearLayout rowBackup           = view.findViewById(R.id.rowBackup);
        LinearLayout rowImportExport     = view.findViewById(R.id.rowImportExport);
        LinearLayout rowStorage          = view.findViewById(R.id.rowStorage);
        SwitchMaterial switchNotifications = view.findViewById(R.id.switchNotifications);
        MaterialButton btnLogout         = view.findViewById(R.id.btnLogout);

        // User card views
        TextView tvUserName       = view.findViewById(R.id.tvUserName);
        TextView tvUserEmail      = view.findViewById(R.id.tvUserEmail);
        TextView tvAvatarInitials = view.findViewById(R.id.tvAvatarInitials);

        // --- Load saved user info using AuthManager ---
        AuthManager auth = new AuthManager(requireContext());

        String userName  = auth.getUserName();
        String userEmail = auth.getUserEmail();

        if (userName == null || userName.trim().isEmpty()) {
            userName = "User";
        }
        if (userEmail == null || userEmail.trim().isEmpty()) {
            userEmail = "user@email.com";
        }

        tvUserName.setText(userName);
        tvUserEmail.setText(userEmail);
        tvAvatarInitials.setText(getInitials(userName));

        // Back arrow → go back to Dashboard
        btnBack.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).openFragment(new DashboardFragment());
            } else {
                requireActivity().onBackPressed();
            }
        });

        rowProfileInfo.setOnClickListener(v ->
                Toast.makeText(requireContext(),
                        "Profile details not implemented in demo.",
                        Toast.LENGTH_SHORT).show());

        rowSecurity.setOnClickListener(v ->
                Toast.makeText(requireContext(),
                        "Security & privacy not implemented in demo.",
                        Toast.LENGTH_SHORT).show());

        rowBackup.setOnClickListener(v ->
                Toast.makeText(requireContext(),
                        "Backup settings not implemented in demo.",
                        Toast.LENGTH_SHORT).show());

        rowImportExport.setOnClickListener(v ->
                Toast.makeText(requireContext(),
                        "Import/Export data not implemented in demo.",
                        Toast.LENGTH_SHORT).show());

        rowStorage.setOnClickListener(v ->
                Toast.makeText(requireContext(),
                        "Storage details not implemented in demo.",
                        Toast.LENGTH_SHORT).show());

        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) ->
                Toast.makeText(requireContext(),
                        isChecked ? "Notifications enabled" : "Notifications disabled",
                        Toast.LENGTH_SHORT).show());

        // --- Log Out ---
        btnLogout.setOnClickListener(v -> {
            // Clear auth prefs (simple demo logout)
            auth.clearAll();

            Toast.makeText(requireContext(),
                    "Logged out (demo – no real auth).",
                    Toast.LENGTH_SHORT).show();

            // TODO: navigate back to login screen if you have one
            // if (getActivity() instanceof MainActivity) {
            //     ((MainActivity) getActivity()).openFragment(new LoginFragment());
            // }
        });
    }

    /** Build initials from full name, e.g. "Andrews Palaparthi" -> "AP" */
    private String getInitials(String name) {
        if (name == null || name.trim().isEmpty()) return "?";

        String[] parts = name.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();

        // First letter of first word
        sb.append(Character.toUpperCase(parts[0].charAt(0)));

        // First letter of last word (if multiple words)
        if (parts.length > 1) {
            sb.append(Character.toUpperCase(
                    parts[parts.length - 1].charAt(0)
            ));
        }

        return sb.toString();
    }
}
