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

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.android.pets.R;
import com.example.android.pets.data.PetsContract.PetsEntry;

/**
 * Allows user to create a new pet or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * EditText field to enter the pet's name
     */
    private EditText mNameEditText;

    /**
     * EditText field to enter the pet's breed
     */
    private EditText mBreedEditText;

    /**
     * EditText field to enter the pet's weight
     */
    private EditText mWeightEditText;

    /**
     * EditText field to enter the pet's gender
     */
    private Spinner mGenderSpinner;

    /**
     * Gender of the pet. The possible values are:
     * 0 for unknown gender, 1 for male, 2 for female.
     */
    private int mGender = PetsEntry.UNKNOWN;

    private Uri uri;
    //متغير لمعرفة هل تم الضغط او التغيير في واجهة التعديل او الاضافه
    private boolean mPetHasChange = false;

    //هذا الكائن يخبرنا بحدوث عملية ضغط على الحقول ام لا
    private View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            mPetHasChange = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // استقبال ال Uri القادم من الأكتفتي الرئيسية
        Intent intent = getIntent();
        uri = intent.getData();

        // تغيير العنوان الرئيسي للأكتفتي بناء على ال Uri
        if (uri == null) {
            setTitle(getString(R.string.editor_activity_title_new_pet));
            // يتم استدعائها لإخبار الأكتفتي بتغيير حدث في ال Menu ليقوم بتعريفها من جديد
            invalidateOptionsMenu();
        } else {
            setTitle(getString(R.string.editor_activity_title_edit_pet));
            getSupportLoaderManager().initLoader(0, null, this);
        }

        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_pet_name);
        mBreedEditText = (EditText) findViewById(R.id.edit_pet_breed);
        mWeightEditText = (EditText) findViewById(R.id.edit_pet_weight);
        mGenderSpinner = (Spinner) findViewById(R.id.spinner_gender);

        // ربط مستمع النقرات على الشاشه بال editText
        mNameEditText.setOnTouchListener(onTouchListener);
        mBreedEditText.setOnTouchListener(onTouchListener);
        mGenderSpinner.setOnTouchListener(onTouchListener);
        mWeightEditText.setOnTouchListener(onTouchListener);

        setupSpinner();
    }

    //مربع حواري يتأكد من اليوزر من عملية الحذف
    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Alert!");
        builder.setMessage("Delete this pet?");
        // لو ضغط المستخدم كان بالتأكيد للعملية المطلوبه ماذا سيحدث
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deletePet();
            }
        });
        // لو ضغط المستخدم كان بالتراجع عن العملية المطلوبه ماذا سيحدث
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //الغاء مربع الحوار من امام المستخدم
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void deletePet() {
        if (uri != null) {
            int result = getContentResolver().delete(uri, null, null);
            if (result != 0) {
                Toast.makeText(this, "Delete Pet", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Error with Delete pet", Toast.LENGTH_LONG).show();
            }
        }
        finish();
    }
    // مربع حواري يتأكد من المستخدم من عملية ضغطه على زر الرجوع وتجاهل التغييرات التي تمت
    private void showUnsavedChangesDialog(DialogInterface.OnClickListener onClickListener) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Discard your changes and quit editing?");
        builder.setTitle("Alert!");
        // لو ضغط المستخدم كان بالتأكيد للعملية المطلوبه ماذا سيحدث
        builder.setPositiveButton("Discard", onClickListener);
        // لو ضغط المستخدم كان بالتراجع عن العملية المطلوبه ماذا سيحدث
        builder.setNegativeButton("Keep Editing", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (dialog != null) {
                    //الغاء الخروج من وضع الاضافة اوالتعديل في حالة البقاء في الوضع المذكور انفا
                    dialog.dismiss();
                }
            }
        });
        //انشاء مربع الحوار الذي تم تهيئته بالأعلى
        AlertDialog dialog = builder.create();
        //عرض مربع الحوار للمستخدم بعد انشائه بالخطوه السابقه
        dialog.show();
    }

    //دالة خاصة بزر الرجوع السفلي
    @Override
    public void onBackPressed() {
        //لو كان مؤشر الضغط يعطي false فمعناه ان المستخدم لم يضغط على اي حقل وبالتالي عدم ظهور المربع الحواري
        if (!mPetHasChange) {
            super.onBackPressed();
            return;
        }

        DialogInterface.OnClickListener clickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //الخروج من وضع الاضافة او التعديل في حالة الضغط عل discard
                finish();
            }
        };
        showUnsavedChangesDialog(clickListener);
    }

    /**
     * Setup the dropdown spinner that allows the user to select the gender of the pet.
     */
    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter genderSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_gender_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        genderSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mGenderSpinner.setAdapter(genderSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mGenderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String select = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(select)) {
                    if (select.equals(getString(R.string.gender_male))) {
                        mGender = PetsEntry.MALE; // Male
                    } else if (select.equals(getString(R.string.gender_female))) {
                        mGender = PetsEntry.FEMALE; // Female
                    } else {
                        mGender = PetsEntry.UNKNOWN; // Unknown
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mGender = PetsEntry.UNKNOWN; // Unknown
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        //اخفاء عنصر ال delete من ال Menu أثناء وضع الاضافه
        super.onPrepareOptionsMenu(menu);
        if (uri == null) {
            MenuItem item = menu.findItem(R.id.action_delete);
            item.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                savePet();
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Do nothing for now
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                //لو كان مؤشر الضغط يعطي false فمعناه ان المستخدم لم يضغط على اي حقل وبالتالي عدم ظهور المربع الحواري
                // Navigate back to parent activity (CatalogActivity)
                if (!mPetHasChange) {
                    NavUtils.navigateUpFromSameTask(this);
                    return true;
                }

                DialogInterface.OnClickListener clickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //الخروج من وضع الاضافة او التعديل في حالة الضغط عل discard
                        NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    }
                };
                showUnsavedChangesDialog(clickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void savePet() {
        //INSERT INTO pets (name, breed, gender, weight) VALUES ("dog" ,"sisi" ,1, 12);
        ContentValues values = null;
        //بدء عملية اضافة بيانات داخل قاعدة البيانات من خلال حقول الادخال المتاحه للمستخدم
        String name = mNameEditText.getText().toString().trim();
        String breed = mBreedEditText.getText().toString().trim();
        String weightString = mWeightEditText.getText().toString().trim();
        if (uri == null && TextUtils.isEmpty(name) && TextUtils.isEmpty(breed) &&
                TextUtils.isEmpty(weightString) && mGender == PetsEntry.UNKNOWN) {
            //حفظ التطبيق من الانهيار اذا كانت القيم فارغه
            return;
        }
        //حفظ التطبيق من الانهيار بجعل خانة الوزن ب 0 اذا كانت قيمتها فارغه
        int weight = 0;
        if (!TextUtils.isEmpty(weightString)) {
            weight = Integer.parseInt(weightString);
        }

        values = new ContentValues();
        values.put(PetsEntry.COLUMN_NAME, name);
        values.put(PetsEntry.COLUMN_BREED, breed);
        values.put(PetsEntry.COLUMN_GENDER, mGender);
        values.put(PetsEntry.COLUMN_WEIGHT, weight);

        // في حالة حفظ كائن جديد
        if (uri == null) {
            uri = getContentResolver().insert(PetsEntry.CONTENT_URI, values);
            if (uri == null) {
                Toast.makeText(this, "Error with saving pet", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Pet Save", Toast.LENGTH_LONG).show();
            }
        }
        // في حالة التعديل على كائن جديد
        else {
            int result = getContentResolver().update(uri, values, null, null);
            if (result != 0) {
                Toast.makeText(this, "Pet Update", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Error with Update pet", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        //الأعمده الذي سيتم الاستعلام عنها داخل قاعدة البيانات
        String[] projection = {
                PetsEntry.COLUMN_ID,
                PetsEntry.COLUMN_NAME,
                PetsEntry.COLUMN_BREED,
                PetsEntry.COLUMN_GENDER,
                PetsEntry.COLUMN_WEIGHT
        };

        //يقوم بالاستعلام داخل قاعدة البيانات من خلال ContentResolver وباستخدام ال Content URI
        //يقوم بتحديث القائمة لنا أولا بأولا من خلال التواصل الذي تم شرحه سابقا، ويرجع لنا كائن من نوع Cursor
        return new CursorLoader(this, uri, projection,
                null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        //إعادة محتويات ال Cursor الذي سيتم تعديله إلى حقول ال EditText
        if (data != null) {
            if (data.moveToFirst()) {
                int coloName = data.getColumnIndex(PetsEntry.COLUMN_NAME);
                int coloBreed = data.getColumnIndex(PetsEntry.COLUMN_BREED);
                int coloGender = data.getColumnIndex(PetsEntry.COLUMN_GENDER);
                int coloWeight = data.getColumnIndex(PetsEntry.COLUMN_WEIGHT);

                mNameEditText.setText(data.getString(coloName));
                mBreedEditText.setText(data.getString(coloBreed));
                mWeightEditText.setText(Integer.toString(data.getInt(coloWeight)));

                switch (data.getInt(coloGender)) {
                    case PetsEntry.FEMALE:
                        mGenderSpinner.setSelection(PetsEntry.FEMALE);
                        break;
                    case PetsEntry.MALE:
                        mGenderSpinner.setSelection(PetsEntry.MALE);
                        break;
                    default:
                        mGenderSpinner.setSelection(PetsEntry.UNKNOWN);
                }
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //تفريغ حقول ال EditText من القيم بعد الانتهاء من التعديل
        mNameEditText.getText().clear();
        mBreedEditText.getText().clear();
        mWeightEditText.getText().clear();
        mGenderSpinner.setSelection(PetsEntry.UNKNOWN);
    }
}