package ca.radarlove.services;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.widget.Toast;
import ca.radarlove.LocationChangeHistory;
import ca.radarlove.MapMath;
import ca.radarlove.PointOfInterest;
import ca.radarlove.Polygon;
import ca.radarlove.providers.RadarLoveProvider;

public class ScannerService extends Service {
	public static final String ACTION_POI_DETECTED        = "ca.radarlove.services.scannerservice.poi.detected";
	//public static final String ACTION_SCAN_AREAUPDATED    = "ca.radarlove.services.scannerservice.scan.areaupdated";
	
	private SharedPreferences     mSettings           = null;
	private LocationManager       mLocManager         = null;
	private LocationListener      mLocationListener   = null;
	private int                   mScanStartSpeed     = 40;
	private double                mMinMps             = (40 * 1000)/3600;
	private float				  mMinDistance		  = 100;
	private Polygon               mPolygon            = null;
	private ScanForAccidentsTask  mLastAccidentScan   = null;
	private ScanForCamerasTask    mLastCameraScan     = null;
	private ScanForPhotoRadarTask mLastPhotoRadarScan = null;
	private boolean               mContinue           = true;
	private LocationChangeHistory mLocChangeHistory   = null;
	
	private double	              mPrevLatitude       = 0d;
	private double	              mPrevLongitude      = 0d;
	private double	              mPrevBearing        = 0d;
	private double	              mPrevSpeed          = 0d;
	
	public ScannerService() { }

	@Override
	public IBinder onBind(Intent arg0) {

		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Toast.makeText(this, "Started Scanning", Toast.LENGTH_LONG).show();

		mSettings       = PreferenceManager.getDefaultSharedPreferences(this);
		mScanStartSpeed = Integer.valueOf(mSettings.getString("scan_start_speed", "40"));
		mMinDistance	= 100; // metres
		
		mMinMps         = (mScanStartSpeed * 1000)/3600;
		
		mLocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE); 
		mLocationListener = new MyLocationListener();
		
		//TODO - change location update minDistance to be a preference passed in via an intent
		mLocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0 , mMinDistance, mLocationListener);

		mPolygon = new Polygon();
		mLocChangeHistory = new LocationChangeHistory(3);
		
		return super.onStartCommand(intent, flags, startId);
	}

	public void broadcastAlert(PointOfInterest poi){
		Intent intent = new Intent(ACTION_POI_DETECTED);
		intent.putExtra("ca.radarlove.PointOfInterest", poi);

		sendBroadcast(intent);
	}
	
	private class MyLocationListener implements LocationListener 
	{

		@Override
		public void onLocationChanged(Location loc) {

			if (loc != null) {
				double mCurLatitude     = loc.getLatitude();
				double mCurLongitude    = loc.getLongitude();
				double mCurBearing 		= 90;//loc.getBearing();
				double mCurSpeed        = 22;//loc.getSpeed();				

				mLocChangeHistory.addLatitude(mCurLatitude - mPrevLatitude);
				mLocChangeHistory.addLongitude(mCurLongitude - mPrevLongitude);
				mLocChangeHistory.addBearing(mCurBearing - mPrevBearing);
				mLocChangeHistory.addSpeed(mCurSpeed - mPrevSpeed);
				
				if(mCurSpeed > mMinMps) {	

					mPolygon.setLocation(loc);
					//mPolygon.setProjectedBearing(mLocChangeHistory.getProjectedBearing(mCurLatitude, mCurLongitude));
					mPolygon.setBearingAdj(mLocChangeHistory.getAverageBearing());
					mPolygon.calculate();
					
					if (mLastAccidentScan == null || mLastAccidentScan.getStatus().equals(AsyncTask.Status.FINISHED)) {
						mLastAccidentScan = new ScanForAccidentsTask();
						mLastAccidentScan.execute(mPolygon);
					}

					if (mLastCameraScan == null || mLastCameraScan.getStatus().equals(AsyncTask.Status.FINISHED)) {
						mLastCameraScan = new ScanForCamerasTask();      			
						mLastCameraScan.execute(mPolygon);
					}

					if (mLastPhotoRadarScan == null || mLastPhotoRadarScan.getStatus().equals(AsyncTask.Status.FINISHED)) {
						mLastPhotoRadarScan = new ScanForPhotoRadarTask();
						mLastPhotoRadarScan.execute(mPolygon);
					}
				}
				
				mPrevLatitude       = mCurLatitude;
				mPrevLongitude      = mCurLongitude;
				mPrevBearing 		= mCurBearing;
				mPrevSpeed          = mCurSpeed;
			}
		}

		@Override
		public void onProviderDisabled(String provider) {
			mLocManager.removeUpdates(mLocationListener);
			Toast.makeText(getApplicationContext(), "Paused Scanning", Toast.LENGTH_LONG).show();
		}

		@Override
		public void onProviderEnabled(String provider) {
			mLocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0 , 100, mLocationListener);
			Toast.makeText(getApplicationContext(), "Resumed Scanning", Toast.LENGTH_LONG).show();
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO - Broadcast an intent to scanner service when location provider status changes

		}
	}   
	
	/**
	  * 
	  */
	private class ScanForAccidentsTask extends AsyncTask<Polygon,Void,Void>{

		@Override
		protected Void doInBackground(Polygon... params) {
			ContentResolver mCr = getContentResolver();
			Cursor mResult      = null;
			Polygon mPoly       = params[0];

			// TODO - add uri to Provider to return only those records that are current: end date time is null or in future
			String[] mWhereArgs = mPoly.getBoundingSquareAsString(2);
			mResult = mCr.query(RadarLoveProvider.CONTENT_URI_ACCIDENT_DIR, null, null, mWhereArgs, null);

			int mNumVert        = mPoly.getNumberofVertices();
			double[] mPolyLats  = mPoly.getLatitudes();
			double[] mPolyLongs = mPoly.getLongitudes();

			if (mResult.moveToFirst()){
				do {
					double mLat  = mResult.getFloat(RadarLoveProvider.COLUMN_IDX_LATITUDE);
					double mLong = mResult.getFloat(RadarLoveProvider.COLUMN_IDX_LONGITUDE);

					if (MapMath.isPointInPolygon(mNumVert, mPolyLats, mPolyLongs, mLat, mLong)) {
						PointOfInterest mPoi = new PointOfInterest();
						mPoi.setPOIId        ( mResult.getInt   (RadarLoveProvider.COLUMN_IDX_ID));
						mPoi.setName         ( mResult.getString(RadarLoveProvider.COLUMN_IDX_NAME));
						mPoi.setType         ( mResult.getString(RadarLoveProvider.COLUMN_IDX_TYPE));
						mPoi.setStartDateTime( mResult.getString(RadarLoveProvider.COLUMN_IDX_STARTDATETIME));
						mPoi.setEndDateTime  ( mResult.getString(RadarLoveProvider.COLUMN_IDX_ENDDATETIME));
						mPoi.setLatitude     ( mResult.getDouble(RadarLoveProvider.COLUMN_IDX_LATITUDE));
						mPoi.setLongitude    ( mResult.getDouble(RadarLoveProvider.COLUMN_IDX_LONGITUDE));
						mPoi.setComment      ( mResult.getString(RadarLoveProvider.COLUMN_IDX_COMMENT));

						broadcastAlert(mPoi);
					}

				} while(mContinue && mResult.moveToNext());
			}

			mResult.close();
			mResult = null;

			return null;
		}

	}

	 /**
	  * 
	  */
	private class ScanForCamerasTask extends AsyncTask<Polygon,Void,Void>{

		@Override
		protected Void doInBackground(Polygon... params) {
			ContentResolver mCr = getContentResolver();
			Cursor mResult      = null;
			Polygon mPoly       = params[0];

			// TODO - add uri to Provider to return only those records that are current: end date time is null or in future
			String[] mWhereArgs = mPoly.getBoundingSquareAsString(2);
			mResult = mCr.query(RadarLoveProvider.CONTENT_URI_CAMERAS_DIR, null, null, mWhereArgs, null);

			int mNumVert        = mPoly.getNumberofVertices();
			double[] mPolyLats  = mPoly.getLatitudes();
			double[] mPolyLongs = mPoly.getLongitudes();

			if (mResult.moveToFirst()){
				do {
					double mLat  = mResult.getFloat(RadarLoveProvider.COLUMN_IDX_LATITUDE);
					double mLong = mResult.getFloat(RadarLoveProvider.COLUMN_IDX_LONGITUDE);

					if (MapMath.isPointInPolygon(mNumVert, mPolyLats, mPolyLongs, mLat, mLong)) {
						PointOfInterest mPoi = new PointOfInterest();
						mPoi.setPOIId        ( mResult.getInt   (RadarLoveProvider.COLUMN_IDX_ID));
						mPoi.setName         ( mResult.getString(RadarLoveProvider.COLUMN_IDX_NAME));
						mPoi.setType         ( mResult.getString(RadarLoveProvider.COLUMN_IDX_TYPE));
						mPoi.setStartDateTime( mResult.getString(RadarLoveProvider.COLUMN_IDX_STARTDATETIME));
						mPoi.setEndDateTime  ( mResult.getString(RadarLoveProvider.COLUMN_IDX_ENDDATETIME));
						mPoi.setLatitude     ( mResult.getDouble(RadarLoveProvider.COLUMN_IDX_LATITUDE));
						mPoi.setLongitude    ( mResult.getDouble(RadarLoveProvider.COLUMN_IDX_LONGITUDE));
						mPoi.setComment      ( mResult.getString(RadarLoveProvider.COLUMN_IDX_COMMENT));

						broadcastAlert(mPoi);
					}
				} while(mContinue && mResult.moveToNext());
			}

			mResult.close();
			mResult = null;

			return null;
		}

	}

	 /**
	  * 
	  */
	private class ScanForPhotoRadarTask extends AsyncTask<Polygon,Void,Void>{

		@Override
		protected Void doInBackground(Polygon... params) {
			ContentResolver mCr = getContentResolver();
			Cursor mResult      = null;
			Polygon mPoly       = params[0];

			// TODO - add uri to Provider to return only those records that are current: end date time is null or in future
			String[] mWhereArgs = mPoly.getBoundingSquareAsString(2);
			mResult = mCr.query(RadarLoveProvider.CONTENT_URI_PHOTORADAR_DIR, null, null, mWhereArgs, null);

			int mNumVert        = mPoly.getNumberofVertices();
			double[] mPolyLats  = mPoly.getLatitudes();
			double[] mPolyLongs = mPoly.getLongitudes();

			if (mResult.moveToFirst()){
				do {
					double mLat  = mResult.getFloat(RadarLoveProvider.COLUMN_IDX_LATITUDE);
					double mLong = mResult.getFloat(RadarLoveProvider.COLUMN_IDX_LONGITUDE);

					if (MapMath.isPointInPolygon(mNumVert, mPolyLats, mPolyLongs, mLat, mLong)) {
						PointOfInterest mPoi = new PointOfInterest();
						mPoi.setPOIId        ( mResult.getInt   (RadarLoveProvider.COLUMN_IDX_ID));
						mPoi.setName         ( mResult.getString(RadarLoveProvider.COLUMN_IDX_NAME));
						mPoi.setType         ( mResult.getString(RadarLoveProvider.COLUMN_IDX_TYPE));
						mPoi.setStartDateTime( mResult.getString(RadarLoveProvider.COLUMN_IDX_STARTDATETIME));
						mPoi.setEndDateTime  ( mResult.getString(RadarLoveProvider.COLUMN_IDX_ENDDATETIME));
						mPoi.setLatitude     ( mResult.getDouble(RadarLoveProvider.COLUMN_IDX_LATITUDE));
						mPoi.setLongitude    ( mResult.getDouble(RadarLoveProvider.COLUMN_IDX_LONGITUDE));
						mPoi.setComment      ( mResult.getString(RadarLoveProvider.COLUMN_IDX_COMMENT));
						
						broadcastAlert(mPoi);

					}
				} while(mContinue && mResult.moveToNext());
			}

			mResult.close();
			mResult = null;

			return null;
		}

	}

	@Override
	public void onDestroy() {
		Toast.makeText(this, "Stopped Scanning", Toast.LENGTH_LONG).show();
		mLocManager.removeUpdates(mLocationListener);
		mLocManager         = null;
		mPolygon            = null;
		mContinue           = false;
		mLastAccidentScan   = null;
		mLastCameraScan     = null;
		mLastPhotoRadarScan = null;
		mLocChangeHistory   = null;
		super.onDestroy();
	}


}
