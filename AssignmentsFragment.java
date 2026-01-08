package com.example.studybuddy.ui.assignments;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studybuddy.R;
import com.example.studybuddy.data.database.AppDatabase;
import com.example.studybuddy.data.entity.AssignmentEntity;
import com.example.studybuddy.ui.examcountdown.NotificationReceiver;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class AssignmentsFragment extends Fragment {

    private AppDatabase db;
    private RecyclerView recyclerView;
    private TextView tvEmpty;
    private int userId;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_assignments, container, false);

        ((TextView) view.findViewById(R.id.tvHeaderTitle))
                .setText("Assignments");

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

        recyclerView = view.findViewById(R.id.rvAssignments);
        tvEmpty = view.findViewById(R.id.tvEmpty);

        recyclerView.setLayoutManager(
                new LinearLayoutManager(requireContext()));

        FloatingActionButton fab =
                view.findViewById(R.id.fabAddAssignment);

        fab.setOnClickListener(v -> showAddDialog());

        loadAssignments();
        return view;
    }

    // ================= LOAD ASSIGNMENTS =================

    private void loadAssignments() {

        List<AssignmentEntity> list =
                db.assignmentDao().getAll(userId);

        if (list.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            return;
        }

        tvEmpty.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);

        recyclerView.setAdapter(
                new AssignmentAdapter(list, this::showOptionsDialog)
        );
    }

    // ================= OPTIONS (UPDATE / DELETE) =================

    private void showOptionsDialog(AssignmentEntity assignment) {

        String[] options = {"Update", "Delete"};

        new AlertDialog.Builder(requireContext())
                .setTitle("Choose Action")
                .setItems(options, (d, which) -> {
                    if (which == 0) {
                        showEditDialog(assignment);
                    } else {
                        showDeleteConfirmation(assignment);
                    }
                })
                .show();
    }

    // ================= ADD ASSIGNMENT =================

    private void showAddDialog() {

        View dialog = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_add_assignment, null);

        EditText etTitle = dialog.findViewById(R.id.etTitle);
        TextView tvPickDate = dialog.findViewById(R.id.tvPickDate);
        RadioGroup rgStatus = dialog.findViewById(R.id.rgStatus);

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        tvPickDate.setOnClickListener(v ->
                openDatePicker(cal, tvPickDate)
        );

        new AlertDialog.Builder(requireContext())
                .setTitle("Add Assignment")
                .setView(dialog)
                .setPositiveButton("Save", (d, w) -> {

                    String title = etTitle.getText().toString().trim();

                    if (!validateAssignment(title, tvPickDate, rgStatus, cal))
                        return;

                    AssignmentEntity a = buildAssignment(
                            null, title, cal, rgStatus);

                    db.assignmentDao().insert(a);

                    if ("Pending".equals(a.status)) {
                        scheduleAssignmentNotification(a.dueDate, a.title);
                    }

                    loadAssignments();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ================= UPDATE ASSIGNMENT =================

    private void showEditDialog(AssignmentEntity assignment) {

        View dialog = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_add_assignment, null);

        EditText etTitle = dialog.findViewById(R.id.etTitle);
        TextView tvPickDate = dialog.findViewById(R.id.tvPickDate);
        RadioGroup rgStatus = dialog.findViewById(R.id.rgStatus);

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(assignment.dueDate);

        etTitle.setText(assignment.title);
        tvPickDate.setText(formatDate(cal));

        if ("Submitted".equalsIgnoreCase(assignment.status)) {
            ((RadioButton) dialog.findViewById(R.id.rbSubmitted))
                    .setChecked(true);
        } else {
            ((RadioButton) dialog.findViewById(R.id.rbPending))
                    .setChecked(true);
        }

        tvPickDate.setOnClickListener(v ->
                openDatePicker(cal, tvPickDate)
        );

        new AlertDialog.Builder(requireContext())
                .setTitle("Edit Assignment")
                .setView(dialog)
                .setPositiveButton("Update", (d, w) -> {

                    String title = etTitle.getText().toString().trim();

                    if (!validateAssignment(title, tvPickDate, rgStatus, cal))
                        return;

                    assignment.title = title;
                    assignment.dueDate = cal.getTimeInMillis();
                    assignment.status =
                            rgStatus.getCheckedRadioButtonId()
                                    == R.id.rbSubmitted
                                    ? "Submitted"
                                    : "Pending";

                    db.assignmentDao().update(assignment);
                    loadAssignments();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ================= DELETE =================

    private void showDeleteConfirmation(AssignmentEntity assignment) {

        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Assignment")
                .setMessage("Are you sure you want to delete this assignment?")
                .setPositiveButton("Delete", (d, w) -> {
                    db.assignmentDao().delete(assignment);
                    loadAssignments();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ================= HELPERS =================

    private boolean validateAssignment(
            String title,
            TextView tvPickDate,
            RadioGroup rgStatus,
            Calendar cal) {

        if (title.isEmpty()) {
            Toast.makeText(requireContext(),
                    "Enter assignment title",
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        if (tvPickDate.getText().toString().contains("Pick")) {
            Toast.makeText(requireContext(),
                    "Pick due date",
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        if (rgStatus.getCheckedRadioButtonId() == -1) {
            Toast.makeText(requireContext(),
                    "Select assignment status",
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        if (cal.getTimeInMillis() <= System.currentTimeMillis()) {
            Toast.makeText(requireContext(),
                    "Due date must be in future",
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private AssignmentEntity buildAssignment(
            AssignmentEntity a,
            String title,
            Calendar cal,
            RadioGroup rgStatus) {

        if (a == null) a = new AssignmentEntity();

        a.userId = userId;
        a.title = title;
        a.dueDate = cal.getTimeInMillis();
        a.status =
                rgStatus.getCheckedRadioButtonId()
                        == R.id.rbSubmitted
                        ? "Submitted"
                        : "Pending";

        return a;
    }

    private void openDatePicker(Calendar cal, TextView tv) {

        new DatePickerDialog(
                requireContext(),
                (dp, y, m, d) -> {
                    cal.set(y, m, d, 0, 0);
                    tv.setText(formatDate(cal));
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private String formatDate(Calendar cal) {
        return cal.get(Calendar.DAY_OF_MONTH) + "/"
                + (cal.get(Calendar.MONTH) + 1) + "/"
                + cal.get(Calendar.YEAR);
    }

    // ================= NOTIFICATIONS =================

    private void scheduleAssignmentNotification(long dueTime, String title) {

        AlarmManager alarmManager =
                (AlarmManager) requireContext()
                        .getSystemService(Context.ALARM_SERVICE);

        if (alarmManager == null) return;

        long oneDayBefore = dueTime - TimeUnit.DAYS.toMillis(1);

        if (oneDayBefore <= System.currentTimeMillis()) return;

        Intent intent = new Intent(requireContext(), NotificationReceiver.class);
        intent.putExtra("title", "Assignment Due Tomorrow");
        intent.putExtra("message", title + " is due tomorrow!");

        PendingIntent pi = PendingIntent.getBroadcast(
                requireContext(),
                (int) System.currentTimeMillis(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                oneDayBefore,
                pi
        );
    }
}
