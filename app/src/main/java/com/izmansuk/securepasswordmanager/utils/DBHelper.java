 package com.izmansuk.securepasswordmanager.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.izmansuk.securepasswordmanager.activityscreens.AboutActivity;
import com.izmansuk.securepasswordmanager.activityscreens.SetMPasswordActivity;

import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteOpenHelper;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {

    private static DBHelper instance;

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

    public Boolean insertCredentials(Context context, String label, String domain, String username, String password) throws GeneralSecurityException, IOException {
        String PPHRASE = UtilsHelper.getEncryptedSharedPreferences(context)
                .getString("encMasterPasswd", null);

        SQLiteDatabase db = instance.getWritableDatabase("PPHRASE");
        ContentValues contentValues = new ContentValues();
        contentValues.put("label", label);
        contentValues.put("domain", domain);
        contentValues.put("username", username);
        contentValues.put("password", password);
        float result = db.insert("VaultData", null, contentValues);
        db.close();

        return result != -1;
    }

    public Boolean updateCredentials(Context context, String label, String domain, String username, String password) throws GeneralSecurityException, IOException {
        String PPHRASE = UtilsHelper.getEncryptedSharedPreferences(context)
                .getString("encMasterPasswd", null);

        SQLiteDatabase db = instance.getWritableDatabase(PPHRASE);
        ContentValues contentValues = new ContentValues();
        contentValues.put("domain", domain);
        contentValues.put("username", username);
        contentValues.put("password", password);

        Cursor cursor = db.rawQuery("Select * from VaultData where label = ?", new String[]{label});

        if(cursor.getCount() > 0) {
            float result = db.update(
                    "VaultData",
                    contentValues,
                    "label = ?", new String[]{label});
            db.close();
            return result != -1;
        }
        else
            return false;
    }

    public Boolean deleteCredentials(Context context, String label) throws GeneralSecurityException, IOException {
        String PPHRASE = UtilsHelper.getEncryptedSharedPreferences(context)
                .getString("encMasterPasswd", null);
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

    public List<String> getAllData(Context context) throws GeneralSecurityException, IOException {
        String PPHRASE = UtilsHelper.getEncryptedSharedPreferences(context)
                .getString("encMasterPasswd", null);
        Log.e("DBPASS", PPHRASE);
        SQLiteDatabase db = instance.getWritableDatabase(PPHRASE);
        Cursor cursor = (Cursor)db.rawQuery("Select * from VaultData", null);
        List<String> lbls = new ArrayList<>();
        if(cursor.moveToFirst()){
            while(!cursor.isAfterLast()) {
                String lbl = cursor.getString(cursor.getColumnIndex("label"));
                String usrnam = cursor.getString(cursor.getColumnIndex("username"));
                String domain = cursor.getString(cursor.getColumnIndex("domain"));
                lbls.add(lbl);
                cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();

        return lbls;
    }

    public String getDomain(Context context, String label) throws GeneralSecurityException, IOException {
        String PPHRASE = UtilsHelper.getEncryptedSharedPreferences(context)
                .getString("encMasterPasswd", null);

        SQLiteDatabase db = instance.getWritableDatabase(PPHRASE);
        Cursor cursor = db.rawQuery("Select domain from VaultData where label =?", new String[]{label});
        String domain = "";
        if(cursor.moveToFirst()) {
            while(!cursor.isAfterLast()) {
                domain = cursor.getString(cursor.getColumnIndex("domain"));
                cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();

        return domain;
    }

    public String getUsername(Context context, String label) throws GeneralSecurityException, IOException {
        String PPHRASE = UtilsHelper.getEncryptedSharedPreferences(context)
                .getString("encMasterPasswd", null);

        SQLiteDatabase db = instance.getWritableDatabase(PPHRASE);
        Cursor cursor = db.rawQuery("Select username from VaultData where label =?", new String[]{label});
        String domain = "";
        if(cursor.moveToFirst()) {
            while(!cursor.isAfterLast()) {
                domain = cursor.getString(cursor.getColumnIndex("username"));
                cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();

        return domain;
    }

    public String getPassword(Context context, String label) throws GeneralSecurityException, IOException {
        String PPHRASE = UtilsHelper.getEncryptedSharedPreferences(context)
                .getString("encMasterPasswd", null);

        SQLiteDatabase db = instance.getWritableDatabase(PPHRASE);
        Cursor cursor = db.rawQuery("Select password from VaultData where label =?", new String[]{label});
        String passwd = "";
        if(cursor.moveToFirst()) {
            while(!cursor.isAfterLast()) {
                passwd = cursor.getString(cursor.getColumnIndex("password"));
                cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();

        return passwd;
    }
}
