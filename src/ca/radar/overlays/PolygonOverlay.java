package ca.radar.overlays;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;

import ca.radarlove.Polygon;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

public class PolygonOverlay extends Overlay {
	Polygon mPolygon;
	
	public PolygonOverlay(Polygon polygon) {
		mPolygon = polygon;
	}

	@Override
	public boolean draw(Canvas canvas, MapView mapView, boolean shadow, long when) {
		
		if (shadow || mPolygon == null){
			return false;
		}
		
		super.draw(canvas, mapView, shadow, when);
		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

	    paint.setStrokeWidth(2);
	    paint.setARGB(100, 156, 192, 36);
	    //paint.setColor(android.graphics.Color.RED);     
	    paint.setStyle(Paint.Style.FILL_AND_STROKE);
	    paint.setAntiAlias(true);

        // get coordinates
        double[] lats       = mPolygon.getLatitudes();
        double[] longs      = mPolygon.getLongitudes();
        int mNumVertices    = mPolygon.getNumberofVertices();
        GeoPoint[] mGPoints = new GeoPoint[mNumVertices];
        Point[] mPoints     = new Point[mNumVertices];
        
        for (int i = 0; i < mNumVertices; i++) {
        	mGPoints[i] = new GeoPoint((int) (lats[i] * 1E6), (int) (longs[i]  * 1E6));
        	mPoints[i]  = mapView.getProjection().toPixels(mGPoints[i], mPoints[i]);
        }
        
	    Path path = new Path();
	    path.setFillType(Path.FillType.EVEN_ODD);
	    path.moveTo(mPoints[0].x,mPoints[0].y);
	    path.lineTo(mPoints[1].x,mPoints[1].y);
	    path.lineTo(mPoints[2].x,mPoints[2].y);
	    path.lineTo(mPoints[0].x,mPoints[0].y);
	    path.close();

	    canvas.drawPath(path, paint);
	    mapView.postInvalidate();
	    
	    return true;
	}
}
