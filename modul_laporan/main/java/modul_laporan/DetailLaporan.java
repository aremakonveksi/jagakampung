package modul_laporan;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.lastproject.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DetailLaporan extends AppCompatActivity {

    private TextView tvJudul, tvStatus, tvInfo, tvDeskripsi, tvLokasi, tvFotoKosong;
    private ImageView ivFoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_warga_detail_laporan);

        tvJudul = findViewById(R.id.tvJudul);
        tvStatus = findViewById(R.id.tvStatus);
        tvInfo = findViewById(R.id.tvInfo);
        tvDeskripsi = findViewById(R.id.tvDeskripsi);
        tvLokasi = findViewById(R.id.tvLokasi);
        tvFotoKosong = findViewById(R.id.tvFotoKosong);
        ivFoto = findViewById(R.id.ivFoto);
        Button btnKembali = findViewById(R.id.btnKembali);
        btnKembali.setOnClickListener(v -> finish());

        String id = getIntent().getStringExtra("laporanId");
        if (id == null) {
            Toast.makeText(this, "Laporan tidak ditemukan", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        DatabaseReference root = FirebaseDatabase.getInstance().getReference();

        // 1. Data teks laporan
        root.child("laporan").child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Laporan l = snapshot.getValue(Laporan.class);
                if (l == null) {
                    Toast.makeText(DetailLaporan.this, "Data kosong", Toast.LENGTH_SHORT).show();
                    return;
                }
                tvJudul.setText(l.judul);
                LaporanUtil.terapkanStatus(tvStatus, l.status);
                tvInfo.setText((l.kategori == null ? "-" : l.kategori)
                        + " • " + LaporanUtil.formatTanggal(l.dibuatPada));
                tvDeskripsi.setText(l.deskripsi);

                StringBuilder lokasi = new StringBuilder();
                if (l.alamatTeks != null && !l.alamatTeks.isEmpty()) lokasi.append(l.alamatTeks);
                if (l.lat != null && l.lng != null) {
                    if (lokasi.length() > 0) lokasi.append("\n");
                    lokasi.append(String.format("Koordinat: %.5f, %.5f", l.lat, l.lng));
                }
                tvLokasi.setText(lokasi.length() == 0 ? "Lokasi tidak dicantumkan" : lokasi.toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DetailLaporan.this, "Gagal memuat: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        // 2. Foto (Base64 -> gambar)
        root.child("laporanFoto").child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String base64 = snapshot.getValue(String.class);
                if (base64 == null || base64.isEmpty()) {
                    tvFotoKosong.setVisibility(View.VISIBLE);
                    return;
                }
                try {
                    byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
                    Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    ivFoto.setImageBitmap(bmp);
                    ivFoto.setVisibility(View.VISIBLE);
                } catch (Exception e) {
                    tvFotoKosong.setText("Foto gagal ditampilkan");
                    tvFotoKosong.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                tvFotoKosong.setText("Foto tidak bisa dimuat");
                tvFotoKosong.setVisibility(View.VISIBLE);
            }
        });
    }
}