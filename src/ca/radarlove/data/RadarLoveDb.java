package ca.radarlove.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

public class RadarLoveDb {
	private static final String DATABASE_NAME    ="radar_love.db";
	private static final int    DATABASE_VERSION =1;
	private static final String TABLE_NAME       ="points_of_interest";
	
	private SQLiteDatabase       mDb;
	private final Context        mContext;
	private final DBHelper       mDbHelper;
	public static final String[] mColumns = new String[] {"_id", "name", "type", "latitude", "longitude", "startdatetime", "enddatetime", "comment"};
	
	public RadarLoveDb(Context c){
		mContext = c;
		mDbHelper = new DBHelper(mContext, DATABASE_NAME, null, DATABASE_VERSION);
		
	}
	
	public void close()
	{
		mDb.close();
	}

	public void open() throws SQLiteException
	{
		try{
			mDb = mDbHelper.getWritableDatabase();
		} catch(SQLiteException ex){
			Log.v("open database exception caught", ex.getMessage());
			mDb = mDbHelper.getReadableDatabase();
		}
	}

	public Cursor getPointOfInterest(String id)
	{
		String mWhere       = "_id = ?";
		String[] mWhereArgs = new String[] {id};
		
		Cursor c = mDb.query(TABLE_NAME, mColumns, mWhere , mWhereArgs, null, null, null);
		
		return c;
	}
	
	public Cursor getAllPointsOfInterest()
	{
		Cursor c = mDb.query(TABLE_NAME, mColumns, null , null, null, null, "_id");
		
		return c;
	}

	public Cursor getAllPointsOfInterestWithinBounds(String[] whereArgs)
	{
		String mWhere = "(latitude >= ? AND latitude <= ?) AND (longitude >= ? AND longitude <= ?)";
		String[]mWhereArgs = whereArgs;	
		
		Cursor c = mDb.query(TABLE_NAME, mColumns, mWhere , mWhereArgs, null, null, null);
		
		return c;
	}
	
	public Cursor getIntersectionCamerasWithinBounds(String[] whereArgs)
	{
/*		String mWhere = "type = ?";
		String[] mWhereArgs = new String[] {"Intersection_Camera"};*/
		
		String mWhere = "type = 'Intersection_Camera' And (latitude >= ? AND latitude <= ?) AND (longitude >= ? AND longitude <= ?)";
		String[]mWhereArgs = whereArgs;		
		Cursor c = mDb.query(TABLE_NAME, mColumns, mWhere , mWhereArgs, null, null, null);
		
		return c;
	}

	public Cursor getPhotoRadarWithinBounds(String[] whereArgs)
	{
/*		String mWhere = "type = ?";
		String[] mWhereArgs = new String[] {"Photo_Radar"};*/
		
		String mWhere = "type = 'Photo_Radar' And (latitude >= ? AND latitude <= ?) AND (longitude >= ? AND longitude <= ?)";
		String[]mWhereArgs = whereArgs;
		
		Cursor c = mDb.query(TABLE_NAME, mColumns, mWhere , mWhereArgs, null, null, null);
		
		return c;
	}
	
	public Cursor getTrafficAccidentsWithinBounds(String[] whereArgs)
	{
/*		String mWhere = "type = ?";
		String[] mWhereArgs = new String[] {"Traffic_Accident"};*/
		
		String mWhere = "type = 'Traffic_Accident' And (latitude >= ? AND latitude <= ?) AND (longitude >= ? AND longitude <= ?)";
		String[]mWhereArgs = whereArgs;


		Cursor c = mDb.query(TABLE_NAME, mColumns, mWhere , mWhereArgs, null, null, null);

		return c;
	}
	
	public long insertPointOfInterest(ContentValues newPoiRow)
	{
		try{
			
			return mDb.insert(TABLE_NAME, null, newPoiRow);
			
		} catch(SQLiteException ex){
			Log.v("Database insert exception", ex.getMessage());
			return -1;
		}
	}
}
