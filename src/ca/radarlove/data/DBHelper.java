package ca.radarlove.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper {
	String[] mCreateTables = new String[1];
	String[] mDropTables   = new String[1];
	
	public DBHelper(Context context, String name, CursorFactory factory, int version){
		super(context,name,factory,version);
		
		mCreateTables[0] = "CREATE TABLE points_of_interest " +
                          "( _id integer primary key, " +
                          "type text not null, " +
                          "name text not null, " +
                          "startdatetime text not null, " +
                          "enddatetime text not null, " +
                          "latitude float not null, " +
                          "longitude float not null, " +
                          "comment text)";

		mDropTables[0]   = "DROP TABLE IF EXISTS points_of_interest";
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		

		
		try{
			for (int i = 0; i < mCreateTables.length; i++)
			{
				db.execSQL(mCreateTables[i]);
			}
			
		} catch(SQLiteException ex){
			Log.v("Create table exception", ex.getMessage());
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w("DBHelper","Upgrading from version " +oldVersion+ " to version: "+newVersion);
		
		for (int i = 0; i < mDropTables.length; i++)
		{
			db.execSQL(mDropTables[i]);
		}
		
		onCreate(db);
	}

}
