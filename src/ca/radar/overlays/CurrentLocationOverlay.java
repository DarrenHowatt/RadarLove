package ca.radar.overlays;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.location.Location;
import ca.radarlove.LocationChangeHistory;
import ca.radarlove.Polygon;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Projection;

public class CurrentLocationOverlay extends MyLocationOverlay {

	  private MapController mc;
	  @SuppressWarnings("unused")
	  private Bitmap                mMarker;
	  private Point                 mCurrentPoint     = new Point();
	  private Polygon	            mPolygon          = new Polygon();
	  private LocationChangeHistory mLocChangeHistory = new LocationChangeHistory(3);
	  private Paint                 mPaint            = new Paint(Paint.ANTI_ALIAS_FLAG);
	  private Paint                 mBoundingPaint    = new Paint(Paint.ANTI_ALIAS_FLAG);
	  private Path                  mPath             = new Path();
	  private double	            mPrevLatitude     = 0d;
	  private double	            mPrevLongitude    = 0d;
	  private double	            mPrevBearing      = 0d;
	  private double	            mPrevSpeed        = 0d;
	  /**
	   * By default this CurrentLocationOverlay will centre on the current location, if the currentLocation is near the
	   * edge, or off the screen. To dynamically enable/disable this, use {@link #setCenterOnCurrentLocation(boolean)}.
	   *
	   * @param context
	   * @param mapView
	   */
	  public CurrentLocationOverlay(Context context, MapView mapView) {
	    super(context, mapView);
	    this.mc = mapView.getController();
	    
	    // set paint object here to reduce processing in drawMyLocation
		mPaint.setStrokeWidth(2);
		//mPaint.setARGB(100, 156, 192, 36); // light green     
		mPaint.setARGB(100, 255, 99, 71);   // tomato
		mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
		mPaint.setAntiAlias(true);

		mBoundingPaint.setStrokeWidth(2);
		mBoundingPaint.setARGB(100, 156, 192, 36); // light green     
		mBoundingPaint.setStyle(Paint.Style.FILL_AND_STROKE);
		mBoundingPaint.setAntiAlias(true);
		
	    //this.mMarker = BitmapFactory.decodeResource(context.getResources(), R.drawable.position);
	  }

	  @Override
	  protected void drawMyLocation(Canvas canvas, MapView mapView, Location lastFix, GeoPoint myLocation, long when) {
		  
		  
	    mapView.getProjection().toPixels(myLocation, mCurrentPoint);
	    //canvas.drawBitmap(mMarker, mCurrentPoint.x, mCurrentPoint.y - 40, null);

        // get coordinates
        double[] lats          = mPolygon.getLatitudes();
        double[] longs         = mPolygon.getLongitudes();
        int mNumVertices       = 3;        //mPolygon.getNumberofVertices();
        GeoPoint mGeoPoint     = null;     //new GeoPoint[mNumVertices];
        Point[] mPoints        = new Point[mNumVertices];
        Projection mProjection = mapView.getProjection();
        
        for (int i = 0; i < mNumVertices; i++) {
        	mGeoPoint   = new GeoPoint((int) (lats[i] * 1E6), (int) (longs[i]  * 1E6));
        	mPoints[i]  = mProjection.toPixels(mGeoPoint, mPoints[i]);
        }
	    
	    mPath.reset();
	    mPath.setFillType(Path.FillType.EVEN_ODD);
	    mPath.moveTo(mPoints[0].x,mPoints[0].y);
	    mPath.lineTo(mPoints[1].x,mPoints[1].y);
	    mPath.lineTo(mPoints[2].x,mPoints[2].y);
	    mPath.lineTo(mPoints[0].x,mPoints[0].y);
	    mPath.close();
	    
	    canvas.drawPath(mPath, mPaint);
	    
	    double[] mBounds = new double[4];
		try {
			mBounds = mPolygon.getBoundingSquare(1);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    Point mTopLeft        = new Point();
	    Point mBottomRight    = new Point();
	    
	    mProjection.toPixels(new GeoPoint((int) (mBounds[0] * 1E6),(int) (mBounds[2] * 1E6)), mTopLeft);
	    mProjection.toPixels(new GeoPoint((int) (mBounds[1] * 1E6),(int) (mBounds[3] * 1E6)), mBottomRight);
	    
	    Rect mBoundingRect = new Rect();
	    mBoundingRect.set(mTopLeft.x, mTopLeft.y, mBottomRight.x,mBottomRight.y);
	    canvas.drawCircle(mCurrentPoint.x, mCurrentPoint.y, mBoundingRect.width()/2, mBoundingPaint);
	    //canvas.drawRect(mBoundingRect, mBoundingPaint);
	    mGeoPoint = null;
	    mPoints   = null;
	  }

	  @Override
	  public synchronized void onLocationChanged(Location location) {
		super.onLocationChanged(location);
		mc.animateTo(getMyLocation());
		
		double mCurLatitude     = location.getLatitude();
		double mCurLongitude    = location.getLongitude();
		double mCurBearing 		= location.getBearing();
		double mCurSpeed        = location.getSpeed();
		
		mLocChangeHistory.addLatitude(mCurLatitude - mPrevLatitude);
		mLocChangeHistory.addLongitude(mCurLongitude - mPrevLongitude);
		mLocChangeHistory.addBearing(mCurBearing - mPrevBearing);
		mLocChangeHistory.addSpeed(mCurSpeed - mPrevSpeed);
		
		
		mPolygon.setLocation(location);
		//mPolygon.setProjectedBearing(mLocChangeHistory.getProjectedBearing(mCurLatitude, mCurLongitude));
		mPolygon.setBearingAdj(mLocChangeHistory.getAverageBearing());
		mPolygon.calculate();
		
		mPrevLatitude       = mCurLatitude;
		mPrevLongitude      = mCurLongitude;
		mPrevBearing 		= mCurBearing;
		mPrevSpeed          = mCurSpeed;

	  }
}
