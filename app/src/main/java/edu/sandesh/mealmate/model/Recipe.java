package edu.sandesh.mealmate.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Recipe implements Parcelable {
    private String recipeName;
    private String cookTime;
    private String photoUrl;
    private Map<String, List<String>> ingredients;
    private List<Map<String, Object>> instructions;
    private long timestamp;

    // Default constructor required for Firestore
    public Recipe() {}

    public Recipe(String recipeName, String cookTime, String photoUrl,
                  Map<String, List<String>> ingredients, List<Map<String, Object>> instructions, long timestamp) {
        this.recipeName = recipeName;
        this.cookTime = cookTime;
        this.photoUrl = photoUrl;
        this.ingredients = ingredients != null ? ingredients : new HashMap<>();
        this.instructions = instructions != null ? instructions : new ArrayList<>();
        this.timestamp = timestamp;
    }

    // Parcelable implementation
    protected Recipe(Parcel in) {
        recipeName = in.readString();
        cookTime = in.readString();
        photoUrl = in.readString();
        ingredients = in.readHashMap(List.class.getClassLoader());
        instructions = in.readArrayList(Map.class.getClassLoader());
        timestamp = in.readLong();
    }

    public static final Creator<Recipe> CREATOR = new Creator<Recipe>() {
        @Override
        public Recipe createFromParcel(Parcel in) {
            return new Recipe(in);
        }

        @Override
        public Recipe[] newArray(int size) {
            return new Recipe[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(recipeName);
        dest.writeString(cookTime);
        dest.writeString(photoUrl);
        dest.writeMap(ingredients);
        dest.writeList(instructions);
        dest.writeLong(timestamp);
    }

    // Getters and Setters
    public String getRecipeName() { return recipeName; }
    public void setRecipeName(String recipeName) { this.recipeName = recipeName; }

    public String getCookTime() { return cookTime; }
    public void setCookTime(String cookTime) { this.cookTime = cookTime; }

    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }

    public Map<String, List<String>> getIngredients() { return ingredients; }
    public void setIngredients(Map<String, List<String>> ingredients) {
        this.ingredients = ingredients != null ? ingredients : new HashMap<>();
    }

    public List<Map<String, Object>> getInstructions() { return instructions; }
    public void setInstructions(List<Map<String, Object>> instructions) {
        this.instructions = instructions != null ? instructions : new ArrayList<>();
    }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}