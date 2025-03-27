package edu.sandesh.mealmate.model;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.List;

public class SavedLocation implements Parcelable {
    private String name;
    private String imageUrl;
    private double latitude;
    private double longitude;
    private String distance;
    private List<String> availableIngredients;
    private int matchingCount;
    private List<String> matchedIngredients;
    private String address;

    public SavedLocation(String name, String imageUrl, String address, double latitude, double longitude, String distance, List<String> availableIngredients, int matchingCount) {
        this.name = name;
        this.imageUrl = imageUrl;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.distance = distance;
        this.availableIngredients = availableIngredients;
        this.matchingCount = matchingCount;
    }

    // Getter methods
    public String getName() {
        return name;
    }

    public String getAddress(){
        return address;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getDistance() {
        return distance;
    }

    public List<String> getAvailableIngredients() {
        return availableIngredients;
    }

    public int getMatchingCount() {
        return matchingCount;
    }

    public List<String> getMatchedIngredients() {
        return matchedIngredients;
    }

    public void setMatchedIngredients(List<String> matchedIngredients) {
        this.matchedIngredients = matchedIngredients;
    }

    // Parcelable implementation
    protected SavedLocation(Parcel in) {
        name = in.readString();
        imageUrl = in.readString();
        latitude = in.readDouble();
        longitude = in.readDouble();
        distance = in.readString();
        availableIngredients = in.createStringArrayList();
        matchingCount = in.readInt();
        matchedIngredients = in.createStringArrayList();
        address = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(imageUrl);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeString(distance);
        dest.writeStringList(availableIngredients);
        dest.writeInt(matchingCount);
        dest.writeStringList(matchedIngredients);
        dest.writeString(address);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<SavedLocation> CREATOR = new Creator<SavedLocation>() {
        @Override
        public SavedLocation createFromParcel(Parcel in) {
            return new SavedLocation(in);
        }

        @Override
        public SavedLocation[] newArray(int size) {
            return new SavedLocation[size];
        }
    };
}
