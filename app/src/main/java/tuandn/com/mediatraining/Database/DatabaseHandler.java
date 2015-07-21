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

import tuandn.com.mediatraining.Model.AudioFile;

/**
 * Created by Anh Trung on 7/21/2015.
 */
public class DatabaseHandler extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "FILE_MANAGEMENT";

    // Table Names
    private static final String TABLE_AUDIO = "TABLE_AUDIO";
    private static final String TABLE_VIDEO = "TABLE_VIDEO";

    // Audio Table - column names
    private static final String COLUMN_AUDIO_ID = "COLUMN_AUDIO_ID";
    private static final String COLUMN_AUDIO_NAME = "COLUMN_AUDIO_NAME";
    private static final String COLUMN_AUDIO_DATE = "COLUMN_AUDIO_DATE";

    // AUDIO Table create statement
    private static final String CREATE_TABLE_AUDIO = "CREATE TABLE "
            + TABLE_AUDIO + "(" + COLUMN_AUDIO_ID + " INTEGER PRIMARY KEY,"
            + COLUMN_AUDIO_NAME + " TEXT," + COLUMN_AUDIO_DATE
            + " TEXT" + ")";


    public DatabaseHandler(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_AUDIO);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public ArrayList<AudioFile> getListAudio(){
        ArrayList<AudioFile> listAudio = new ArrayList<AudioFile>();
        String selectQuery = "SELECT  * FROM " + TABLE_AUDIO;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
                AudioFile f = new AudioFile();
                f.setId(c.getInt(c.getColumnIndex(COLUMN_AUDIO_ID)));
                f.setName((c.getString(c
                        .getColumnIndex(COLUMN_AUDIO_NAME))));
                f.setDate(c.getString(c
                        .getColumnIndex(COLUMN_AUDIO_DATE)));
                listAudio.add(f);
            } while (c.moveToNext());
        }
        closeDatabse();
        return listAudio;
    }

    public void deleteAudio(int audioID){

    }

    public void addAudio(String fileName){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_AUDIO_NAME, fileName);
        values.put(COLUMN_AUDIO_DATE, getCurrentDate());
        db.insert(TABLE_AUDIO, null, values);
        closeDatabse();
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
