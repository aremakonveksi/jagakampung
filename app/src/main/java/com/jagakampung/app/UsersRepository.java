package com.jagakampung.app;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class UsersRepository {

    public static final String PERAN_WARGA = "warga";
    public static final String STATUS_PENDING = "pending";
    public static final String STATUS_APPROVED = "approved";
    public static final String STATUS_REJECTED = "rejected";

    public interface HasilCallback {
        void onSelesai(boolean berhasil, String pesan);
    }

    public interface ProfilCallback {
        void onProfil(Profil profil);
        void onGagal(String pesan);
    }

    public static class Profil {
        public String uid;
        public String nama;
        public String email;
        public String rt;
        public String rw;
        public String peran;
        public String status;
        public String alasanTolak;

        public boolean sudahDisetujui() {
            return STATUS_APPROVED.equals(status);
        }

        public boolean ditolak() {
            return STATUS_REJECTED.equals(status);
        }
    }

    private final DatabaseReference usersRef;

    public UsersRepository() {
        usersRef = FirebaseDatabase.getInstance().getReference("users");
    }

    public void simpanProfilBaru(String uid, String nama, String email, String rt, String rw,
                                 HasilCallback callback) {
        Map<String, Object> data = new HashMap<>();
        data.put("nama", nama);
        data.put("email", email);
        data.put("rt", rt);
        data.put("rw", rw);
        data.put("peran", PERAN_WARGA);
        data.put("status", STATUS_PENDING);
        data.put("dibuatPada", ServerValue.TIMESTAMP);

        usersRef.child(uid).setValue(data)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSelesai(true, "Profil tersimpan");
                    } else {
                        String pesan = task.getException() == null
                                ? "Gagal menyimpan profil"
                                : task.getException().getMessage();
                        callback.onSelesai(false, pesan);
                    }
                });
    }

    public void bacaProfil(String uid, ProfilCallback callback) {
        usersRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    callback.onGagal("Profil belum tersedia");
                    return;
                }
                Profil profil = new Profil();
                profil.uid = uid;
                profil.nama = snapshot.child("nama").getValue(String.class);
                profil.email = snapshot.child("email").getValue(String.class);
                profil.rt = snapshot.child("rt").getValue(String.class);
                profil.rw = snapshot.child("rw").getValue(String.class);
                profil.peran = snapshot.child("peran").getValue(String.class);
                profil.status = snapshot.child("status").getValue(String.class);
                profil.alasanTolak = snapshot.child("alasanTolak").getValue(String.class);
                callback.onProfil(profil);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                callback.onGagal(error.getMessage());
            }
        });
    }
}
