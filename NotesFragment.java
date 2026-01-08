package com.example.studybuddy.ui.notes;

import android.app.AlertDialog;
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
import com.example.studybuddy.data.entity.NoteEntity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class NotesFragment extends Fragment {

    private AppDatabase db;
    private RecyclerView rvNotes;
    private TextView tvEmpty;
    private int userId;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_notes, container, false);

        View header = view.findViewById(R.id.header);
        if (header != null) {
            ((TextView) header.findViewById(R.id.tvHeaderTitle))
                    .setText("My Notes");
        }

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

        rvNotes = view.findViewById(R.id.rvNotes);
        tvEmpty = view.findViewById(R.id.tvEmptyNotes);
        FloatingActionButton fab = view.findViewById(R.id.fabAddNote);

        rvNotes.setLayoutManager(
                new LinearLayoutManager(requireContext()));

        loadNotes();

        fab.setOnClickListener(v -> showAddDialog());

        return view;
    }

    // ================= LOAD NOTES =================

    private void loadNotes() {

        List<NoteEntity> list =
                db.noteDao().getAllNotes(userId);

        if (list.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            rvNotes.setVisibility(View.GONE);
            return;
        }

        tvEmpty.setVisibility(View.GONE);
        rvNotes.setVisibility(View.VISIBLE);

        NoteAdapter adapter = new NoteAdapter(list);
        rvNotes.setAdapter(adapter);

        adapter.setOnNoteLongClickListener(this::showOptionsDialog);
    }

    // ================= ADD NOTE =================

    private void showAddDialog() {

        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_add_note, null);

        EditText etTitle = dialogView.findViewById(R.id.etNoteTitle);
        EditText etContent = dialogView.findViewById(R.id.etNoteContent);

        new AlertDialog.Builder(requireContext())
                .setTitle("Add Note")
                .setView(dialogView)
                .setPositiveButton("Save", (d, w) -> {

                    String title = etTitle.getText().toString().trim();
                    String content = etContent.getText().toString().trim();

                    if (title.isEmpty() || content.isEmpty()) {
                        Toast.makeText(requireContext(),
                                "Fill all fields",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    NoteEntity note = new NoteEntity();
                    note.userId = userId;
                    note.title = title;
                    note.content = content;
                    note.createdAt = System.currentTimeMillis();

                    db.noteDao().insert(note);
                    loadNotes();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ================= OPTIONS (UPDATE / DELETE) =================

    private void showOptionsDialog(NoteEntity note) {

        String[] options = {"Update", "Delete"};

        new AlertDialog.Builder(requireContext())
                .setTitle("Choose Action")
                .setItems(options, (dialog, which) -> {

                    if (which == 0) {
                        showEditDialog(note);
                    } else {
                        showDeleteConfirmation(note);
                    }
                })
                .show();
    }

    // ================= UPDATE =================

    private void showEditDialog(NoteEntity note) {

        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_add_note, null);

        EditText etTitle = dialogView.findViewById(R.id.etNoteTitle);
        EditText etContent = dialogView.findViewById(R.id.etNoteContent);

        etTitle.setText(note.title);
        etContent.setText(note.content);

        new AlertDialog.Builder(requireContext())
                .setTitle("Edit Note")
                .setView(dialogView)
                .setPositiveButton("Update", (d, w) -> {

                    String title = etTitle.getText().toString().trim();
                    String content = etContent.getText().toString().trim();

                    if (title.isEmpty() || content.isEmpty()) {
                        Toast.makeText(requireContext(),
                                "Fill all fields",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    note.title = title;
                    note.content = content;

                    db.noteDao().update(note);
                    loadNotes();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ================= DELETE =================

    private void showDeleteConfirmation(NoteEntity note) {

        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Note")
                .setMessage("Are you sure you want to delete this note?")
                .setPositiveButton("Delete", (d, w) -> {
                    db.noteDao().delete(note);
                    loadNotes();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
