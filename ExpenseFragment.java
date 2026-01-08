package com.example.studybuddy.ui.expense;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studybuddy.R;
import com.example.studybuddy.data.database.AppDatabase;
import com.example.studybuddy.data.entity.ExpenseEntity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ExpenseFragment extends Fragment {

    private AppDatabase db;
    private RecyclerView recyclerView;
    private TextView tvEmpty, tvTotalAmount;
    private int userId;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_expense, container, false);

        // Header
        ((TextView) view.findViewById(R.id.tvHeaderTitle))
                .setText("Expenses");

        // Get logged-in user
        SharedPreferences prefs =
                requireContext().getSharedPreferences(
                        "StudyBuddyPrefs", Context.MODE_PRIVATE);

        userId = prefs.getInt("user_id", -1);

        if (userId == -1) {
            Toast.makeText(requireContext(),
                    "Please login again",
                    Toast.LENGTH_SHORT).show();
            return view;
        }

        db = AppDatabase.getInstance(requireContext());

        recyclerView = view.findViewById(R.id.rvExpenses);
        tvEmpty = view.findViewById(R.id.tvEmptyExpenses);
        tvTotalAmount = view.findViewById(R.id.tvTotalAmount);
        FloatingActionButton fab = view.findViewById(R.id.fabAddExpense);

        recyclerView.setLayoutManager(
                new LinearLayoutManager(requireContext()));

        fab.setOnClickListener(v -> showExpenseDialog(null));

        loadExpenses();
        return view;
    }

    // ================= LOAD =================

    private void loadExpenses() {

        List<ExpenseEntity> list =
                db.expenseDao().getAll(userId);

        if (list.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            tvTotalAmount.setText("₹ 0.00");
            return;
        }

        tvEmpty.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);

        ExpenseAdapter adapter = new ExpenseAdapter(list);
        recyclerView.setAdapter(adapter);

        double total = 0;
        for (ExpenseEntity e : list) {
            total += e.amount;
        }

        tvTotalAmount.setText(
                String.format(Locale.getDefault(),
                        "₹ %.2f", total));

        adapter.setOnLongClickListener(expense ->
                showOptionsDialog(expense)
        );
    }

    // ================= OPTIONS =================

    private void showOptionsDialog(ExpenseEntity expense) {

        String[] options = {"Update", "Delete"};

        new AlertDialog.Builder(requireContext())
                .setTitle("Choose Action")
                .setItems(options, (d, which) -> {
                    if (which == 0) {
                        showExpenseDialog(expense);
                    } else {
                        confirmDelete(expense);
                    }
                })
                .show();
    }

    // ================= ADD / UPDATE =================

    private void showExpenseDialog(@Nullable ExpenseEntity originalExpense) {

        // ✅ Lambda-safe object
        final ExpenseEntity expense =
                (originalExpense == null)
                        ? new ExpenseEntity()
                        : originalExpense;

        View dialog = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_add_expense, null);

        EditText etTitle = dialog.findViewById(R.id.etTitle);
        EditText etAmount = dialog.findViewById(R.id.etAmount);
        EditText etCategory = dialog.findViewById(R.id.etCategory);
        TextView tvPickDate = dialog.findViewById(R.id.tvPickDate);

        Calendar calendar = Calendar.getInstance();

        // Prefill for UPDATE
        if (originalExpense != null) {
            etTitle.setText(expense.title);
            etAmount.setText(String.valueOf(expense.amount));
            etCategory.setText(expense.category);
            tvPickDate.setText(expense.date);

            try {
                calendar.setTime(
                        new SimpleDateFormat(
                                "dd MMM yyyy",
                                Locale.getDefault())
                                .parse(expense.date));
            } catch (Exception ignored) {}
        }

        // Date picker
        tvPickDate.setOnClickListener(v ->
                new DatePickerDialog(
                        requireContext(),
                        (dp, y, m, d) -> {
                            calendar.set(y, m, d);
                            tvPickDate.setText(
                                    new SimpleDateFormat(
                                            "dd MMM yyyy",
                                            Locale.getDefault())
                                            .format(calendar.getTime()));
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
        );

        new AlertDialog.Builder(requireContext())
                .setTitle(originalExpense == null
                        ? "Add Expense"
                        : "Edit Expense")
                .setView(dialog)
                .setPositiveButton("Save", (d, w) -> {

                    if (etTitle.getText().toString().trim().isEmpty()
                            || etAmount.getText().toString().trim().isEmpty()
                            || etCategory.getText().toString().trim().isEmpty()
                            || tvPickDate.getText().toString().contains("Pick")) {

                        Toast.makeText(
                                requireContext(),
                                "Fill all fields",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    expense.userId = userId;
                    expense.title = etTitle.getText().toString().trim();
                    expense.amount = Double.parseDouble(
                            etAmount.getText().toString().trim());
                    expense.category = etCategory.getText().toString().trim();
                    expense.date = tvPickDate.getText().toString();

                    if (originalExpense == null) {
                        db.expenseDao().insert(expense);
                    } else {
                        db.expenseDao().update(expense);
                    }

                    loadExpenses();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ================= DELETE =================

    private void confirmDelete(ExpenseEntity expense) {

        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Expense")
                .setMessage("Are you sure you want to delete this expense?")
                .setPositiveButton("Delete", (d, w) -> {
                    db.expenseDao().delete(expense);
                    loadExpenses();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
