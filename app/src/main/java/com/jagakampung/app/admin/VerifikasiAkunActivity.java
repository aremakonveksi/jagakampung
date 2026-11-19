package com.jagakampung.app.admin;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jagakampung.app.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VerifikasiAkunActivity extends Activity {

    // Warna sesuai desain Rizal (colors.xml Gaya A)
    private static final int WARNA_PENDING = Color.parseColor("#FBB315");
    private static final int WARNA_SETUJU = Color.parseColor("#10766B");
    private static final int WARNA_TOLAK = Color.parseColor("#E5484D");

    // Satu akun warga (field sama dengan node "users" di Firebase)
    static class Akun {
        String uid, nama, email, rt, rw;

        Akun(String uid, String nama, String email, String rt, String rw) {
            this.uid = uid;
            this.nama = nama;
            this.email = email;
            this.rt = rt;
            this.rw = rw;
        }
    }

    // Hanya akun yang masih pending yang ditampilkan
    private final List<Akun> pending = new ArrayList<>();
    private AkunAdapter adapter;
    private DatabaseReference usersRef;
    private ValueEventListener listener;
    private boolean sudahMemuat = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verifikasi_akun);

        usersRef = FirebaseDatabase.getInstance().getReference("users");

        ListView list = findViewById(R.id.listAkun);
        adapter = new AkunAdapter();
        list.setAdapter(adapter);

        ((TextView) findViewById(R.id.tvJumlah)).setText("Memuat data...");
    }

    // ===== MEMBACA DATA DARI FIREBASE =====

    @Override
    protected void onStart() {
        super.onStart();
        // Listener ini otomatis dipanggil lagi setiap data "users" berubah,
        // jadi akun yang baru disetujui/ditolak langsung hilang dari daftar.
        listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                pending.clear();
                for (DataSnapshot anak : snapshot.getChildren()) {
                    String status = anak.child("status").getValue(String.class);
                    if (!"pending".equals(status)) continue;
                    pending.add(new Akun(
                            anak.getKey(),
                            teks(anak, "nama"),
                            teks(anak, "email"),
                            teks(anak, "rt"),
                            teks(anak, "rw")));
                }
                sudahMemuat = true;
                perbaruiTampilan();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                ((TextView) findViewById(R.id.tvJumlah))
                        .setText("Gagal memuat: " + error.getMessage());
            }
        };
        usersRef.addValueEventListener(listener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (listener != null) usersRef.removeEventListener(listener);
    }

    private String teks(DataSnapshot induk, String kunci) {
        String nilai = induk.child(kunci).getValue(String.class);
        return nilai == null ? "-" : nilai;
    }

    // Jumlah akun menunggu + teks kosong
    private void perbaruiTampilan() {
        TextView tvJumlah = findViewById(R.id.tvJumlah);
        if (pending.isEmpty()) {
            tvJumlah.setText("Semua pendaftaran sudah diproses");
        } else {
            tvJumlah.setText(pending.size() + " akun menunggu verifikasi");
        }
        findViewById(R.id.tvKosong).setVisibility(
                sudahMemuat && pending.isEmpty() ? View.VISIBLE : View.GONE);
        adapter.notifyDataSetChanged();
    }

    // ===== SETUJUI =====

    private void setujui(final Akun a) {
        // Menulis ke Firebase: users/{uid}/status = "approved"
        usersRef.child(a.uid).child("status").setValue("approved")
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Akun " + a.nama + " disetujui",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        gagal(task.getException());
                    }
                });
    }

    // ===== TOLAK (dengan alasan) =====

    private void tolak(final Akun a) {
        final EditText etAlasan = new EditText(this);
        etAlasan.setHint("Contoh: data RT/RW tidak sesuai");
        etAlasan.setTextColor(Color.parseColor("#444853"));
        etAlasan.setHintTextColor(Color.parseColor("#9E9E9E"));
        etAlasan.setInputType(InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_FLAG_MULTI_LINE
                | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        etAlasan.setMinLines(2);
        etAlasan.setGravity(Gravity.TOP | Gravity.START);

        FrameLayout wadah = new FrameLayout(this);
        wadah.setPadding(dp(20), dp(8), dp(20), 0);
        wadah.addView(etAlasan);

        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Tolak " + a.nama + "?")
                .setMessage("Tulis alasan penolakan. Alasan ini akan tersimpan di akun.")
                .setView(wadah)
                .setPositiveButton("Tolak", null)   // diatur di bawah supaya bisa divalidasi
                .setNegativeButton("Batal", null)
                .create();
        dialog.show();

        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(WARNA_TOLAK);
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v -> {
            String alasan = etAlasan.getText().toString().trim();
            if (alasan.isEmpty()) {
                etAlasan.setError("Alasan wajib diisi");
                return;
            }
            // Menulis ke Firebase: status = "rejected" dan alasanTolak = alasan,
            // dalam satu perintah (berhasil semua atau gagal semua).
            Map<String, Object> ubah = new HashMap<>();
            ubah.put("status", "rejected");
            ubah.put("alasanTolak", alasan);
            usersRef.child(a.uid).updateChildren(ubah)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Akun " + a.nama + " ditolak",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            gagal(task.getException());
                        }
                    });
            dialog.dismiss();
        });
    }

    private void gagal(Exception e) {
        String pesan = e == null ? "Gagal menyimpan" : e.getMessage();
        Toast.makeText(this, "Gagal: " + pesan, Toast.LENGTH_LONG).show();
    }

    private int dp(int nilai) {
        return Math.round(nilai * getResources().getDisplayMetrics().density);
    }

    // Adapter: mengisi satu kartu dengan satu akun
    private class AkunAdapter extends BaseAdapter {
        @Override public int getCount() { return pending.size(); }
        @Override public Object getItem(int i) { return pending.get(i); }
        @Override public long getItemId(int i) { return i; }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(VerifikasiAkunActivity.this)
                        .inflate(R.layout.item_akun, parent, false);
            }
            final Akun a = pending.get(position);

            ((TextView) convertView.findViewById(R.id.tvNama)).setText(a.nama);
            ((TextView) convertView.findViewById(R.id.tvEmail)).setText(a.email);
            ((TextView) convertView.findViewById(R.id.tvRtRw))
                    .setText("RT " + a.rt + " / RW " + a.rw);

            // Label "Menunggu" berwarna kuning
            TextView tvStatus = convertView.findViewById(R.id.tvStatusAkun);
            tvStatus.setTextColor(Color.parseColor("#9A6B00"));
            GradientDrawable chip = new GradientDrawable();
            chip.setCornerRadius(dp(8));
            chip.setColor(Color.argb(46, Color.red(WARNA_PENDING),
                    Color.green(WARNA_PENDING), Color.blue(WARNA_PENDING)));
            tvStatus.setBackground(chip);

            // Tombol Setujui: hijau penuh
            Button btnSetujui = convertView.findViewById(R.id.btnSetujui);
            GradientDrawable bgSetuju = new GradientDrawable();
            bgSetuju.setCornerRadius(dp(12));
            bgSetuju.setColor(WARNA_SETUJU);
            btnSetujui.setBackground(bgSetuju);
            btnSetujui.setOnClickListener(v -> setujui(a));

            // Tombol Tolak: garis merah
            Button btnTolak = convertView.findViewById(R.id.btnTolak);
            GradientDrawable bgTolak = new GradientDrawable();
            bgTolak.setCornerRadius(dp(12));
            bgTolak.setColor(Color.WHITE);
            bgTolak.setStroke(dp(1), WARNA_TOLAK);
            btnTolak.setBackground(bgTolak);
            btnTolak.setTextColor(WARNA_TOLAK);
            btnTolak.setOnClickListener(v -> tolak(a));

            return convertView;
        }
    }
}
