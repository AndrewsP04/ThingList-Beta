package com.example.thinglistbeta;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class LoginFragment extends Fragment {

    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin, btnAppleSignIn, btnGoogleSignIn;
    private CheckBox cbRememberMe;
    private TextView tvNeedAccount, tvForgotPassword;

    public LoginFragment() {
        // Required empty constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etEmail = view.findViewById(R.id.etLoginEmail);
        etPassword = view.findViewById(R.id.etLoginPassword);
        cbRememberMe = view.findViewById(R.id.cbRememberMe);
        btnLogin = view.findViewById(R.id.btnLogin);
        btnAppleSignIn = view.findViewById(R.id.btnAppleSignIn);
        btnGoogleSignIn = view.findViewById(R.id.btnGoogleSignIn);
        tvNeedAccount = view.findViewById(R.id.tvNeedAccount);
        tvForgotPassword = view.findViewById(R.id.tvForgotPassword);

        AuthManager authManager = new AuthManager(requireContext());

        // Prefill email / remember state if previously set
        if (authManager.hasUser() && authManager.isRememberMe()) {
            etEmail.setText(authManager.getUserEmail());
            cbRememberMe.setChecked(true);
        }

        btnLogin.setOnClickListener(v -> handleLogin(authManager));
        tvNeedAccount.setOnClickListener(v -> navigateToSignUp());

        // Social sign-in buttons: demo only
        btnAppleSignIn.setOnClickListener(v ->
                Toast.makeText(requireContext(),
                        "Apple sign-in not implemented in demo.",
                        Toast.LENGTH_SHORT).show());

        btnGoogleSignIn.setOnClickListener(v ->
                Toast.makeText(requireContext(),
                        "Google sign-in not implemented in demo.",
                        Toast.LENGTH_SHORT).show());

        tvForgotPassword.setOnClickListener(v ->
                Toast.makeText(requireContext(),
                        "Forgot password flow not implemented in demo.",
                        Toast.LENGTH_SHORT).show());
    }

    private void handleLogin(AuthManager authManager) {
        String email = getText(etEmail);
        String password = getText(etPassword);

        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Enter a valid email");
            etEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Enter your password");
            etPassword.requestFocus();
            return;
        }

        if (!authManager.hasUser()) {
            Toast.makeText(requireContext(),
                    "No account found. Please sign up first.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (authManager.validateLogin(email, password)) {
            authManager.setRememberMe(cbRememberMe.isChecked());

            // Tell MainActivity to show navbar and go to Dashboard
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).openDashboardScreen();
            }

        } else {
            Toast.makeText(requireContext(),
                    "Invalid email or password.",
                    Toast.LENGTH_SHORT).show();
        }

    }

    private void navigateToSignUp() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).openFragment(new SignUpFragment());
        }
    }


    private String getText(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }
}
