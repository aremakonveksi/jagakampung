package com.example.lastproject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import modul_laporan.LaporanForm;
import modul_laporan.LaporanSaya;

/** LAYAR UJI: Beranda setelah login (jangan diupload). */
public class BerandaUji extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beranda_uji);
        TampilanUtil.layarPenuh(this);

        TextView tvSapaan = findViewById(R.id.tvSapaan);
        TextView tvAkun = findViewById(R.id.tvAkun);
        Button btnForm = findViewById(R.id.btnBukaForm);
        Button btnSaya = findViewById(R.id.btnBukaSaya);
        Button btnKeluar = findViewById(R.id.btnKeluar);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            keHalamanMasuk();
            return;
        }
        tvAkun.setText("Masuk sebagai: " + user.getEmail());

        // Sapaan memakai nama dari users/{uid}/nama
        FirebaseDatabase.getInstance().getReference("users").child(user.getUid()).child("nama")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String nama = snapshot.getValue(String.class);
                        if (nama != null && !nama.isEmpty()) {
                            tvSapaan.setText("Selamat datang, " + nama);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { }
                });

        btnForm.setOnClickListener(v -> startActivity(new Intent(this, LaporanForm.class)));
        btnSaya.setOnClickListener(v -> startActivity(new Intent(this, LaporanSaya.class)));

        btnKeluar.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            keHalamanMasuk();
        });
    }

    private void keHalamanMasuk() {
        Intent i = new Intent(this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
        finish();
    }
}