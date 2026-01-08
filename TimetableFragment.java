package com.example.studybuddy.ui.timetable;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studybuddy.R;
import com.example.studybuddy.data.database.AppDatabase;
import com.example.studybuddy.data.entity.TimetableEntity;

import java.util.Calendar;
import java.util.List;

public class TimetableFragment extends Fragment {

    private AppDatabase db;
    private RecyclerView recyclerView;
    private TimetableAdapter adapter;
    private int userId;

    // 🔹 TOP SPINNER (ONLY FOR VIEWING)
    private Spinner spinnerDay;
    private String selectedDay = "Monday";

    private final String[] days = {
            "Monday", "Tuesday", "Wednesday",
            "Thursday", "Friday", "Saturday", "Sunday"
    };

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_timetable, container, false);

        View header = view.findViewById(R.id.header);
        if (header != null) {
            TextView title = header.findViewById(R.id.tvHeaderTitle);
            title.setText("Class Timetable");
        }

        // 🔹 GET LOGGED-IN USER ID
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

        spinnerDay = view.findViewById(R.id.spinnerDay);
        recyclerView = view.findViewById(R.id.recyclerTimetable);
        Button btnAdd = view.findViewById(R.id.btnAddClass);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        setupTopDaySpinner();   // 🔥 VIEW FILTER
        loadData();

        btnAdd.setOnClickListener(v -> showAddDialog());

        return view;
    }

    // ================= TOP DAY SPINNER (VIEW ONLY) =================

    private void setupTopDaySpinner() {

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        requireContext(),
                        android.R.layout.simple_spinner_item,
                        days
                );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spinnerDay.setAdapter(adapter);

        spinnerDay.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(
                            AdapterView<?> parent,
                            View view,
                            int position,
                            long id) {

                        selectedDay = days[position];
                        loadData();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {}
                }
        );
    }

    // ================= LOAD DATA (FILTERED) =================

    private void loadData() {

        List<TimetableEntity> list =
                db.timetableDao().getByDay(userId, selectedDay);

        adapter = new TimetableAdapter(list);
        recyclerView.setAdapter(adapter);

        adapter.setOnItemLongClickListener(this::showOptionsDialog);
    }

    // ================= ADD CLASS =================

    private void showAddDialog() {

        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_add_timetable, null);

        EditText etSubject = dialogView.findViewById(R.id.etSubject);
        EditText etTime = dialogView.findViewById(R.id.etTime);
        EditText etRoom = dialogView.findViewById(R.id.etRoom);
        Spinner spinnerDialogDay = dialogView.findViewById(R.id.spinnerDialogDay);

        setupDialogDaySpinner(spinnerDialogDay, null);

        etTime.setFocusable(false);
        etTime.setOnClickListener(v -> openTimePicker(etTime));

        new AlertDialog.Builder(requireContext())
                .setTitle("Add Class")
                .setView(dialogView)
                .setPositiveButton("Save", (d, w) -> {

                    String subject = etSubject.getText().toString().trim();
                    String time = etTime.getText().toString().trim();
                    String room = etRoom.getText().toString().trim();
                    String chosenDay =
                            spinnerDialogDay.getSelectedItem().toString();

                    if (subject.isEmpty() || time.isEmpty()) {
                        Toast.makeText(requireContext(),
                                "Fill all fields",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    TimetableEntity t = new TimetableEntity();
                    t.userId = userId;
                    t.subject = subject;
                    t.day = chosenDay;     // ✅ USER-CHOSEN DAY
                    t.time = time;
                    t.room = room;

                    db.timetableDao().insert(t);
                    loadData();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ================= OPTIONS =================

    private void showOptionsDialog(TimetableEntity item) {

        String[] options = {"Update", "Delete"};

        new AlertDialog.Builder(requireContext())
                .setTitle("Choose Action")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        showEditDialog(item);
                    } else {
                        showDeleteConfirmation(item);
                    }
                })
                .show();
    }

    // ================= UPDATE =================

    private void showEditDialog(TimetableEntity item) {

        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_add_timetable, null);

        EditText etSubject = dialogView.findViewById(R.id.etSubject);
        EditText etTime = dialogView.findViewById(R.id.etTime);
        EditText etRoom = dialogView.findViewById(R.id.etRoom);
        Spinner spinnerDialogDay = dialogView.findViewById(R.id.spinnerDialogDay);

        etSubject.setText(item.subject);
        etTime.setText(item.time);
        etRoom.setText(item.room);

        setupDialogDaySpinner(spinnerDialogDay, item.day);

        etTime.setOnClickListener(v -> openTimePicker(etTime));

        new AlertDialog.Builder(requireContext())
                .setTitle("Edit Class")
                .setView(dialogView)
                .setPositiveButton("Update", (d, w) -> {

                    item.subject = etSubject.getText().toString().trim();
                    item.time = etTime.getText().toString().trim();
                    item.room = etRoom.getText().toString().trim();
                    item.day =
                            spinnerDialogDay.getSelectedItem().toString();

                    db.timetableDao().update(item);
                    loadData();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ================= DELETE =================

    private void showDeleteConfirmation(TimetableEntity item) {

        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Class")
                .setMessage("Delete " + item.subject + "?")
                .setPositiveButton("Delete", (d, w) -> {
                    db.timetableDao().delete(item);
                    loadData();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ================= DIALOG DAY SPINNER =================

    private void setupDialogDaySpinner(Spinner spinner, String preselectDay) {

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        requireContext(),
                        android.R.layout.simple_spinner_item,
                        days
                );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spinner.setAdapter(adapter);

        if (preselectDay != null) {
            for (int i = 0; i < days.length; i++) {
                if (days[i].equals(preselectDay)) {
                    spinner.setSelection(i);
                    break;
                }
            }
        }
    }

    // ================= TIME PICKER =================

    private void openTimePicker(EditText etTime) {

        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        new TimePickerDialog(
                requireContext(),
                (view, h, m) -> {

                    String amPm;
                    int displayHour = h;

                    if (h >= 12) {
                        amPm = "PM";
                        if (h > 12) displayHour -= 12;
                    } else {
                        amPm = "AM";
                        if (h == 0) displayHour = 12;
                    }

                    etTime.setText(
                            String.format("%02d:%02d %s", displayHour, m, amPm)
                    );
                },
                hour,
                minute,
                false
        ).show();
    }
}