package ca.radarlove.providers;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import ca.radarlove.data.RadarLoveDb;

public class RadarLoveProvider extends ContentProvider {

	private RadarLoveDb dba;
	
	public static final int COLUMN_IDX_ID               = 0;
	public static final int COLUMN_IDX_NAME             = 1;
	public static final int COLUMN_IDX_TYPE             = 2;
	public static final int COLUMN_IDX_LATITUDE         = 3;
	public static final int COLUMN_IDX_LONGITUDE        = 4;
	public static final int COLUMN_IDX_STARTDATETIME    = 5;
	public static final int COLUMN_IDX_ENDDATETIME      = 6;
	public static final int COLUMN_IDX_COMMENT          = 7;
	
	public static final String AUTHORITY                = "ca.radarlove.provider";
	public static final Uri CONTENT_URI_POI_DIR         = Uri.parse("content://"+AUTHORITY+"/pointsofinterest");
	public static final Uri CONTENT_URI_POI_ITEM        = Uri.parse("content://"+AUTHORITY+"/pointsofinterest_ID");
	public static final Uri CONTENT_URI_CAMERAS_DIR     = Uri.parse("content://"+AUTHORITY+"/intersectioncameras");
	public static final Uri CONTENT_URI_CAMERA_ITEM     = Uri.parse("content://"+AUTHORITY+"/intersectioncameras_ID");
	public static final Uri CONTENT_URI_PHOTORADAR_DIR  = Uri.parse("content://"+AUTHORITY+"/photoradar");
	public static final Uri CONTENT_URI_PHOTORADAR_ITEM = Uri.parse("content://"+AUTHORITY+"/photoradar_ID");
	public static final Uri CONTENT_URI_ACCIDENT_DIR    = Uri.parse("content://"+AUTHORITY+"/trafficaccidents");
	public static final Uri CONTENT_URI_ACCIDENT_ITEM   = Uri.parse("content://"+AUTHORITY+"/trafficaccidents_ID");
	
	public static final int POINTSOFINTEREST           = 0;
	public static final int INTERSECTIONCAMERAS        = 1;
	public static final int PHOTORADAR                 = 2;
	public static final int TRAFFICACCIDENTS           = 3;
	public static final int POINTSOFINTEREST_ID        = 4;
	public static final int TRAFFICACCIDENTS_ID        = 5;
	public static final int INTERSECTIONCAMERAS_ID     = 6;
	public static final int PHOTORADAR_ID              = 7;
	
	public static final String POI_COLUMN_ID            = "_id";
	public static final String POI_COLUMN_TYPE          = "type";
	public static final String POI_COLUMN_NAME          = "name";
	public static final String POI_COLUMN_STARTDATETIME = "startdatetime";
	public static final String POI_COLUMN_ENDDATETIME   = "enddatetime";
	public static final String POI_COLUMN_LATITUDE      = "latitude";
	public static final String POI_COLUMN_LONGITUDE     = "longitude";
	public static final String POI_COLUMN_COMMENT       = "comment";
	
	private static final UriMatcher mUriMatcher;
	static{
		mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		mUriMatcher.addURI(AUTHORITY, "pointsofinterest",         POINTSOFINTEREST);
		mUriMatcher.addURI(AUTHORITY, "pointsofinterest_ID/#",    POINTSOFINTEREST_ID);
		mUriMatcher.addURI(AUTHORITY, "intersectioncameras",      INTERSECTIONCAMERAS);
		mUriMatcher.addURI(AUTHORITY, "intersectioncameras_ID/#", INTERSECTIONCAMERAS_ID);
		mUriMatcher.addURI(AUTHORITY, "photoradar",               PHOTORADAR);
		mUriMatcher.addURI(AUTHORITY, "photoradar_ID/#",          PHOTORADAR_ID);
		mUriMatcher.addURI(AUTHORITY, "trafficaccidents",         TRAFFICACCIDENTS);
		mUriMatcher.addURI(AUTHORITY, "trafficaccidents_ID/#",    TRAFFICACCIDENTS_ID);		
	}
	
	@Override
	public boolean onCreate() {
		
		dba = new RadarLoveDb(this.getContext());
		dba.open();
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		Cursor mCursor = null;
		int matchID    = mUriMatcher.match(uri);
		
		switch(matchID){
			case POINTSOFINTEREST:
				mCursor = dba.getAllPointsOfInterest();
				break;			
			case INTERSECTIONCAMERAS:
				mCursor = dba.getIntersectionCamerasWithinBounds(selectionArgs);
				break;
			case PHOTORADAR:
				mCursor = dba.getPhotoRadarWithinBounds(selectionArgs);
				break;
			case TRAFFICACCIDENTS:
				mCursor = dba.getTrafficAccidentsWithinBounds(selectionArgs);
				break;
			case POINTSOFINTEREST_ID:
			case TRAFFICACCIDENTS_ID:
			case INTERSECTIONCAMERAS_ID:
			case PHOTORADAR_ID:
				mCursor = dba.getPointOfInterest(uri.getPathSegments().get(1));
				break;
			default:
					throw new IllegalArgumentException("Unknown URI " + uri);
		}
		
		mCursor.setNotificationUri(getContext().getContentResolver(),uri);
		
		return mCursor;
	}
	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		
		long mRowId = dba.insertPointOfInterest(values);
		
	    // Return a URI to the newly inserted row on success.
	    if (mRowId > 0) {
	      Uri mUri = ContentUris.withAppendedId(CONTENT_URI_POI_ITEM, mRowId);
	      getContext().getContentResolver().notifyChange(mUri, null);
	      return mUri;
	    } 
	    throw new SQLException("Failed to insert row into " + uri);
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		return 0;
	}

	@Override
	public int bulkInsert(Uri uri, ContentValues[] values) {
		// TODO - investigate possible change to bulk insert
		// within a transaction as seen here: http://eshyu.wordpress.com/2010/08/15/using-sqlite-transactions-with-your-contentprovider/
		return super.bulkInsert(uri, values);
	}

}
