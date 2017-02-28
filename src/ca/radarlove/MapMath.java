package ca.radarlove;

import android.util.FloatMath;

@SuppressWarnings("unused")
public class MapMath {
	private static final int   EARTH_RADIUS_KM = 6371;
	private static final float PI_OVER_180 = 0.017453292519943295769236907684886f;
	private static final double LAT_MIN = Math.toRadians(-90d);  // -PI/2
	private static final double LAT_MAX = Math.toRadians(90d);   //  PI/2
	private static final double LONG_MIN = Math.toRadians(-180d); // -PI
	private static final double LONG_MAX = Math.toRadians(180d);  //  PI

	/**
	 * 
	 */
	public static boolean isPointInPolygon(int nvert, double[] vertx, double[] verty, double testx, double testy)
	{
		// Source: http://www.ecse.rpi.edu/Homepages/wrf/Research/Short_Notes/pnpoly.html
		//
		// nvert 	Number of vertices in the polygon.
		// vertx, verty 	Arrays containing the x- and y-coordinates of the polygon's vertices.
		// testx, testy	X- and y-coordinate of the test point.
	    //
	  boolean c = false;
	  int     i = 0;
	  int     j = 0;
	  
	  for (i = 0, j = nvert-1; i < nvert; j = i++) {
	    if ( ((verty[i]>testy) != (verty[j]>testy)) && (testx < (vertx[j]-vertx[i]) * (testy-verty[i]) / (verty[j]-verty[i]) + vertx[i]) )
	       c =  !c;
	  }
	  return c;
	}

	/**
	 * 
	 */
    public static double[] getEndCoordinate(double startDegreesLatitude, double startDegreesLongitude, double distance, double bearing)
    {
    	// Source:    http://www.movable-type.co.uk/scripts/latlong.html
    	//            Destination point given distance and bearing from start point
    	//
        //Formula: 	  lat2 = asin(sin(lat1)*cos(d/R) + cos(lat1)*sin(d/R)*cos(bearng))
        //            lon2 = lon1 + atan2(sin(bearing)*sin(d/R)*cos(lat1), cos(d/R)-sin(lat1)*sin(lat2))
        //            bearing is in radians, clockwise from north;
        //            d/R is the angular distance (in radians), where d is the distance travelled and R is the earth’s radius
    	//            For final bearing, take the initial bearing from the end point to the 
    	//            start point and reverse it (using bearing = (bearing+180) % 360)
    	//
        double d    = distance;// km
        int R       = EARTH_RADIUS_KM;// km
        double lat1 = DegreeToRadian(startDegreesLatitude);
        double lon1 = DegreeToRadian(startDegreesLongitude);
        double brng = DegreeToRadian(bearing);
        
        double lat2 = Math.asin( Math.sin(lat1) * Math.cos(d/R) + Math.cos(lat1) * Math.sin(d/R) * Math.cos(brng) );
        double lon2 = lon1 + Math.atan2(Math.sin(brng) * Math.sin(d/R) * Math.cos(lat1), Math.cos(d/R) - Math.sin(lat1) * Math.sin(lat2));

        double[] newCoord = new double[2];
        newCoord[0]       = RadianToDegree(lat2);
        newCoord[1]       = RadianToDegree(lon2);
        
        
        return newCoord;
    }
    
	/**
	 * 
	 */
    public static double getDistanceBetweenPoints(double startDegreesLatitude,double startDegreesLongitude,
    										   double endDegreesLatitude,double endDegreesLongitude)
    {
    	// Source: http://www.movable-type.co.uk/scripts/latlong.html
    	//         Spherical Law of Cosines
    	//
        double d    = 0; // same units as R
        int R       = EARTH_RADIUS_KM;// km
        double lat1 = DegreeToRadian(startDegreesLatitude);
        double lon1 = DegreeToRadian(startDegreesLongitude);
        double lat2 = DegreeToRadian(endDegreesLatitude);
        double lon2 = DegreeToRadian(endDegreesLongitude);
        
    	d = Math.acos(Math.sin(lat1)*Math.sin(lat2) + Math.cos(lat1)*Math.cos(lat2) * Math.cos(lon2-lon1)) * R;
    	
    	return d*1000; // m
    }
  
	/**
	 * 
	 */
    public static double getBearingToDestination(double startDegreesLatitude, double startDegreesLongitude, 
    										  double endDegreesLatitude, double endDegreesLongitude)
    {
    	// Source: 
        double lat1 = DegreeToRadian(startDegreesLatitude);
        double lon1 = DegreeToRadian(startDegreesLongitude);
        double lat2 = DegreeToRadian(endDegreesLatitude);
        double lon2 = DegreeToRadian(endDegreesLongitude);
        double dLon = lon2 - lon1;
        
    	double y = Math.sin(dLon) * Math.cos(lat2);
    	double x = Math.cos(lat1)*Math.sin(lat2) - Math.sin(lat1)*Math.cos(lat2)*Math.cos(dLon);
    	double brng = RadianToDegree(Math.atan2(y, x));
    	
    	return brng;
    }
	
/* 
 * 	Step 1. Define r = d/R as the angular radius of the query circle.
 * 				where: d = size of circle to be bound  (radius of query circle) 
 *                     R = average radius of Earth
 *              so r =  (1000 km)/(6371 km) = 0.1570 
 *     
 *  Step 2. Find the min and max possible Latitude; ignoring possiblity that either pole is in the search area   
 *          latMin = lat - r
 *          latMax = lat + r
 *          
 *  Step 3. Find the min and max Longitude
 *  		longDelta = asin(sin(r)/cos(lat))
 *  		longMin   = long - longDelta
 *  		longMax   = long + longDelta
 *  
 *  Step 4.	Handle the poles and the 180 meridian
 *  		-If latMax > PI/2 then north pole is in the query circle so: 
 *  		bounding coord are (latMin, -PI) and (PI/2,PI)
 *          -If latMin < -PI/2 then south pole is in query circle so:
 *          bounding coord as (-PI/2,-PI) and (latMax, PI)
 *          -If longMin or longMax is outside range [-PI,PI], the 180 meridian is in the query circle so:
 *          bounding coord as (latMin,-PI) and (latMax,PI) 
 *          
 *  Source: http://janmatuschek.de/LatitudeLongitudeBoundingCoordinates
 */  
/**
 * @param distance in km
 */
   public static double[] getBoundingCoordinates(double distance, double latitude, double longitude){
	   double[] mResult = new double[4];
	   int mR       = EARTH_RADIUS_KM;     // km
       double mD    = distance;            // same units as mR  
       double mLat  = DegreeToRadian(latitude);
       double mLong = DegreeToRadian(longitude);
       
       //Step 1.
       double mAngularRadius = mD/mR;
       
       //Step 2.
       double mLatMin = mLat - mAngularRadius;
       double mLatMax = mLat + mAngularRadius;
       
       //Step 3 & 4.
		double mLongMin;
		double mLongMax;       

		if (mLatMin > LAT_MIN && mLatMax < LAT_MAX) {
			double mLongDelta = Math.asin(Math.sin(mAngularRadius)/Math.cos(mLat));
			mLongMin   = mLong - mLongDelta;
			mLongMax   = mLong + mLongDelta;
			
			if (mLongMin < LONG_MIN){
				mLongMin += 2d * Math.PI;
			}
			
			if (mLongMax > LONG_MAX){
				mLongMax -= 2d * Math.PI;
			}
		} else {
			// a pole is within the distance
			mLatMin  = Math.max(mLatMin, LAT_MIN);
			mLatMax  = Math.min(mLatMax, LAT_MAX);
			mLongMin = LONG_MIN;
			mLongMax = LONG_MAX;
		}

		mResult[0] = RadianToDegree(mLatMin);
		mResult[1] = RadianToDegree(mLatMax);
		mResult[2] = RadianToDegree(mLongMin);
		mResult[3] = RadianToDegree(mLongMax);
		
		return mResult;
   }
 
	/**
	 * 
	 */
    private static double DegreeToRadian(double angle)
    {
        return Math.toRadians(angle);
    }

	/**
	 * 
	 */
    private static double RadianToDegree(double angle)
    {
        return Math.toDegrees(angle);
    }
	
	
}
