package com.example.studybuddy.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "notes")
public class NoteEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;
    public int userId;
    public String title;
    public String content;
    public long createdAt;
}
