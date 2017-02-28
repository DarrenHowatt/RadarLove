package ca.radarlove.receivers;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import ca.radarlove.services.DownloadService;
import ca.radarlove.services.ScannerService;


public class RadarLoveReceiver extends BroadcastReceiver {
	public static final String ACTION_DOWNLOAD_POI = DownloadService.ACTION_DOWNLOAD_POI;
	public static final String ACTION_POI_DETECTED = ScannerService.ACTION_POI_DETECTED;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		
		if (intent.getAction().equals(DownloadService.ACTION_DOWNLOAD_POI)) {
	        
			Intent mDownloadIntent = new Intent(context, DownloadService.class);
			
			mDownloadIntent.setAction(DownloadService.ACTION_DOWNLOAD_POI);
			
	        mDownloadIntent.putExtra("DeviceID",          intent.getStringExtra("DeviceID"));
	        mDownloadIntent.putExtra("WebDomain",         intent.getStringExtra("WebDomain"));
	        mDownloadIntent.putExtra("WebPort",           intent.getStringExtra("WebPort"));
	        mDownloadIntent.putExtra("LastRecordID",      intent.getStringExtra("LastRecordID"));
	        	
	        context.startService(mDownloadIntent);
		}
	}

}
