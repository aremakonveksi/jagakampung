package modul_laporan;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.example.lastproject.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class LaporanForm extends AppCompatActivity {

    // ====== Komponen layar ======
    private EditText etJudul, etDeskripsi, etAlamat;
    private Spinner spKategori;
    private TextView tvLokasi;
    private ImageView ivFoto;
    private Button btnLokasi, btnFoto, btnKirim;

    // ====== Data sementara ======
    private Double lat = null;
    private Double lng = null;
    private File fileFoto = null;
    private boolean fotoSudahDiambil = false;

    private FusedLocationProviderClient lokasiClient;

    // ====== Peminta izin lokasi ======
    private final ActivityResultLauncher<String> izinLokasi =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), diizinkan -> {
                if (diizinkan) {
                    ambilLokasi();
                } else {
                    Toast.makeText(this, "Izin lokasi ditolak", Toast.LENGTH_SHORT).show();
                }
            });

    // ====== Pembuka kamera ======
    private final ActivityResultLauncher<Uri> kamera =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), berhasil -> {
                if (berhasil && fileFoto != null) {
                    fotoSudahDiambil = true;
                    BitmapFactory.Options opsi = new BitmapFactory.Options();
                    opsi.inSampleSize = 4; // pratinjau saja, hemat memori
                    Bitmap bmp = BitmapFactory.decodeFile(fileFoto.getAbsolutePath(), opsi);
                    ivFoto.setImageBitmap(bmp);
                    ivFoto.setVisibility(View.VISIBLE);
                    btnFoto.setText("Ambil ulang foto");
                } else {
                    fotoSudahDiambil = false;
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_laporan_form);

        etJudul = findViewById(R.id.etJudul);
        etDeskripsi = findViewById(R.id.etDeskripsi);
        etAlamat = findViewById(R.id.etAlamat);
        spKategori = findViewById(R.id.spKategori);
        tvLokasi = findViewById(R.id.tvLokasi);
        ivFoto = findViewById(R.id.ivFoto);
        btnLokasi = findViewById(R.id.btnLokasi);
        btnFoto = findViewById(R.id.btnFoto);
        btnKirim = findViewById(R.id.btnKirim);

        lokasiClient = LocationServices.getFusedLocationProviderClient(this);

        btnLokasi.setOnClickListener(v -> ambilLokasi());
        btnFoto.setOnClickListener(v -> bukaKamera());
        btnKirim.setOnClickListener(v -> kirimLaporan());
    }

    // ========================================================
    // GPS
    // ========================================================
    @SuppressLint("MissingPermission")
    private void ambilLokasi() {
        boolean punyaIzin = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        if (!punyaIzin) {
            izinLokasi.launch(Manifest.permission.ACCESS_FINE_LOCATION);
            return;
        }

        tvLokasi.setText("Mencari lokasi...");
        lokasiClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY,
                        new CancellationTokenSource().getToken())
                .addOnSuccessListener(lokasi -> {
                    if (lokasi == null) {
                        tvLokasi.setText("Lokasi tidak ditemukan. Nyalakan GPS lalu coba lagi.");
                        return;
                    }
                    lat = lokasi.getLatitude();
                    lng = lokasi.getLongitude();
                    tvLokasi.setText(String.format("Lokasi tersimpan: %.5f, %.5f", lat, lng));
                })
                .addOnFailureListener(e ->
                        tvLokasi.setText("Gagal mengambil lokasi: " + e.getMessage()));
    }

    // ========================================================
    // KAMERA
    // ========================================================
    private void bukaKamera() {
        try {
            fileFoto = File.createTempFile("laporan_", ".jpg", getCacheDir());
            Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", fileFoto);
            kamera.launch(uri);
        } catch (IOException e) {
            Toast.makeText(this, "Tidak bisa membuka kamera: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // ========================================================
    // KIRIM KE FIREBASE
    // ========================================================
    private void kirimLaporan() {
        String judul = etJudul.getText().toString().trim();
        String deskripsi = etDeskripsi.getText().toString().trim();

        if (TextUtils.isEmpty(judul)) {
            etJudul.setError("Judul wajib diisi");
            etJudul.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(deskripsi)) {
            etDeskripsi.setError("Deskripsi wajib diisi");
            etDeskripsi.requestFocus();
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Kamu belum masuk (login)", Toast.LENGTH_LONG).show();
            return;
        }

        btnKirim.setEnabled(false);
        btnKirim.setText("Mengirim...");

        // Ambil nama pelapor dari users/{uid}/nama (milik modul akun Rizal). Kalau tidak ada: "Warga".
        FirebaseDatabase.getInstance().getReference("users").child(user.getUid()).child("nama")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String nama = snapshot.getValue(String.class);
                        simpan(user.getUid(), nama == null ? "Warga" : nama, judul, deskripsi);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        simpan(user.getUid(), "Warga", judul, deskripsi);
                    }
                });
    }

    private void simpan(String uid, String nama, String judul, String deskripsi) {
        // Siapkan foto (jika ada)
        String fotoBase64 = null;
        if (fotoSudahDiambil && fileFoto != null) {
            try {
                fotoBase64 = FotoUtil.keBase64(fileFoto);
            } catch (Exception e) {
                Toast.makeText(this, "Foto gagal diproses: " + e.getMessage(), Toast.LENGTH_LONG).show();
                resetTombol();
                return;
            }
        }

        DatabaseReference root = FirebaseDatabase.getInstance().getReference();
        String idLaporan = root.child("laporan").push().getKey();
        if (idLaporan == null) {
            resetTombol();
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("uidPelapor", uid);
        data.put("namaPelapor", nama);
        data.put("judul", judul);
        data.put("deskripsi", deskripsi);
        data.put("kategori", spKategori.getSelectedItem().toString());
        data.put("alamatTeks", etAlamat.getText().toString().trim());
        if (lat != null && lng != null) {
            data.put("lat", lat);
            data.put("lng", lng);
        }
        data.put("status", "dilaporkan");
        data.put("dibuatPada", ServerValue.TIMESTAMP);
        data.put("diperbaruiPada", ServerValue.TIMESTAMP);

        // Simpan laporan + foto sekaligus (jika satu gagal, keduanya batal)
        Map<String, Object> semua = new HashMap<>();
        semua.put("laporan/" + idLaporan, data);
        if (fotoBase64 != null) {
            semua.put("laporanFoto/" + idLaporan, fotoBase64);
        }

        root.updateChildren(semua)
                .addOnSuccessListener(v -> {
                    Toast.makeText(this, "Laporan terkirim!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Gagal mengirim: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    resetTombol();
                });
    }

    private void resetTombol() {
        btnKirim.setEnabled(true);
        btnKirim.setText("Kirim laporan");
    }
}