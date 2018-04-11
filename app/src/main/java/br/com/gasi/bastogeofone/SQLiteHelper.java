package br.com.gasi.bastogeofone;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SQLiteHelper extends SQLiteOpenHelper {

    private static final String TAG = SQLiteHelper.class.getSimpleName();
    private static final String DBNAME = "BastaoGeofone";
    private static final String TABLECOL1 = "\'x\'";
    private static final String TABLECOL2 = "\'y\'";
    private static final String TABLECOL3 = "\'val\'";
    private String tableInspecoes = "Inspecoes";
    private SQLiteDatabase database;

    public SQLiteHelper(Context context) {
        super(context, DBNAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String createTable = "CREATE TABLE IF NOT EXISTS \'"+tableInspecoes+"\' (\'id\' INTEGER PRIMARY KEY AUTOINCREMENT, \'nomeInspecao\' VARCHAR(256), \'nomeTabela\' VARCHAR(256))";
        sqLiteDatabase.execSQL(createTable);
        database = sqLiteDatabase;
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        onCreate(sqLiteDatabase);
    }

    public void novaInspecao(String tableName, JSONArray values){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        String completeTableName = "\'Inspecao "+tableName.trim()+"\'";
        if(completeTableName.length()>255){
            contentValues.put("nomeInspecao", completeTableName.substring(0,255));
        }else{
            contentValues.put("nomeInspecao", completeTableName);
        }
        contentValues.put("nomeTabela", tableName.hashCode());
        db.insert(tableInspecoes,null, contentValues);
        String createTable = "CREATE TABLE IF NOT EXISTS "+completeTableName+" (\'id\' INTEGER PRIMARY KEY AUTOINCREMENT, "+TABLECOL1+" DOUBLE, "+TABLECOL2+" DOUBLE, "+TABLECOL3+" DOUBLE)";
        db.execSQL(createTable);
        insertValuesIntoTable(completeTableName, values);
    }

    private void insertValuesIntoTable(String completeTableName, JSONArray values) {
        SQLiteDatabase db = this.getWritableDatabase();
        for (int i=0; i<values.length();i++) {
            try {
                ContentValues contentValues = new ContentValues();
                JSONObject mJson = values.getJSONObject(i);
                contentValues.put(TABLECOL1,mJson.getDouble("lat"));
                contentValues.put(TABLECOL2,mJson.getDouble("lng"));
                contentValues.put(TABLECOL3,mJson.getDouble("val"));
                db.insert(completeTableName, null, contentValues);
                contentValues = null;
                mJson = null;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }



}
