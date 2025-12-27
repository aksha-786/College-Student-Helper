package com.example.studybuddy.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "assignments")
public class AssignmentEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;
    public int userId;
    public String title;

    // millis (used for sorting + notifications)
    public long dueDate;

    // "Pending" or "Submitted"
    public String status;
}