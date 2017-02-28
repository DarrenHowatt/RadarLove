package ca.radarlove;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

public class Polygon implements Parcelable{


	private double [] mLatitudes;
	private double [] mLongitudes;
	private int       mBearingRange        = 15;
	private double    mProjectedBearing    = 0d;
	private double	  mBearingAdj          = 0d;
	public Location   mLocation            = null;
	
	public Polygon(){}
	
	public Polygon(Parcel in){
		readFromParcel(in);
	}
	
	public void calculate(){

		double [] lngs  = new double[4];
		double [] lats  = new double[4];
		double [] tmp   = new double[2];
		double mcurLat;
		double mcurLong;
		double mcurBearing;
		double mcurSpeed;
		double mForwardDistance;
		
		if(mLocation != null){
			mcurLat             = mLocation.getLatitude();
			mcurLong            = mLocation.getLongitude();
			mcurBearing         = mLocation.getBearing();
			mcurSpeed           = mLocation.getSpeed();  // in metres per second
			mForwardDistance    = mcurSpeed*30/1000;     // (metres/sec)*(60 sec/minute)= metres/minute then / 1000 to = kms/minute
			
			if(mBearingAdj < -15 || mBearingAdj > 15) mBearingAdj =0; // this assumes a +45 or -45 in the calculation of bearing adj
			
			lats[0] = mcurLat;
			lngs[0] = mcurLong;
	
			tmp = MapMath.getEndCoordinate(mcurLat, mcurLong, mForwardDistance, mcurBearing + (mBearingAdj*2) + mBearingRange);
			lats[1] = tmp[0];
			lngs[1] = tmp[1];
			
			tmp = MapMath.getEndCoordinate(mcurLat, mcurLong, mForwardDistance, mcurBearing + (mBearingAdj*2) - mBearingRange);
			lats[2] = tmp[0];
			lngs[2] = tmp[1];
			
			// start and end will be the same
			lats[3] = mcurLat;
			lngs[3] = mcurLong; 
			
			mLatitudes  = lats;
			mLongitudes = lngs;
		}
		else
		{
			//TODO: throw an Exception("Location must be set before calling this Calculate method");
		}
	}

	/**
	 * @param distance in Kms
	 * @returns double[] 0=LatMin; 1=LatMax; 2=LongMin; 3=LongMax
	 */
	public double[] getBoundingSquare(double distance) throws Exception{
		double[] sqr = new double[4];
		
		if(mLatitudes.length==0){
			throw new Exception("Run calculate method first.");
		}
		
		sqr = MapMath.getBoundingCoordinates(distance, mLatitudes[0], mLongitudes[0]);
		
		return sqr;
	}

	/**
	 * @param distance in Kms
	 * @returns String[] 0=LatMin; 1=LongMin; 2=LatMax; 3=LongMax
	 */
	public String[] getBoundingSquareAsString(double distance) {
		double[] mCoordAsDouble = new double[4];
		String[] mCoordAsString = new String[4];
		
		try{
			mCoordAsDouble = getBoundingSquare(distance);
			
			mCoordAsString[0]=String.valueOf(mCoordAsDouble[0]);
			mCoordAsString[1]=String.valueOf(mCoordAsDouble[1]);
			mCoordAsString[2]=String.valueOf(mCoordAsDouble[2]);
			mCoordAsString[3]=String.valueOf(mCoordAsDouble[3]);
		}
		catch(Exception ex){
			
		}

		
		return mCoordAsString;

	}
	
	public Location getLocation() {
		return mLocation;
	}

	public void setLocation(Location location) {
		this.mLocation = location;
	}
	
	public double[] getLatitudes() {
		return mLatitudes;
	}

	public void setLatitudes(double[] latitudes) {
		this.mLatitudes = latitudes;
	}

	public double[] getLongitudes() {
		return mLongitudes;
	}

	public void setLongitudes(double[] longitudes) {
		this.mLongitudes = longitudes;
	}

	public int getBearingRange() {
		return mBearingRange;
	}

	public void setBearingRange(int bearingRange) {
		this.mBearingRange = bearingRange;
	}

	public double getProjectedBearing() {
		return mProjectedBearing;
	}

	public void setProjectedBearing(double projectedBearing) {
		this.mProjectedBearing = projectedBearing;
	}

	public double getBearingAdj() {
		return mBearingAdj;
	}

	public void setBearingAdj(double bearingAdj) {
		this.mBearingAdj = bearingAdj;
	}
	
	public int getNumberofVertices() {
		return mLatitudes.length-1;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
 
		// We just need to write each field into the
		// parcel. When we read from parcel, they
		// will come back in the same order
		dest.writeDoubleArray(mLatitudes);
		dest.writeDoubleArray(mLongitudes);
		dest.writeInt(mBearingRange);
		dest.writeDouble(mProjectedBearing);
		mLocation.writeToParcel(dest, flags);

	}
 
	/**
	 *
	 * Called from the constructor to create this
	 * object from a parcel.
	 *
	 * @param in parcel from which to re-create object
	 */
	private void readFromParcel(Parcel in) {
 
		// We just need to read back each
		// field in the order that it was
		// written to the parcel	
		mLatitudes        = in.createDoubleArray();//in.readDoubleArray(mLatitudes);
		mLongitudes       = in.createDoubleArray();//in.readDoubleArray(mLongitudes);
		mBearingRange     = in.readInt();
		mProjectedBearing = in.readDouble();
		mLocation         = Location.CREATOR.createFromParcel(in);

	}
 
    public static final Parcelable.Creator<Polygon> CREATOR = new Parcelable.Creator<Polygon>() {
            public Polygon createFromParcel(Parcel in) {
                return new Polygon(in);
            }
 
            public Polygon[] newArray(int size) {
                return new Polygon[size];
            }
        };

	@Override
	public int describeContents() {
		return 0;
	}

}
