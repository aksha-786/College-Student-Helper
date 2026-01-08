package com.example.studybuddy.ui.timetable;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studybuddy.R;
import com.example.studybuddy.data.entity.TimetableEntity;

import java.util.List;

public class TimetableAdapter extends RecyclerView.Adapter<TimetableAdapter.ViewHolder> {

    private final List<TimetableEntity> list;
    private OnItemLongClickListener listener;

    public interface OnItemLongClickListener {
        void onLongClick(TimetableEntity item);
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.listener = listener;
    }

    public TimetableAdapter(List<TimetableEntity> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_timetable, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder, int position) {

        TimetableEntity t = list.get(position);
        holder.tvSubject.setText(t.subject);
        holder.tvDayTime.setText(t.day + " • " + t.time);
        holder.tvRoom.setText("Room " + t.room);

        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) listener.onLongClick(t);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvSubject, tvDayTime, tvRoom;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSubject = itemView.findViewById(R.id.tvSubject);
            tvDayTime = itemView.findViewById(R.id.tvDayTime);
            tvRoom = itemView.findViewById(R.id.tvRoom);
        }
    }
}
