package ca.radarlove;

import java.util.Calendar;
import java.util.List;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import ca.radar.overlays.CurrentLocationOverlay;
import ca.radar.overlays.PoiOverlay;
import ca.radarlove.receivers.RadarLoveReceiver;
import ca.radarlove.services.ScannerService;

import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MapView.ReticleDrawMode;
import com.google.android.maps.Overlay;

public class RadarLoveActivity extends MapActivity {
	private SharedPreferences      mSettings         = null;
	private MapView                mMapView          = null;
	private PointOfInterest        mPOI              = null;
	private MapController          mMapController    = null;
	private BroadcastReceiver      mReceiver         = null;
	private IntentFilter           mFilter           = null;
	private LocationManager        mLocManager       = null;
	private CurrentLocationOverlay mCurrentLocation  = null;
	private boolean				   mSatelliteView    = false;
	private boolean				   mTrafficView      = false;
	private boolean				   mPerformUpdates	 = true;
	private long    		       mUpdateInterval   ;

	private String 				   mNotifyCamera;
	private String 				   mNotifyTraffic;
	private String 				   mNotifyPhoto;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		readSettings();
		
		setContentView(R.layout.main);

		createFilterForScanner();

		configureMapView();  
		
		mLocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		if(mLocManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
			startService(new Intent(this, ScannerService.class));
		}
		else{
			Toast.makeText(this, "GPS not enabled", Toast.LENGTH_LONG).show();
		}
		
		if (mPerformUpdates){
			setAlarm();
		}
	}

	/**
	 * 
	 */
	protected void readSettings() {
		
		mSettings       = PreferenceManager.getDefaultSharedPreferences(this);
		
		mNotifyCamera   = mSettings.getString("notifycamera",  "content://settings/system/notification_sound");
		mNotifyTraffic  = mSettings.getString("notifytraffic", "content://settings/system/notification_sound");
		mNotifyPhoto    = mSettings.getString("notifyphoto",   "content://settings/system/notification_sound");

		mPerformUpdates = mSettings.getBoolean("perform_updates", true);
		mUpdateInterval = Long.valueOf(mSettings.getString("data_update_interval", "0"));
		
		mSatelliteView  = mSettings.getBoolean("satellite_view", false);
		mTrafficView    = mSettings.getBoolean("traffic_view", false);

		
	}

	@Override
	protected void onPause() {
		super.onPause();
		
		try{
			unregisterReceiver(mReceiver);
		}
		catch(IllegalArgumentException ex){}
			
		if(mCurrentLocation != null){
			mCurrentLocation.disableMyLocation();
		}
		
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mReceiver != null && mFilter != null){
			registerReceiver(mReceiver,mFilter);
		}
		if (mCurrentLocation != null){
			mCurrentLocation.enableMyLocation();
		}
		
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		stopService(new Intent(this, ScannerService.class));

		try{
			unregisterReceiver(mReceiver);
		}
		catch(IllegalArgumentException ex){}
			
		if(mCurrentLocation != null){
			mCurrentLocation.disableMyLocation();
		}
		
		mCurrentLocation   = null;
		mReceiver          = null;
		mMapController     = null;
		mMapView           = null;
	}
	
	private static final int ITEM_SETTINGS = 0;
	private static final int ITEM_STOP_SCAN = 1;
	private static final int ITEM_START_SCAN = 2;
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, ITEM_SETTINGS, 0, R.string.settings);
		menu.add(Menu.NONE, ITEM_STOP_SCAN, 1, R.string.stop_scanner_service);
		menu.add(Menu.NONE, ITEM_START_SCAN, 2, R.string.start_scanner_service);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case ITEM_SETTINGS:	
			startActivity(new Intent(this, Preferences.class));
			return true;
		case ITEM_STOP_SCAN:	
			stopService(new Intent(this, ScannerService.class));
			return true;
		case ITEM_START_SCAN:	
			startService(new Intent(this, ScannerService.class));
			return true;
		}
		return false;
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	 /**
	  * 
	  */
	 private void createFilterForScanner() {
		 mFilter = new IntentFilter();
		 mFilter.addAction(ScannerService.ACTION_POI_DETECTED);
		 mReceiver = new ScannerServiceReceiver();
		 registerReceiver(mReceiver,mFilter);
	 }
	 
	/**
	 * 
	 */
	 private void configureMapView() {
		mMapView = (MapView) findViewById(R.id.map1);
		mMapView.setBuiltInZoomControls(true);
		mMapView.setSatellite(mSatelliteView);
		mMapView.setTraffic(mTrafficView);
		mMapView.setReticleDrawMode(ReticleDrawMode.DRAW_RETICLE_OVER);
		mMapController = mMapView.getController();
		mMapController.setZoom(15);

		CurrentLocationOverlay mCurrentLocation = new CurrentLocationOverlay(this, mMapView);
		mCurrentLocation.enableMyLocation();

		List<Overlay> overlays = mMapView.getOverlays();
		overlays.add(mCurrentLocation);
	 }

	 private void setAlarm(){
		 // add alarm
		 AlarmManager alarmMgr = (AlarmManager)getSystemService(Context.ALARM_SERVICE);

		 Intent intent = new Intent(this, RadarLoveReceiver.class);
		 intent.setAction(RadarLoveReceiver.ACTION_DOWNLOAD_POI);
		 intent.putExtra("DeviceID",          mSettings.getString("DeviceID",     "123"));
		 intent.putExtra("WebDomain",         mSettings.getString("WebDomain",    "192.168.1.67"));
		 //intent.putExtra("WebDomain",         mSettings.getString("WebDomain",    "HOME-PC"));
		 intent.putExtra("WebPort",           mSettings.getString("WebPort",      "80"));

		 PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent,PendingIntent.FLAG_CANCEL_CURRENT);

		 Calendar time = Calendar.getInstance();
		 time.setTimeInMillis(System.currentTimeMillis());
		 time.add(Calendar.SECOND, 5);

		 alarmMgr.cancel(pendingIntent);
		 alarmMgr.set(AlarmManager.RTC_WAKEUP, time.getTimeInMillis(), pendingIntent);
		 //TODO: - change to alarmMgr.setInexactRepeating and get time interval from preferences
		 //alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, 0, 0, pendingIntent);
	 }

	 protected void playNotification(String type){
		 Uri mNotification = null;
		 
		 if(type.equalsIgnoreCase("Intersection_Camera")){
			 mNotification = Uri.parse(mNotifyCamera);
		 } else if(type.equalsIgnoreCase("Photo_Radar")){
			 mNotification = Uri.parse(mNotifyPhoto);
		 } else if(type.equalsIgnoreCase("Traffic_Accident")){
			 mNotification = Uri.parse(mNotifyTraffic);
		 }

		 if(mNotification != null){
			 try{
				Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), mNotification);
				r.play();
			 }catch(Exception ex)
			 {
				 Log.v("Main",	ex.getMessage());
			 }
		 }
	 }
	 
	 /**
	  * 
	  */
	 private class ScannerServiceReceiver extends BroadcastReceiver{

		 @Override
		 public void onReceive(Context context, Intent intent) {

			 if(intent.getAction().equals(ScannerService.ACTION_POI_DETECTED)) {
				 Bundle mBundle = intent.getExtras();
				 mPOI = mBundle.getParcelable("ca.radarlove.PointOfInterest");
				 
				 //TODO: Put any heavy processing in asynctask
				 //TODO: PoiOverlay needs to be unique for each type
				 List<Overlay> overlays = mMapView.getOverlays();
				 PoiOverlay mOverlay    = new PoiOverlay(mPOI);
				 overlays.add(mOverlay);

				 mMapView.invalidate();
				 
				 //TODO: Add method to keep track of number of notifications/poi so as to avoid being annoying
				 playNotification(mPOI.getType());
				 
				 //TODO: Add more sophisticated visual cues				 
				 Toast.makeText(context, mPOI.getComment(), Toast.LENGTH_LONG).show();
				 
				 

			 }
		 }
		 
	 }
}