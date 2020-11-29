package com.example.android.pets.data;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.pets.R;

public class ListAdapter extends CursorAdapter {

    public ListAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = null;
        ViewHolder viewHolder;
        view = LayoutInflater.from(context).inflate(R.layout.templet, parent, false);
        viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder;
        View v = view;
        if (v == null) {
            viewHolder = new ViewHolder(v);
            v.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) v.getTag();
        }
        int coloName = cursor.getColumnIndex(PetsContract.PetsEntry.COLUMN_NAME);
        int coloBreed = cursor.getColumnIndex(PetsContract.PetsEntry.COLUMN_BREED);

        viewHolder.tv_petName.setText(cursor.getString(coloName));
        if (!TextUtils.isEmpty(cursor.getString(coloBreed))) {
            viewHolder.tv_petBreed.setText(cursor.getString(coloBreed));
        } else {
            viewHolder.tv_petBreed.setText("Unknown breed");
        }
    }

    private final class ViewHolder {
        private TextView tv_petName;
        private TextView tv_petBreed;

        ViewHolder(View view) {
            tv_petName = view.findViewById(R.id.pet_name);
            tv_petBreed = view.findViewById(R.id.pet_breed);
        }
    }
}
