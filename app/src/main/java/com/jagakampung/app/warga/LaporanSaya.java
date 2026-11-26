package com.jagakampung.app.warga;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jagakampung.app.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LaporanSaya extends AppCompatActivity {

    private final List<Laporan> daftar = new ArrayList<>();
    private LaporanAdapter adapter;
    private TextView tvKosong;
    private Query query;
    private ValueEventListener pendengar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_warga_laporan_saya);

        RecyclerView rv = findViewById(R.id.rvLaporan);
        tvKosong = findViewById(R.id.tvKosong);
        Button btnBuat = findViewById(R.id.btnBuat);

        adapter = new LaporanAdapter(daftar, laporan -> {
            Intent i = new Intent(this, DetailLaporan.class);
            i.putExtra("laporanId", laporan.id);
            startActivity(i);
        });
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        btnBuat.setOnClickListener(v -> startActivity(new Intent(this, LaporanForm.class)));

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Kamu belum masuk (login)", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Hanya ambil laporan milik user ini
        query = FirebaseDatabase.getInstance().getReference("laporan")
                .orderByChild("uidPelapor").equalTo(user.getUid());

        pendengar = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                daftar.clear();
                for (DataSnapshot s : snapshot.getChildren()) {
                    Laporan l = s.getValue(Laporan.class);
                    if (l != null) {
                        l.id = s.getKey();
                        daftar.add(l);
                    }
                }
                // Terbaru di atas
                Collections.sort(daftar, (a, b) -> {
                    long x = a.dibuatPada == null ? 0 : a.dibuatPada;
                    long y = b.dibuatPada == null ? 0 : b.dibuatPada;
                    return Long.compare(y, x);
                });
                adapter.notifyDataSetChanged();
                tvKosong.setVisibility(daftar.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(LaporanSaya.this,
                        "Gagal memuat: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        };
        query.addValueEventListener(pendengar);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (query != null && pendengar != null) {
            query.removeEventListener(pendengar);
        }
    }
}