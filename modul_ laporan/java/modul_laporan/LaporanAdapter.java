package modul_laporan;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lastproject.R;

import java.util.List;

public class LaporanAdapter extends RecyclerView.Adapter<LaporanAdapter.VH> {

    public interface OnItemClick {
        void onClick(Laporan laporan);
    }

    private final List<Laporan> data;
    private final OnItemClick listener;

    public LaporanAdapter(List<Laporan> data, OnItemClick listener) {
        this.data = data;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_laporan, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Laporan l = data.get(position);
        h.tvJudul.setText(l.judul);
        h.tvInfo.setText((l.kategori == null ? "-" : l.kategori) + " • " + LaporanUtil.formatTanggal(l.dibuatPada));
        LaporanUtil.terapkanStatus(h.tvStatus, l.status);
        h.itemView.setOnClickListener(v -> listener.onClick(l));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvJudul, tvInfo, tvStatus;

        VH(@NonNull View itemView) {
            super(itemView);
            tvJudul = itemView.findViewById(R.id.tvJudul);
            tvInfo = itemView.findViewById(R.id.tvInfo);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
}