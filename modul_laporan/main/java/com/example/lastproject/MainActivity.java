package com.example.lastproject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * LAYAR UJI: halaman Masuk (bukan bagian final aplikasi, jangan diupload).
 */
public class MainActivity extends AppCompatActivity {

    // true  = halaman Masuk SELALU tampil saat aplikasi dibuka
    // false = kalau sudah pernah masuk, langsung ke Beranda
    private static final boolean SELALU_TAMPILKAN_LOGIN = true;

    private EditText etEmail, etPassword;
    private Button btnMasuk, btnDaftar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TampilanUtil.layarPenuh(this);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnMasuk = findViewById(R.id.btnMasuk);
        btnDaftar = findViewById(R.id.btnDaftar);

        btnMasuk.setOnClickListener(v -> masuk());
        btnDaftar.setOnClickListener(v ->
                startActivity(new Intent(this, DaftarUji.class)));

        if (SELALU_TAMPILKAN_LOGIN) {
            FirebaseAuth.getInstance().signOut();
        } else if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            periksaAkun();
        }
    }

    private void masuk() {
        String email = etEmail.getText().toString().trim();
        String sandi = etPassword.getText().toString();
        if (email.isEmpty() || sandi.isEmpty()) {
            Toast.makeText(this, "Email dan kata sandi wajib diisi", Toast.LENGTH_SHORT).show();
            return;
        }
        setSibuk(true);
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, sandi)
                .addOnSuccessListener(hasil -> periksaAkun())
                .addOnFailureListener(e -> {
                    setSibuk(false);
                    Toast.makeText(this, "Gagal masuk: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    /** Baca users/{uid}, lalu bedakan warga / admin dan status akun. */
    private void periksaAkun() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            setSibuk(false);
            return;
        }
        setSibuk(true);
        FirebaseDatabase.getInstance().getReference("users").child(user.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        setSibuk(false);

                        if (!snapshot.exists()) {
                            keluarDenganPesan("Profil akun tidak ditemukan. Hubungi admin.");
                            return;
                        }
                        String status = snapshot.child("status").getValue(String.class);
                        String peran = snapshot.child("peran").getValue(String.class);

                        if (!"approved".equals(status)) {
                            keluarDenganPesan("Akun belum disetujui (status: " + status + ").");
                            return;
                        }
                        if ("admin".equals(peran)) {
                            keluarDenganPesan("Akun admin: halaman admin ada di modul Rizqi.");
                            return;
                        }
                        startActivity(new Intent(MainActivity.this, BerandaUji.class));
                        finish();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        setSibuk(false);
                        Toast.makeText(MainActivity.this,
                                "Gagal membaca profil: " + error.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void keluarDenganPesan(String pesan) {
        FirebaseAuth.getInstance().signOut();
        Toast.makeText(this, pesan, Toast.LENGTH_LONG).show();
    }

    private void setSibuk(boolean sibuk) {
        btnMasuk.setEnabled(!sibuk);
        btnDaftar.setEnabled(!sibuk);
    }
}