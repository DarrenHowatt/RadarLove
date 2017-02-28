package ca.radarlove;

import android.os.Parcel;
import android.os.Parcelable;

public class PointOfInterest implements Parcelable{

    private int     mPOIId;
    private String  mName;
    private String  mType;
    private String  mStartDateTime;
    private String  mEndDateTime;
    private double  mLatitude;
    private double  mLongitude;
    private String  mComment;

    public PointOfInterest(){ }
	
	public PointOfInterest(Parcel in){
		readFromParcel(in);
	}
	
	public int getPOIId() {
		return mPOIId;
	}
	
	public void setPOIId(int mPointOfInterestId) {
		this.mPOIId = mPointOfInterestId;
	}
	
	 /**
	  * Name of user first reporting POI
	  */
	public String getName() {
		return mName;
	}
	
	public void setName(String name) {
		this.mName = name;
	}
	
	public String getType() {
		return mType;
	}
	
	public void setType(String mType) {
		this.mType = mType;
	}
	
	public String getStartDateTime() {
		return mStartDateTime;
	}
	
	public void setStartDateTime(String startDateTime) {
		this.mStartDateTime = startDateTime;
	}
	
	public String getEndDateTime() {
		return mEndDateTime;
	}
	
	public void setEndDateTime(String endDateTime) {
		this.mEndDateTime = endDateTime;
	}
	
	public double getLatitude() {
		return mLatitude;
	}
	
	public void setLatitude(double latitude) {
		this.mLatitude = latitude;
	}
	
	public double getLongitude() {
		return mLongitude;
	}
	
	public void setLongitude(double longitude) {
		this.mLongitude = longitude;
	}
	
	public String getComment() {
		return mComment;
	}
	
	public void setComment(String comment) {
		this.mComment = comment;
	}
	
	@Override
	public int describeContents() {
		return 0;
	}
 
	@Override
	public void writeToParcel(Parcel dest, int flags) {
 
		// We just need to write each field into the
		// parcel. When we read from parcel, they
		// will come back in the same order
		dest.writeInt   (mPOIId);
		dest.writeString(mName);
		dest.writeString(mType);
		dest.writeString(mStartDateTime);
		dest.writeString(mEndDateTime);
		dest.writeDouble(mLatitude);
		dest.writeDouble(mLongitude);
		dest.writeString(mComment);
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
	    mPOIId             = in.readInt();
	    mName              = in.readString();
	    mType              = in.readString();
	    mStartDateTime     = in.readString();
	    mEndDateTime       = in.readString();
	    mLatitude          = in.readDouble();
	    mLongitude         = in.readDouble();
	    mComment           = in.readString();
	}
 
    public static final Parcelable.Creator<PointOfInterest> CREATOR = new Parcelable.Creator<PointOfInterest>() {
            public PointOfInterest createFromParcel(Parcel in) {
                return new PointOfInterest(in);
            }
 
            public PointOfInterest[] newArray(int size) {
                return new PointOfInterest[size];
            }
        };
}
