package com.example.studybuddy.ui.notes;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studybuddy.R;
import com.example.studybuddy.data.entity.NoteEntity;

import java.util.List;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.ViewHolder> {

    private final List<NoteEntity> list;
    private OnNoteLongClickListener listener;

    public interface OnNoteLongClickListener {
        void onLongClick(NoteEntity note);
    }

    public void setOnNoteLongClickListener(OnNoteLongClickListener listener) {
        this.listener = listener;
    }

    public NoteAdapter(List<NoteEntity> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_note, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NoteEntity note = list.get(position);
        holder.tvTitle.setText(note.title);
        holder.tvContent.setText(note.content);

        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) listener.onLongClick(note);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvTitle, tvContent;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvContent = itemView.findViewById(R.id.tvContent);
        }
    }
}
