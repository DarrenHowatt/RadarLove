package ca.radarlove;

public class LocationChangeHistory {
	// Source: http://stackoverflow.com/questions/3793400/is-there-a-function-in-java-to-get-moving-average
    private int    mSize;
    // Latitude
    private int    mLatitudeTotal = 0;
    private int    mLatitudeIndex = 0;
    private double mLatitudeSamples[];
    // Longitude
    private int    mLongitudeTotal = 0;
    private int    mLongitudeIndex = 0;
    private double mLongitudeSamples[];   
    // Bearing
    private int    mBearingTotal = 0;
    private int    mBearingIndex = 0;
    private double mBearingSamples[];
    // Speed
    private int    mSpeedTotal = 0;
    private int    mSpeedIndex = 0;
    private double mSpeedSamples[];
    
    public LocationChangeHistory(int size) {
        this.mSize = size;
        
        // Latitude
        mLatitudeSamples = new double[size];
        for (int i = 0; i < size; i++) mLatitudeSamples[i] = 0;
        
        // Longitude
        mLongitudeSamples = new double[size];
        for (int i = 0; i < size; i++) mLongitudeSamples[i] = 0;       
        
        // Bearing
        mBearingSamples = new double[size];
        for (int i = 0; i < size; i++) mBearingSamples[i] = 0;        
        
        //Speed
        mSpeedSamples = new double[size];
        for (int i = 0; i < size; i++) mSpeedSamples[i] = 0;         
        
    }

    public void addLatitude(double latitude) {
        mLatitudeTotal -= mLatitudeSamples[mLatitudeIndex];
        mLatitudeSamples[mLatitudeIndex] = latitude;
        mLatitudeTotal += latitude;
        if (++mLatitudeIndex == mSize) mLatitudeIndex = 0;
    }

    public double getAverageLatitude() {
        return mLatitudeTotal / mSize;
    }   
    
    public void addLongitude(double longitude) {
        mLongitudeTotal -= mLongitudeSamples[mLongitudeIndex];
        mLongitudeSamples[mLongitudeIndex] = longitude;
        mLongitudeTotal += longitude;
        if (++mLongitudeIndex == mSize) mLongitudeIndex = 0;
    }

    public double getAverageLongitude() {
        return mLongitudeTotal / mSize;
    } 
    
    public void addBearing(double bearing) {
        mBearingTotal -= mBearingSamples[mBearingIndex];
        mBearingSamples[mBearingIndex] = bearing;
        mBearingTotal += bearing;
        if (++mBearingIndex == mSize) mBearingIndex = 0;
    }

    public double getAverageBearing() {
        return mBearingTotal / mSize;
    }  

    public double getProjectedBearing(double latitude, double longitude) {
        double mProjectedLatitude  = latitude + (mLatitudeTotal/mSize);
        double mProjectedLongitude = longitude + (mLongitudeTotal/mSize);
    	double mProjectedBearing   = MapMath.getBearingToDestination(latitude, longitude, mProjectedLatitude, mProjectedLongitude);
    	
    	return mProjectedBearing;
    }
    
    public void addSpeed(double speed) {
        mSpeedTotal -= mSpeedSamples[mSpeedIndex];
        mSpeedSamples[mSpeedIndex] = speed;
        mSpeedTotal += speed;
        if (++mSpeedIndex == mSize) mSpeedIndex = 0; // cheaper than modulus
    }

    public double getAverageSpeed() {
        return mSpeedTotal / mSize;
    }
}
