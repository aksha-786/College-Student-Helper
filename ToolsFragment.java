package com.example.studybuddy.ui.tools;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.studybuddy.LoginActivity;
import com.example.studybuddy.R;

public class ToolsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_tools, container, false);

        // ✅ HEADER TITLE

        View header = view.findViewById(R.id.header);
        if (header != null) {
            TextView title = header.findViewById(R.id.tvHeaderTitle);
            title.setText("Tools / Setting");
        }

        TextView tvAbout = view.findViewById(R.id.tvAbout);
        TextView tvLogout = view.findViewById(R.id.tvLogout);

        // ABOUT
        tvAbout.setOnClickListener(v ->
                new AlertDialog.Builder(requireContext())
                        .setTitle("About StudyBuddy")
                        .setMessage(
                                "StudyBuddy is your all-in-one academic companion 📚✨\n\n" +
                                        "Plan your timetable, track assignments, manage notes, " +
                                        "and stay focused with smart study tools — all in one place.\n\n" +
                                        "Designed to make student life simpler, smarter, and stress-free 💜"
                        )

                        .setPositiveButton("OK", null)
                        .show()
        );

        // LOGOUT
        tvLogout.setOnClickListener(v ->
                new AlertDialog.Builder(requireContext())
                        .setTitle("Logout")
                        .setMessage("Do you want to logout?")
                        .setPositiveButton("Yes", (d, w) -> {
                            requireActivity()
                                    .getSharedPreferences("StudyBuddyPrefs", 0)
                                    .edit()
                                    .clear()
                                    .apply();

                            startActivity(new Intent(requireContext(), LoginActivity.class));
                            requireActivity().finish();
                        })
                        .setNegativeButton("Cancel", null)
                        .show()
        );

        return view;
    }
}
