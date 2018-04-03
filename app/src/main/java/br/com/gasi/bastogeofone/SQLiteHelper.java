package br.com.gasi.bastogeofone;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class SQLiteHelper extends SQLiteOpenHelper {

    private static final String TAG = SQLiteHelper.class.getSimpleName();
    private static final String DBNAME = "BastaoGeofone";
    private String tableInspecoes = "Inspecoes";

    public SQLiteHelper(Context context) {
        super(context, DBNAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String createTable = "CREATE TABLE IF NOT EXISTS "+tableInspecoes+" (id INTEGER PRIMARY KEY AUTOINCREMENT, nomeInspecao VARCHAR(256), nomeTabela VARCHAR(256))";
        sqLiteDatabase.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        Log.d(TAG, "onUpgrade: not implemented");
    }

    public void novaInspecao(String name){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        String value = "Inspecao "+name.trim();
        if(value.length()>255){
            contentValues.put("nomeInspecao", value.substring(0,255));
        }else{
            contentValues.put("nomeInspecao", value);
        }
        contentValues.put("nomeTabela", name.hashCode());
    }
}
