package com.example.studybuddy.ui.assignments;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studybuddy.R;
import com.example.studybuddy.data.entity.AssignmentEntity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AssignmentAdapter
        extends RecyclerView.Adapter<AssignmentAdapter.Holder> {

    // 🔹 Long-click interface
    public interface OnLongClick {
        void onLongClick(AssignmentEntity assignment);
    }

    private final List<AssignmentEntity> list;
    private final OnLongClick listener;

    // 🔹 FIXED constructor (matches fragment)
    public AssignmentAdapter(
            List<AssignmentEntity> list,
            OnLongClick listener) {

        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_assignment, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(
            @NonNull Holder h,
            int position) {

        AssignmentEntity a = list.get(position);

        // 🔹 Title
        h.tvTitle.setText(a.title);

        // 🔹 Date format
        SimpleDateFormat sdf =
                new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        h.tvDate.setText(sdf.format(new Date(a.dueDate)));

        // ✅ STATUS BADGE LOGIC (REQUESTED)
        h.tvStatus.setText(a.status);

        if ("Submitted".equalsIgnoreCase(a.status)) {
            h.tvStatus.setBackgroundResource(
                    R.drawable.bg_status_submitted);
        } else {
            h.tvStatus.setBackgroundResource(
                    R.drawable.bg_status_pending);
        }

        // 🔥 Long-press delete
        h.itemView.setOnLongClickListener(v -> {
            listener.onLongClick(a);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class Holder extends RecyclerView.ViewHolder {

        TextView tvTitle, tvStatus, tvDate;

        Holder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvDate = itemView.findViewById(R.id.tvDate);
        }
    }
}