package com.example.studybuddy.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "exams")
public class ExamEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;
    public int userId;
    public String title;

    public long examDate; // millis (date + time)
}