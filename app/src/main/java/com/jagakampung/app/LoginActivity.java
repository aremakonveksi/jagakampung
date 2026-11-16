package com.jagakampung.app;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private TextInputEditText etEmail;
    private TextInputEditText etSandi;
    private MaterialButton btnMasuk;
    private ProgressBar progress;
    private TextView tvLoginError;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private int loginAttempt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();

        etEmail = findViewById(R.id.et_email);
        etSandi = findViewById(R.id.et_sandi);
        btnMasuk = findViewById(R.id.btn_masuk);
        progress = findViewById(R.id.progress);
        tvLoginError = findViewById(R.id.tv_login_error);
        TextView tvKeDaftar = findViewById(R.id.tv_ke_daftar);

        FirebaseUser aktif = auth.getCurrentUser();
        if (aktif != null) {
            bukaHome();
            return;
        }

        btnMasuk.setOnClickListener(v -> cobaMasuk());
        tvKeDaftar.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, SignupActivity.class)));
    }

    private void cobaMasuk() {
        String email = etEmail.getText() == null ? "" : etEmail.getText().toString().trim();
        String sandi = etSandi.getText() == null ? "" : etSandi.getText().toString();

        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError(getString(R.string.email_tidak_valid));
            etEmail.requestFocus();
            return;
        }
        if (sandi.length() < 6) {
            etSandi.setError(getString(R.string.sandi_min_enam));
            etSandi.requestFocus();
            return;
        }

        tvLoginError.setVisibility(View.GONE);
        loginAttempt++;
        int attempt = loginAttempt;
        setSibuk(true);
        Runnable timeout = () -> {
            if (attempt == loginAttempt && progress.getVisibility() == View.VISIBLE) {
                setSibuk(false);
                tampilkanError(getString(R.string.login_timeout));
            }
        };
        mainHandler.postDelayed(timeout, 4_000L);

        auth.signInWithEmailAndPassword(email, sandi)
                .addOnCompleteListener(this, task -> {
                    if (attempt != loginAttempt) {
                        return;
                    }
                    mainHandler.removeCallbacks(timeout);
                    setSibuk(false);
                    if (task.isSuccessful()) {
                        bukaHome();
                    } else {
                        tampilkanError(pesanGagal(task.getException()));
                    }
                });
    }

    private void setSibuk(boolean sibuk) {
        progress.setVisibility(sibuk ? View.VISIBLE : View.GONE);
        btnMasuk.setEnabled(!sibuk);
    }

    private void tampilkanError(String pesan) {
        tvLoginError.setText(pesan);
        tvLoginError.setVisibility(View.VISIBLE);
    }

    private String pesanGagal(Exception e) {
        return e instanceof com.google.firebase.FirebaseNetworkException
                ? getString(R.string.gagal_koneksi)
                : getString(R.string.gagal_masuk);
    }

    private void bukaHome() {
        startActivity(new Intent(LoginActivity.this, HomeActivity.class));
        finish();
    }

    @Override
    protected void onDestroy() {
        mainHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
