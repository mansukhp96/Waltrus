 package com.izmansuk.securepasswordmanager.ui.main;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
//import android.database.sqlite.SQLiteDatabase;
//import android.database.sqlite.SQLiteOpenHelper;
import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {

    private static DBHelper instance;

    private static final String PPHRASE = "Q!W@E#R$T%Y^U&I*O(P)";

    public DBHelper(Context context) {
        super(context, "VaultData.db", null, 1);
    }

    static public synchronized DBHelper getInstance(Context context) {
        if(instance == null) {
            instance = new DBHelper(context);
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create Table VaultData(label TEXT primary key, domain TEXT, username TEXT, password TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop Table if exists VaultData");
    }

    public Boolean insertCredentials(String label, String domain, String username, String password) {
        SQLiteDatabase db = instance.getWritableDatabase(PPHRASE);
        ContentValues contentValues = new ContentValues();
        contentValues.put("label", label);
        contentValues.put("domain", domain);
        contentValues.put("username", username);
        contentValues.put("password", password);
        float result = db.insert("VaultData", null, contentValues);
        db.close();

        return result != -1;
    }

    public Boolean updateCredentials(String label, String domain, String username, String password) {
        SQLiteDatabase db = instance.getWritableDatabase(PPHRASE);
        ContentValues contentValues = new ContentValues();
        contentValues.put("label", label);
        contentValues.put("domain", domain);
        contentValues.put("username", username);
        contentValues.put("password", password);

        Cursor cursor = db.rawQuery("Select * from VaultData where name = ?", new String[]{label});

        if(cursor.getCount() > 0) {
            float result = db.update(
                    "VaultData",
                    contentValues,
                    "Select * from VaultData where label = ?", new String[]{label});
            db.close();
            return result != -1;
        }
        else
            return false;
    }

    public Boolean deleteCredentials(String label) {
        SQLiteDatabase db = instance.getWritableDatabase(PPHRASE);

        Cursor cursor = db.rawQuery("Select * from VaultData where label = ?", new String[]{label});

        if (cursor.getCount() > 0) {
            float result = db.delete(
                    "VaultData",
                    "label = ?",
                    new String[]{label});
            db.close();
            return result != -1;
        } else
            return false;
    }

    public List<String> getAllData() {
        SQLiteDatabase db = instance.getWritableDatabase(PPHRASE);
        Cursor cursor = (Cursor)db.rawQuery("Select * from VaultData", null);
        List<String> lbls = new ArrayList<>();
        if(cursor.moveToFirst()){
            while(!cursor.isAfterLast()) {
                String lbl = cursor.getString(cursor.getColumnIndex("label"));
                String usrnam = cursor.getString(cursor.getColumnIndex("username"));
                lbls.add(lbl + "-----" + usrnam);
                cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();

        return lbls;
    }
}
