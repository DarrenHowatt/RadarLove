package ca.radarlove.services;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import ca.radarlove.providers.RadarLoveProvider;

public class DownloadService extends IntentService {


	public static final String ACTION_DOWNLOAD_POI = "ca.radarlove.services.DownloadService.poi";

	private String mWebDomain                  = "HOME-PC";//"192.168.1.67";
	private String mWebPort                    = "80";	
	private String mDeviceID                   = "123";
	private String mLastRecordID               = "0";
	private boolean mContinue                  = false;
	private SharedPreferences mSettings        = null;
	private BroadcastReceiver mBatteryReceiver = null;

	public DownloadService() {
		super("DownloadService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {

		if (intent.getAction().equals(ACTION_DOWNLOAD_POI)) {
			Context mContext = getApplicationContext();
			mSettings       = PreferenceManager.getDefaultSharedPreferences(mContext);

			mContinue          = true;
			mDeviceID          = intent.getStringExtra("DeviceID");
			mWebDomain         = intent.getStringExtra("WebDomain");
			mWebPort           = intent.getStringExtra("WebPort");
			mLastRecordID      = mSettings.getString("LastRecordID", "0");

			// TODO - add checks for valid network connection

			createBatteryReceiver();

			downloadPointsOfInterest();
		}
	}

	/**
	 * 
	 */
	private void createBatteryReceiver() {
		IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		filter.addAction(Intent.ACTION_BATTERY_LOW);

		mBatteryReceiver = new BatteryBroadcastReceiver();
		registerReceiver(mBatteryReceiver, filter);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(mBatteryReceiver);
	}

	/**
	 * 
	 */
	private void downloadPointsOfInterest(){

		JSONArray mPointsOfInterest = null;
		String url                  = "http://"+mWebDomain+":"+mWebPort+"/Acme/AcmeLocatorService/PointsOfInterest/"+mDeviceID+"/"+ mLastRecordID;

		String rs  = HttpClient.SendHttpGet(url);

		try {
			mPointsOfInterest = new JSONArray(rs);

		} catch (JSONException ex) {			
			Log.v("Downloader Service", ex.getMessage());
		}

		if(mPointsOfInterest != null) {
			ContentResolver cr = getContentResolver();
			Cursor mResult      = null;
			int mResultCount    = 0;

			for (int i = 0; i < mPointsOfInterest.length(); i++) {
				if(mContinue){
					try{
						JSONObject row = mPointsOfInterest.getJSONObject(i);
						String id      = String.valueOf(row.getInt("PointOfInterestID"));
						Uri _uri       = Uri.parse(RadarLoveProvider.CONTENT_URI_POI_ITEM +"/"+ id);

						mResult         = cr.query(_uri, null, null, null, null);
						mResultCount    = mResult.getCount();
						mResult.close();

						// TODO - investigate possible change to bulk insert
						if (mResultCount==0){
							ContentValues poi = new ContentValues();
							poi.put(RadarLoveProvider.POI_COLUMN_ID,            row.getInt("PointOfInterestID"));
							poi.put(RadarLoveProvider.POI_COLUMN_NAME,          row.getString("Name"));
							poi.put(RadarLoveProvider.POI_COLUMN_TYPE,          row.getString("Type"));
							poi.put(RadarLoveProvider.POI_COLUMN_STARTDATETIME, row.getString("StartDateTime"));
							poi.put(RadarLoveProvider.POI_COLUMN_ENDDATETIME,   row.getString("EndDateTime"));
							poi.put(RadarLoveProvider.POI_COLUMN_LATITUDE,      row.getDouble("Latitude"));
							poi.put(RadarLoveProvider.POI_COLUMN_LONGITUDE,     row.getDouble("Longitude"));
							poi.put(RadarLoveProvider.POI_COLUMN_COMMENT,       row.getString("Comment"));

							cr.insert(RadarLoveProvider.CONTENT_URI_POI_DIR, poi);
							mLastRecordID = row.getString("PointOfInterestID");

						}
					}
					catch(Exception ex) {
						Log.v("Downloader Service loading POI data", ex.getMessage());
					}
				}
			}
		}

		// set the last start date time
		SharedPreferences.Editor editor = mSettings.edit();
		editor.putString("LastRecordID", mLastRecordID);
		editor.commit();

	}

	/**
	 * 
	 */
	private class BatteryBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)) {
				Bundle mBundle = intent.getExtras();

				if(null == mBundle){return;}

				boolean mIsPresent  = intent.getBooleanExtra("present", false);
				int     mScale      = intent.getIntExtra("scale", -1);
				int     mRawlevel   = intent.getIntExtra("level", -1);
				int     mLevel      = 0;

				if (mIsPresent && (mRawlevel >= 0) && (mScale > 0)) {
					mLevel = (mRawlevel * 100) / mScale;

					if(mLevel < 20){ mContinue = false; }
				}
			}
			else if(intent.getAction().equals(Intent.ACTION_BATTERY_LOW)) {
				mContinue = false;
			}
		}

	}

}
