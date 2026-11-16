package com.jagakampung.app;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignupActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private UsersRepository usersRepository;
    private TextInputEditText etNama;
    private TextInputEditText etEmail;
    private TextInputEditText etRt;
    private TextInputEditText etRw;
    private TextInputEditText etSandi;
    private MaterialButton btnDaftar;
    private ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        auth = FirebaseAuth.getInstance();
        usersRepository = new UsersRepository();

        etNama = findViewById(R.id.et_nama);
        etEmail = findViewById(R.id.et_email);
        etRt = findViewById(R.id.et_rt);
        etRw = findViewById(R.id.et_rw);
        etSandi = findViewById(R.id.et_sandi);
        btnDaftar = findViewById(R.id.btn_daftar);
        progress = findViewById(R.id.progress);
        TextView tvKeMasuk = findViewById(R.id.tv_ke_masuk);

        btnDaftar.setOnClickListener(v -> cobaDaftar());
        tvKeMasuk.setOnClickListener(v -> {
            startActivity(new Intent(SignupActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void cobaDaftar() {
        String nama = teks(etNama);
        String email = teks(etEmail);
        String rt = teks(etRt);
        String rw = teks(etRw);
        String sandi = teks(etSandi);

        if (nama.length() < 3) {
            etNama.setError(getString(R.string.nama_min_tiga));
            etNama.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError(getString(R.string.email_tidak_valid));
            etEmail.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(rt)) {
            etRt.setError(getString(R.string.rt_wajib));
            etRt.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(rw)) {
            etRw.setError(getString(R.string.rw_wajib));
            etRw.requestFocus();
            return;
        }
        if (sandi.length() < 6) {
            etSandi.setError(getString(R.string.sandi_min_enam));
            etSandi.requestFocus();
            return;
        }

        setSibuk(true);
        auth.createUserWithEmailAndPassword(email, sandi)
                .addOnCompleteListener(this, task -> {
                    if (!task.isSuccessful()) {
                        setSibuk(false);
                        String pesan = task.getException() == null
                                ? getString(R.string.gagal_daftar)
                                : task.getException().getMessage();
                        Toast.makeText(this, pesan, Toast.LENGTH_LONG).show();
                        return;
                    }
                    FirebaseUser user = auth.getCurrentUser();
                    if (user == null) {
                        setSibuk(false);
                        Toast.makeText(this, R.string.gagal_daftar, Toast.LENGTH_LONG).show();
                        return;
                    }
                    simpanProfil(user.getUid(), nama, email, rt, rw);
                });
    }

    private void simpanProfil(String uid, String nama, String email, String rt, String rw) {
        usersRepository.simpanProfilBaru(uid, nama, email, rt, rw, (berhasil, pesan) -> {
            setSibuk(false);
            if (!berhasil) {
                Toast.makeText(this, pesan, Toast.LENGTH_LONG).show();
                return;
            }
            Toast.makeText(this, R.string.daftar_berhasil, Toast.LENGTH_LONG).show();
            startActivity(new Intent(SignupActivity.this, HomeActivity.class));
            finish();
        });
    }

    private String teks(TextInputEditText input) {
        return input.getText() == null ? "" : input.getText().toString().trim();
    }

    private void setSibuk(boolean sibuk) {
        progress.setVisibility(sibuk ? View.VISIBLE : View.GONE);
        btnDaftar.setEnabled(!sibuk);
    }
}
