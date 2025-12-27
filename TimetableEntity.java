package com.example.studybuddy.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "timetable")
public class TimetableEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;
    public int userId;
    public String subject;
    public String day;
    public String time;
    public String room;
}
