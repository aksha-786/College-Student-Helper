package com.example.studybuddy.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "expenses")
public class ExpenseEntity {

    @PrimaryKey(autoGenerate = true)
    public int expense_id;
    public int userId;
    public String title;
    public double amount;
    public String category;
    public String date;
}