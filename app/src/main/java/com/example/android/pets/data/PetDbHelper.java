package com.example.android.pets.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;

import com.example.android.pets.data.PetsContract.PetsEntry;

public class PetDbHelper extends SQLiteOpenHelper {
    // اسم قاعدة البيانات
    private final static String DATABASE_NAME = "shelter.db";
    // اصدار قاعدة البيانات
    private final static int DATABASE_VERSION = 1;

    public PetDbHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL_CREATE_PETS_TABLE =
                "CREATE TABLE " + PetsEntry.TABLE_NAME + " (" +
                        PetsEntry.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        PetsEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                        PetsEntry.COLUMN_BREED + " TEXT, " +
                        PetsEntry.COLUMN_GENDER + " INTEGER NOT NULL, " +
                        PetsEntry.COLUMN_WEIGHT + " INTEGER NOT NULL DEFAULT 0);";
        db.execSQL(SQL_CREATE_PETS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String SQL_DROP_PETS_TABLE = "DROP TABLE IF EXISTS " + PetsEntry.TABLE_NAME + ";";
        db.execSQL(SQL_DROP_PETS_TABLE);
        onCreate(db);
    }
}
