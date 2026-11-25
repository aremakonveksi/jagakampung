package com.example.lastproject;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;
import java.util.Map;

/**
 * LAYAR UJI: pendaftaran akun WARGA (jangan diupload).
 * Peran selalu "warga" dan status selalu "pending".
 * Admin TIDAK bisa dibuat dari sini: hanya diatur di database oleh Rizal.
 */
public class DaftarUji extends AppCompatActivity {

    private EditText etNama, etEmail, etRt, etRw, etPassword, etPassword2;
    private Button btnDaftar, btnKembali;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daftar_uji);
        TampilanUtil.layarPenuh(this);

        etNama = findViewById(R.id.etNama);
        etEmail = findViewById(R.id.etEmail);
        etRt = findViewById(R.id.etRt);
        etRw = findViewById(R.id.etRw);
        etPassword = findViewById(R.id.etPassword);
        etPassword2 = findViewById(R.id.etPassword2);
        btnDaftar = findViewById(R.id.btnDaftar);
        btnKembali = findViewById(R.id.btnKembali);

        btnDaftar.setOnClickListener(v -> daftar());
        btnKembali.setOnClickListener(v -> finish());
    }

    private void daftar() {
        String nama = etNama.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String rt = etRt.getText().toString().trim();
        String rw = etRw.getText().toString().trim();
        String sandi = etPassword.getText().toString();
        String sandi2 = etPassword2.getText().toString();

        // ----- Validasi -----
        if (nama.isEmpty()) {
            etNama.setError("Nama wajib diisi");
            etNama.requestFocus();
            return;
        }
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Email tidak valid");
            etEmail.requestFocus();
            return;
        }
        if (rt.isEmpty()) {
            etRt.setError("RT wajib diisi");
            etRt.requestFocus();
            return;
        }
        if (rw.isEmpty()) {
            etRw.setError("RW wajib diisi");
            etRw.requestFocus();
            return;
        }
        if (sandi.length() < 6) {
            etPassword.setError("Minimal 6 karakter");
            etPassword.requestFocus();
            return;
        }
        if (!sandi.equals(sandi2)) {
            etPassword2.setError("Kata sandi tidak sama");
            etPassword2.requestFocus();
            return;
        }

        setSibuk(true);

        // ----- 1. Buat akun di Firebase Authentication -----
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, sandi)
                .addOnSuccessListener(hasil -> {
                    String uid = hasil.getUser().getUid();

                    // ----- 2. Simpan profil di users/{uid} (sesuai kontrak README) -----
                    Map<String, Object> data = new HashMap<>();
                    data.put("nama", nama);
                    data.put("email", email);
                    data.put("rt", rt);
                    data.put("rw", rw);
                    data.put("peran", "warga");
                    data.put("status", "pending");
                    data.put("dibuatPada", ServerValue.TIMESTAMP);

                    FirebaseDatabase.getInstance().getReference("users").child(uid)
                            .setValue(data)
                            .addOnSuccessListener(v -> {
                                FirebaseAuth.getInstance().signOut();
                                Toast.makeText(this,
                                        "Pendaftaran berhasil. Tunggu akunmu disetujui, lalu masuk.",
                                        Toast.LENGTH_LONG).show();
                                finish(); // kembali ke halaman Masuk
                            })
                            .addOnFailureListener(e -> {
                                setSibuk(false);
                                FirebaseAuth.getInstance().signOut();
                                Toast.makeText(this,
                                        "Akun dibuat, tapi profil ditolak Rules: " + e.getMessage(),
                                        Toast.LENGTH_LONG).show();
                            });
                })
                .addOnFailureListener(e -> {
                    setSibuk(false);
                    Toast.makeText(this, "Gagal daftar: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void setSibuk(boolean sibuk) {
        btnDaftar.setEnabled(!sibuk);
        btnKembali.setEnabled(!sibuk);
        btnDaftar.setText(sibuk ? "Mendaftar..." : "Daftar");
    }
}