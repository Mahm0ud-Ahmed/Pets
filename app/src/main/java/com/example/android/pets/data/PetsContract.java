package com.example.android.pets.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public final class PetsContract {


    public static final String AUTHORITIES = "com.example.android.pets";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITIES);
    public static final String PATH_TABLE = "pets";

    public PetsContract() {
    }

    // Inner Class
    public static abstract class PetsEntry implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_TABLE);

        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + AUTHORITIES + "/" + PATH_TABLE;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + AUTHORITIES + "/" + PATH_TABLE;


        //Table Name
        public static final String TABLE_NAME = "pets";

        //Column Name
        public static final String COLUMN_ID = BaseColumns._ID;
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_BREED = "breed";
        public static final String COLUMN_GENDER = "gender";
        public static final String COLUMN_WEIGHT = "weight";

        //Constant Column Gender
        public static final int UNKNOWN = 0;
        public static final int MALE = 1;
        public static final int FEMALE = 2;

    }
}
