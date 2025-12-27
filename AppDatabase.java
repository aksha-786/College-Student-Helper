package com.example.studybuddy.data.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.studybuddy.data.dao.ExamDao;
import com.example.studybuddy.data.dao.TimetableDao;
import com.example.studybuddy.data.dao.UserDao;
import com.example.studybuddy.data.dao.NoteDao;
import com.example.studybuddy.data.dao.AssignmentDao;
import com.example.studybuddy.data.dao.ExpenseDao;
import com.example.studybuddy.data.entity.ExamEntity;
import com.example.studybuddy.data.entity.TimetableEntity;
import com.example.studybuddy.data.entity.UserEntity;
import com.example.studybuddy.data.entity.NoteEntity;
import com.example.studybuddy.data.entity.AssignmentEntity;
import com.example.studybuddy.data.entity.ExpenseEntity;


@Database(
        entities = {
                UserEntity.class,
                TimetableEntity.class,
                NoteEntity.class,
                AssignmentEntity.class,
                ExpenseEntity.class,
                ExamEntity.class
        },
        version = 2,
        exportSchema = false
)

public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase instance;

    public abstract UserDao userDao();
    public abstract TimetableDao timetableDao();
    public abstract NoteDao noteDao();
    public abstract AssignmentDao assignmentDao();
    public abstract ExpenseDao expenseDao();
    public abstract ExamDao examDao();


    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                    context.getApplicationContext(),
                    AppDatabase.class,
                    "studybuddy_db"
            )
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build();
        }
        return instance;
    }
}

