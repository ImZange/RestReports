package com.example.restreports.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.restreports.R;
import com.example.restreports.model.Report;

import java.util.ArrayList;
import java.util.List;

/**
 * Shows the reports as cards. Risk is communicated with TEXT and colour
 * (never colour alone) and the chip colours keep a contrast ratio above 4.5:1 (WCAG AA).
 */
public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.VH> {

    public interface Listener {
        void onEdit(Report report);

        void onDelete(Report report);
    }

    private final List<Report> items = new ArrayList<>();
    private final Listener listener;

    public ReportAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setItems(List<Report> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    public void addFirst(Report report) {
        items.add(0, report);
        notifyItemInserted(0);
    }

    public void replace(Report oldReport, Report newReport) {
        int index = items.indexOf(oldReport);   // identity comparison (no equals override)
        if (index >= 0) {
            items.set(index, newReport);
            notifyItemChanged(index);
        }
    }

    public void remove(Report report) {
        int index = items.indexOf(report);
        if (index >= 0) {
            items.remove(index);
            notifyItemRemoved(index);
        }
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_report, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        final Report r = items.get(position);
        Context ctx = h.itemView.getContext();
        String risk = r.getRiskLevel();

        h.tvId.setText(ctx.getString(R.string.report_id, r.getId()));
        h.tvMunicipality.setText(r.getMunicipality());
        h.tvSummary.setText(r.getSymptomSummary().replace('\n', ' '));

        h.tvRisk.setText(ctx.getString(R.string.risk_chip, risk));
        h.tvRisk.getBackground().mutate().setTint(ContextCompat.getColor(ctx, colorFor(risk)));

        h.btnDelete.setContentDescription(ctx.getString(R.string.cd_delete_report, r.getId()));
        h.btnDelete.setOnClickListener(v -> listener.onDelete(r));
        h.itemView.setOnClickListener(v -> listener.onEdit(r));
    }

    private int colorFor(String risk) {
        if (Report.RISK_HIGH.equalsIgnoreCase(risk)) return R.color.risk_high;
        if (Report.RISK_MEDIUM.equalsIgnoreCase(risk)) return R.color.risk_medium;
        if (Report.RISK_LOW.equalsIgnoreCase(risk)) return R.color.risk_low;
        return R.color.risk_unknown;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView tvId, tvRisk, tvMunicipality, tvSummary;
        final ImageButton btnDelete;

        VH(@NonNull View itemView) {
            super(itemView);
            tvId = itemView.findViewById(R.id.tvReportId);
            tvRisk = itemView.findViewById(R.id.tvRisk);
            tvMunicipality = itemView.findViewById(R.id.tvMunicipality);
            tvSummary = itemView.findViewById(R.id.tvSummary);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
