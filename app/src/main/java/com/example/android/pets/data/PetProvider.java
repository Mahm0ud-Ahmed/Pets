package com.example.android.pets.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

public class PetProvider extends ContentProvider {
    // كائن uri Matcher يساعدنا في تحديد مسار ال query داخل قاعدة البيانات
    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    //الثابت الأول لمسار ال query الخاص بالجدول بأكمله
    private static final int PETS = 100;
    //الثابت الثاني لمسار ال query وهو خاص بعنصر داخل الجدول
    private static final int PETS_ID = 101;

    static {
        uriMatcher.addURI(PetsContract.AUTHORITIES, PetsContract.PATH_TABLE, PETS);
        uriMatcher.addURI(PetsContract.AUTHORITIES, PetsContract.PATH_TABLE + "/#", PETS_ID);
    }

    private PetDbHelper dbHelper;
    private SQLiteDatabase db;

    @Override
    public boolean onCreate() {
        dbHelper = new PetDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        db = dbHelper.getReadableDatabase();
        Cursor cursor;
        int match = uriMatcher.match(uri);
        switch (match) {
            case PETS:
                // content://com.example.android.pets/pets
                cursor = db.query(PetsContract.PetsEntry.TABLE_NAME, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;
            case PETS_ID:
                // content://com.example.android.pets/pets/5
                selection = PetsContract.PetsEntry.COLUMN_ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = db.query(PetsContract.PetsEntry.TABLE_NAME, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query Unknown Uri " + uri);
        }
        // ارسال اشعال لل Content Resolver بتغيير حدث داخل قاعدة البيانات، ليتم اشغار ال CursorLoader لتحميل البيانات من جديد
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch (uriMatcher.match(uri)) {
            case PETS:
                return PetsContract.PetsEntry.CONTENT_LIST_TYPE;
            case PETS_ID:
                return PetsContract.PetsEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Cannot query Unknown Uri " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        final int match = uriMatcher.match(uri);
        if (match == PETS) {
            return insertPet(uri, values);
        }
        throw new IllegalArgumentException("Cannot query Unknown Uri " + uri);
    }

    private Uri insertPet(Uri uri, ContentValues values) {
        String name = values.getAsString(PetsContract.PetsEntry.COLUMN_NAME);
        if (TextUtils.isEmpty(name)) {
            return null;
        }

        Integer gender = values.getAsInteger(PetsContract.PetsEntry.COLUMN_GENDER);
        if (gender == null || !isValidGender(gender)) {
            return null;
        }

        Integer weight = values.getAsInteger(PetsContract.PetsEntry.COLUMN_WEIGHT);
        if (weight == null || weight < 0) {
            return null;
        }

        db = dbHelper.getWritableDatabase();
        long id = db.insert(PetsContract.PetsEntry.TABLE_NAME, null, values);
        if (id == -1) {
            return null;
        }
        //الابلاغ عن حدوث تغيير حدث داخل قاعدة البيانات ليتم تحديثها
        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, id);
    }

    private boolean isValidGender(int gender) {
        return gender == PetsContract.PetsEntry.UNKNOWN || gender == PetsContract.PetsEntry.MALE ||
                gender == PetsContract.PetsEntry.FEMALE;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        db = dbHelper.getWritableDatabase();
        int result;
        switch (uriMatcher.match(uri)) {
            case PETS:
                result = db.delete(PetsContract.PetsEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case PETS_ID:
                selection = PetsContract.PetsEntry.COLUMN_ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                result = db.delete(PetsContract.PetsEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Cannot query Unknown Uri " + uri);
        }
        if (result != 0) {
            //الابلاغ عن تغيير حدث داخل قاعدة البيانات ليتم تحديثها
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return result;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        final int match = uriMatcher.match(uri);
        switch (match) {
            case PETS:
                return updatePet(uri, values, selection, selectionArgs);
            case PETS_ID:
                selection = PetsContract.PetsEntry.COLUMN_ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updatePet(uri, values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Cannot query Unknown Uri " + uri);
        }
    }

    private int updatePet(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if (values.containsKey(PetsContract.PetsEntry.COLUMN_NAME)) {
            String name = values.getAsString(PetsContract.PetsEntry.COLUMN_NAME);
            if (name.isEmpty()) {
                return 0;
            }
        }

        if (values.containsKey(PetsContract.PetsEntry.COLUMN_GENDER)) {
            Integer gender = values.getAsInteger(PetsContract.PetsEntry.COLUMN_GENDER);
            if (gender == null || !isValidGender(gender)) {
                return 0;
            }
        }

        if (values.containsKey(PetsContract.PetsEntry.COLUMN_WEIGHT)) {
            Integer weight = values.getAsInteger(PetsContract.PetsEntry.COLUMN_WEIGHT);
            if (weight == null && weight < 0) {
                return 0;
            }
        }

        if (values.size() == 0) {
            return 0;
        }
        int result;
        db = dbHelper.getWritableDatabase();
        result = db.update(PetsContract.PetsEntry.TABLE_NAME, values, selection, selectionArgs);
        if (result != 0) {
            //الابلاغ عن تغيير حدث داخل قاعدة البيانات ليتم تحديثها
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return result;
    }


}
