package com.jagakampung.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsCompat.Type;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class HomeActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private UsersRepository usersRepository;
    private TextView tvSapaan;
    private TextView tvSub;
    private TextView tvBadge;
    private ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        EdgeToEdge.apply(this, R.id.root_home);

        auth = FirebaseAuth.getInstance();
        usersRepository = new UsersRepository();

        tvSapaan = findViewById(R.id.tv_sapaan);
        tvSub = findViewById(R.id.tv_sub);
        tvBadge = findViewById(R.id.tv_badge);
        progress = findViewById(R.id.progress);
        MaterialButton btnKeluar = findViewById(R.id.btn_keluar);

        btnKeluar.setOnClickListener(v -> keluar());

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            kembaliKeLogin();
            return;
        }
        muatProfil(user.getUid());
    }

    private void muatProfil(String uid) {
        progress.setVisibility(View.VISIBLE);
        usersRepository.bacaProfil(uid, new UsersRepository.ProfilCallback() {
            @Override
            public void onProfil(UsersRepository.Profil profil) {
                progress.setVisibility(View.GONE);
                tampilkanProfil(profil);
            }

            @Override
            public void onGagal(String pesan) {
                progress.setVisibility(View.GONE);
                Toast.makeText(HomeActivity.this, pesan, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void tampilkanProfil(UsersRepository.Profil profil) {
        String nama = profil.nama == null || profil.nama.isEmpty() ? "Warga" : profil.nama;
        tvSapaan.setText(getString(R.string.selamat_datang, nama));

        if (profil.sudahDisetujui()) {
            tvBadge.setText(R.string.status_approved);
            tvBadge.setBackgroundResource(R.drawable.bg_badge_hijau);
            tvSub.setText(R.string.sub_home);
        } else if (profil.ditolak()) {
            tvBadge.setText(R.string.status_rejected);
            tvBadge.setBackgroundResource(R.drawable.bg_badge_merah);
            String alasan = profil.alasanTolak == null || profil.alasanTolak.isEmpty()
                    ? getString(R.string.akun_ditolak)
                    : getString(R.string.akun_ditolak_alasan, profil.alasanTolak);
            tvSub.setText(alasan);
        } else {
            tvBadge.setText(R.string.status_pending);
            tvBadge.setBackgroundResource(R.drawable.bg_badge_kuning);
            tvSub.setText(R.string.akun_menunggu);
        }
    }

    private void keluar() {
        auth.signOut();
        kembaliKeLogin();
    }

    private void kembaliKeLogin() {
        startActivity(new Intent(HomeActivity.this, LoginActivity.class));
        finish();
    }
}
