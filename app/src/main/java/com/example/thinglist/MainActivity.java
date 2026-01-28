package com.example.thinglist;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.concurrent.Executor;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;

    // --- Biometric fields ---
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;
    private Executor executor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        bottomNav = findViewById(R.id.bottom_nav);

        // Init fingerprint prompt once
        initBiometrics();

        // Setup nav bar (uses biometrics for vault)
        setupBottomNav();

        // First screen: Login (navbar hidden)
        if (savedInstanceState == null) {
            openFragment(new LoginFragment());
        }
    }

    // -------------------------------------------------
    // Biometric setup
    // -------------------------------------------------
    private void initBiometrics() {
        executor = ContextCompat.getMainExecutor(this);

        biometricPrompt = new BiometricPrompt(
                this,
                executor,
                new BiometricPrompt.AuthenticationCallback() {

                    @Override
                    public void onAuthenticationSucceeded(
                            @NonNull BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        // Open Vault ONLY after successful auth
                        openFragment(new VaultFragment());
                    }

                    @Override
                    public void onAuthenticationError(
                            int errorCode,
                            @NonNull CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);
                        Toast.makeText(
                                MainActivity.this,
                                "Authentication cancelled",
                                Toast.LENGTH_SHORT
                        ).show();
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
                        Toast.makeText(
                                MainActivity.this,
                                "Fingerprint not recognized",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Unlock Vault")
                .setSubtitle("Use your fingerprint to open your vault")
                .setNegativeButtonText("Cancel")
                .setAllowedAuthenticators(
                        BiometricManager.Authenticators.BIOMETRIC_STRONG
                )
                .build();
    }

    private void requireVaultAuthAndOpen() {
        BiometricManager manager = BiometricManager.from(this);
        int canAuth = manager.canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_STRONG
        );

        if (canAuth == BiometricManager.BIOMETRIC_SUCCESS) {
            biometricPrompt.authenticate(promptInfo);
        } else {
            // Device has no biometrics set up – fallback behavior
            Toast.makeText(
                    this,
                    "Biometrics not set up on this device – opening vault without lock.",
                    Toast.LENGTH_LONG
            ).show();
            openFragment(new VaultFragment());
        }
    }

    // -------------------------------------------------
    // Bottom nav
    // -------------------------------------------------
    private void setupBottomNav() {
        if (bottomNav == null) return;

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selected = null;
            int id = item.getItemId();

            if (id == R.id.nav_dashboard) {
                selected = new DashboardFragment();
            } else if (id == R.id.nav_add) {
                selected = new AddItemMethodFragment();
            } else if (id == R.id.nav_vault) {
                // Require fingerprint every time vault is tapped
                requireVaultAuthAndOpen();
                return true; // we handled it
            } else if (id == R.id.nav_profile) {
                selected = new ProfileFragment();
            }

            if (selected != null) {
                openFragment(selected);
                return true;
            }
            return false;
        });
    }

    public void openFragment(Fragment fragment) {
        boolean hideNav = (fragment instanceof LoginFragment)
                || (fragment instanceof SignUpFragment);

        if (bottomNav != null) {
            bottomNav.setVisibility(hideNav ? View.GONE : View.VISIBLE);
        }

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container_view, fragment)
                .commit();
    }

    // Call this from Login / SignUp when auth succeeds:
    // ((MainActivity) requireActivity()).openDashboardScreen();
    public void openDashboardScreen() {
        if (bottomNav != null) {
            bottomNav.setVisibility(View.VISIBLE);
            bottomNav.setSelectedItemId(R.id.nav_dashboard);
        } else {
            openFragment(new DashboardFragment());
        }
    }
}
