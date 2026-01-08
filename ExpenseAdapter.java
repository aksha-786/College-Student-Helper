package com.example.studybuddy.ui.expense;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studybuddy.R;
import com.example.studybuddy.data.entity.ExpenseEntity;

import java.util.List;

public class ExpenseAdapter
        extends RecyclerView.Adapter<ExpenseAdapter.ViewHolder> {

    private final List<ExpenseEntity> list;

    // 🔹 Long click listener
    public interface OnItemLongClickListener {
        void onLongClick(ExpenseEntity expense);
    }

    private OnItemLongClickListener longClickListener;

    public void setOnLongClickListener(OnItemLongClickListener listener) {
        this.longClickListener = listener;
    }

    public ExpenseAdapter(List<ExpenseEntity> list) {
        this.list = list;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvExpenseTitle, tvExpenseAmount,
                tvExpenseCategory, tvExpenseDate;

        ViewHolder(View itemView) {
            super(itemView);
            tvExpenseTitle = itemView.findViewById(R.id.tvExpenseTitle);
            tvExpenseAmount = itemView.findViewById(R.id.tvExpenseAmount);
            tvExpenseCategory = itemView.findViewById(R.id.tvExpenseCategory);
            tvExpenseDate = itemView.findViewById(R.id.tvExpenseDate);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_expense, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder, int position) {

        ExpenseEntity e = list.get(position);

        holder.tvExpenseTitle.setText(e.title);
        holder.tvExpenseAmount.setText("₹ " + e.amount);
        holder.tvExpenseCategory.setText(e.category);
        holder.tvExpenseDate.setText(e.date);

        // 🔹 Long press
        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onLongClick(e);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
