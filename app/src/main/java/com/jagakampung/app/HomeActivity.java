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
    private NotifWatcher.Pasangan notifWarga;

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
        MaterialButton btnBuatLaporan = findViewById(R.id.btn_buat_laporan);
        MaterialButton btnLaporanSaya = findViewById(R.id.btn_laporan_saya);
        MaterialButton btnUpdate = findViewById(R.id.btn_update);
        TextView tvVersi = findViewById(R.id.tv_versi);

        tvVersi.setText(getString(R.string.versi_terpasang, BuildConfig.VERSION_NAME));
        btnUpdate.setOnClickListener(v -> UpdateDialog.cekManual(this));

        btnKeluar.setOnClickListener(v -> keluar());
        btnBuatLaporan.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this,
                        com.jagakampung.app.warga.LaporanForm.class)));
        btnLaporanSaya.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this,
                        com.jagakampung.app.warga.LaporanSaya.class)));

        NotifUtil.siapkanKanal(this);
        NotifUtil.mintaIzin(this);

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            kembaliKeLogin();
            return;
        }
        muatProfil(user.getUid());

        // Notifikasi lokal: status laporan warga ini berubah
        notifWarga = NotifWatcher.pasangWarga(this, user);

        // Pemeriksaan latar berkala (tetap jalan walau aplikasi ditutup,
        // selama tidak di-force stop oleh sistem/user)
        CekNotifWorker.jadwalkan(this);

        // Cek update sekali saat beranda dibuka (senyap)
        UpdateDialog.cekSenyap(this);
    }

    @Override
    protected void onDestroy() {
        if (notifWarga != null) notifWarga.lepas();
        super.onDestroy();
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
        CekNotifWorker.hentikan(this);
        auth.signOut();
        kembaliKeLogin();
    }

    private void kembaliKeLogin() {
        startActivity(new Intent(HomeActivity.this, LoginActivity.class));
        finish();
    }
}
