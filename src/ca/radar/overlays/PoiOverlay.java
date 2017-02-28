package ca.radar.overlays;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;

import ca.radarlove.PointOfInterest;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

public class PoiOverlay extends Overlay {
	PointOfInterest mPOI;
	public PoiOverlay(PointOfInterest POI) {
		mPOI = POI;
	}

	@Override
	public boolean draw(Canvas canvas, MapView mapView, boolean shadow, long when) {
		
		if (shadow || mPOI == null){
			return false;
		}
		
		super.draw(canvas, mapView, shadow, when);
		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

	    paint.setStrokeWidth(2);
	    //paint.setARGB(100, 156, 192, 36);
	    paint.setColor(android.graphics.Color.RED);     
	    paint.setStyle(Paint.Style.FILL_AND_STROKE);
	    paint.setAntiAlias(true);

        // get coordinates
	    
        double lat = mPOI.getLatitude();
        double lng = mPOI.getLongitude();
        Point mSPoint = null;
        GeoPoint mGPoint = new GeoPoint((int) (lat * 1E6), (int) (lng  * 1E6));
        mSPoint = mapView.getProjection().toPixels(mGPoint, mSPoint);
        
	    canvas.drawCircle(mSPoint.x, mSPoint.y, 7, paint);

	    mapView.postInvalidate();
	    
	    return true;
	}
}
