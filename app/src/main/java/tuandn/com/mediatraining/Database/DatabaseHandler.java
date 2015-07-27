package tuandn.com.mediatraining.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import tuandn.com.mediatraining.Model.MediaFile;

/**
 * Created by Anh Trung on 7/21/2015.
 */
public class DatabaseHandler extends SQLiteOpenHelper {

    public static final int AUDIO_TYPE = 1;
    public static final int VIDEO_TYPE = 2;

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "FILE_MANAGEMENT";

    // Table Names
    private static final String TABLE_MEDIA= "TABLE_MEDIA";

    // Audio Table - column names
    private static final String COLUMN_MEDIA_ID = "COLUMN_MEDIA_ID";
    private static final String COLUMN_MEDIA_NAME = "COLUMN_MEDIA_NAME";
    private static final String COLUMN_MEDIA_TYPE = "COLUMN_MEDIA_TYPE";
    private static final String COLUMN_MEDIA_DATE = "COLUMN_MEDIA_DATE";

    // AUDIO Table create statement
    private static final String CREATE_TABLE_MEDIA = "CREATE TABLE "
            + TABLE_MEDIA
            + "("
            + COLUMN_MEDIA_ID + " INTEGER PRIMARY KEY,"
            + COLUMN_MEDIA_NAME + " TEXT,"
            + COLUMN_MEDIA_TYPE + " TEXT,"
            + COLUMN_MEDIA_DATE + " TEXT"
            + ")";


    public DatabaseHandler(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_MEDIA);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public ArrayList<MediaFile> getListAudio(){
        ArrayList<MediaFile> listAudio = new ArrayList<MediaFile>();
        String selectQuery = "SELECT  * FROM " + TABLE_MEDIA + " WHERE " + COLUMN_MEDIA_TYPE + " = " +AUDIO_TYPE;

        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
                MediaFile f = new MediaFile();
                f.setId(c.getInt(c.getColumnIndex(COLUMN_MEDIA_ID)));
                f.setName((c.getString(c
                        .getColumnIndex(COLUMN_MEDIA_NAME))));
                f.setDate(c.getString(c
                        .getColumnIndex(COLUMN_MEDIA_DATE)));
                listAudio.add(f);
            } while (c.moveToNext());
        }
        closeDatabse();
        return listAudio;
    }

    public ArrayList<MediaFile> getListVideo(){
        ArrayList<MediaFile> listAudio = new ArrayList<MediaFile>();
        String selectQuery = "SELECT  * FROM " + TABLE_MEDIA + " WHERE " + COLUMN_MEDIA_TYPE + " = " +VIDEO_TYPE;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
                MediaFile f = new MediaFile();
                f.setId(c.getInt(c.getColumnIndex(COLUMN_MEDIA_ID)));
                f.setName((c.getString(c
                        .getColumnIndex(COLUMN_MEDIA_NAME))));
                f.setType((c.getInt(c
                        .getColumnIndex(COLUMN_MEDIA_TYPE))));
                f.setDate(c.getString(c
                        .getColumnIndex(COLUMN_MEDIA_DATE)));
                listAudio.add(f);
            } while (c.moveToNext());
        }
        closeDatabse();
        return listAudio;
    }

    public void deleteAudio(int audioID){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_MEDIA, COLUMN_MEDIA_ID + "=?",
                new String[]{String.valueOf(audioID)});
        closeDatabse();
    }

    public boolean addMediaFile(String fileName, int fileType){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_MEDIA_NAME, fileName);
        values.put(COLUMN_MEDIA_TYPE, fileType);
        values.put(COLUMN_MEDIA_DATE, getCurrentDate());
        if (db.insert(TABLE_MEDIA, null, values)!=0) {
            closeDatabse();
            return true;
        }
        else{
            closeDatabse();
            return false;
        }
    }

    public void closeDatabse() {
        SQLiteDatabase db = getReadableDatabase();
        if (db != null && db.isOpen())
            db.close();
    }

    public String getCurrentDate(){
        String date;
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date2 = new Date();
        date  = dateFormat.format(date2);
        return date;
    }
}
