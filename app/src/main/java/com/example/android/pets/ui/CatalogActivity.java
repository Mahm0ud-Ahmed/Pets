/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.pets.ui;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.android.pets.R;
import com.example.android.pets.data.ListAdapter;
import com.example.android.pets.data.PetDbHelper;
import com.example.android.pets.data.PetsContract.PetsEntry;


/**
 * Displays list of pets that were entered and stored in the app.
 */
public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    ListView listView;
    ListAdapter listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        listView = (ListView) findViewById(R.id.list_item);

        ImageView imageView = (ImageView) findViewById(R.id.img_empty);
        listView.setEmptyView(imageView);

        listAdapter = new ListAdapter(getBaseContext(), null);
        listView.setAdapter(listAdapter);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        LoaderManager manager = getSupportLoaderManager();
        manager.initLoader(0, null, this);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getBaseContext(), EditorActivity.class);
                intent.setData(ContentUris.withAppendedId(PetsEntry.CONTENT_URI, id));
                startActivity(intent);
            }
        });
    }


    private void insertData() {
        //INSERT INTO pets (name, breed, gender, weight) VALUES ("dog" ,"sisi" ,1, 12);
        //Write in Database
        ContentValues values = new ContentValues();
        values.put(PetsEntry.COLUMN_NAME, "dog");
        values.put(PetsEntry.COLUMN_BREED, "sisi");
        values.put(PetsEntry.COLUMN_GENDER, PetsEntry.MALE);
        values.put(PetsEntry.COLUMN_WEIGHT, 12);
        Uri uri = getContentResolver().insert(PetsEntry.CONTENT_URI, values);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy com.example.android.pets.data" menu option
            case R.id.action_insert_dummy_data:
                insertData();
//                displayDatabaseInfo();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                // Do nothing for now
                showDeleteConfirmationDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void deletePet() {
        int result = getBaseContext().getContentResolver().delete(PetsEntry.CONTENT_URI, null, null);
        if (result != 0) {
            Toast.makeText(this, "Pet Delete All", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Error with Delete pet", Toast.LENGTH_LONG).show();
        }
    }

    private void showDeleteConfirmationDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Alert!");
        builder.setMessage("Delete All pet?");
        builder.setPositiveButton("Discard", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deletePet();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        //الأعمده الذي سيتم الاستعلام عنها داخل قاعدة البيانات
        // هذه الأعمده هي التي سيتم عرض بياناتها على الشاشه لليوزر، مع ادارج ال id للاستعلام لأنه هام جدا
        String[] projection = {
                PetsEntry.COLUMN_ID,
                PetsEntry.COLUMN_NAME,
                PetsEntry.COLUMN_BREED
        };
        //يقوم بالاستعلام داخل قاعدة البيانات من خلال ContentResolver وباستخدام ال Content URI
        //يقوم بتحديث القائمة لنا أولا بأولا من خلال التواصل الذي تم شرحه سابقا، ويرجع لنا كائن من نوع Cursor
        return new CursorLoader(getBaseContext(), PetsEntry.CONTENT_URI, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        //إرسال كائن ال Cursor بعد مجيئه من قاعدة البيانات من خلال دالة onCreateLoader إلى ال Adapter لعرضه على الشاشه
        if (data != null) {
            listAdapter.swapCursor(data);
            listAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //يتم استدعاء هذه الداله عند اهمال البيانات السابقه لوجود بيانات جديده قادمه
        // يتم تمرير null لل adapter لتفريغ الذاكره وعدم الاحتفاظ بالبيانات القديمه
        listAdapter.swapCursor(null);
    }
}
