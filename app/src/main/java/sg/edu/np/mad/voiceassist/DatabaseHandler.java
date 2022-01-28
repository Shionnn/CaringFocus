package sg.edu.np.mad.voiceassist;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DatabaseHandler extends SQLiteOpenHelper {

    public static String DATABASE_NAME = "VoiceAssist.db";
    public static String STT = "SpeechToText";
    public static String TTS = "TextToSpeech";
    public static String COLUMN_TEXT = "Text";
    public static String COLUMN_TIME_STAMP = "TimeStamp";

    public static int DATABASE_VERSION= 1;

    public DatabaseHandler(Context context, String name, SQLiteDatabase.CursorFactory factory, int version)
    {
        super(context, DATABASE_NAME, factory,DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE " + STT + "(" +
                        COLUMN_TEXT + " TEXT," +
                        COLUMN_TIME_STAMP + " TEXT)"


        );
        db.execSQL(
                "CREATE TABLE " + TTS + "(" +
                        COLUMN_TEXT + " TEXT," +
                        COLUMN_TIME_STAMP + " TEXT)"


        );
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + TTS);
        db.execSQL("DROP TABLE IF EXISTS " + STT);
        onCreate(db);
    }

    public void storeTTS(String text, String timeStamp){
        ContentValues values = new ContentValues();
        values.put(COLUMN_TEXT,text);
        values.put(COLUMN_TIME_STAMP,timeStamp);
        SQLiteDatabase db = this.getWritableDatabase();
        db.insert(TTS,null, values);
        db.close();
    }


    public ArrayList<String> GetTTS_Text(){
        ArrayList<String> returnText = new ArrayList<String>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TTS, null);
        while (cursor.moveToNext()){
            returnText.add(cursor.getString(1)+": "+cursor.getString(0));
        }
        return returnText;
    }

    public void deleteAllTTS(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from "+ TTS);
        db.close();
    }

    public void storeSTT(String text, String timeStamp){
        ContentValues values = new ContentValues();
        values.put(COLUMN_TEXT,text);
        values.put(COLUMN_TIME_STAMP,timeStamp);
        SQLiteDatabase db = this.getWritableDatabase();
        db.insert(STT,null, values);
        db.close();
    }


    public ArrayList<String> GetSTT_Text(){
        ArrayList<String> returnText = new ArrayList<String>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + STT, null);
        while (cursor.moveToNext()){
            returnText.add(cursor.getString(1)+": "+cursor.getString(0));
        }
        return returnText;
    }

    public void deleteAllSTT(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from "+ STT);
        db.close();
    }
}
