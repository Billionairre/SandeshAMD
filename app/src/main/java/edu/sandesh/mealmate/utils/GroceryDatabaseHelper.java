package edu.sandesh.mealmate.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class GroceryDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "grocery.db";
    private static final int DATABASE_VERSION = 1;

    // Table and Columns
    private static final String TABLE_GROCERY = "grocery_items";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_CATEGORY = "category";
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_IS_PURCHASED = "isPurchased";

    public GroceryDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_GROCERY + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_NAME + " TEXT, " +
                COLUMN_CATEGORY + " TEXT DEFAULT 'Wish List', " +
                COLUMN_DATE + " TEXT, " +
                COLUMN_IS_PURCHASED + " INTEGER DEFAULT 0)";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GROCERY);
        onCreate(db);
    }

    // Add Grocery Item
    public void addGroceryItem(String name, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_DATE, date);
        values.put(COLUMN_CATEGORY, "Wish List");
        db.insert(TABLE_GROCERY, null, values);
        db.close();
    }

    public void addGroceryItem(String name, String date,String category) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_DATE, date);
        values.put(COLUMN_CATEGORY, category);
        db.insert(TABLE_GROCERY, null, values);
        db.close();
    }

    // Get Grocery Items by Date
    public Map<String, List<String>> getGroceryItemsByDate(String date) {
        Map<String, List<String>> groceryMap = new HashMap<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_GROCERY,
                new String[]{COLUMN_NAME, COLUMN_CATEGORY, COLUMN_IS_PURCHASED},
                COLUMN_DATE + "=?",
                new String[]{date},
                null, null, null);

        if (cursor.moveToFirst()) {
            do {
                String category = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME));
                if (!groceryMap.containsKey(category)) {
                    groceryMap.put(category, new ArrayList<>());
                }
                groceryMap.get(category).add(name);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return groceryMap;
    }

    // Get Today's Date
    public String getTodayDate() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }
    public String getTomorrowDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Date today = new Date();
        long tomorrowMillis = today.getTime() + (1000 * 60 * 60 * 24);
        return sdf.format(new Date(tomorrowMillis));
    }

    // Update Purchased Status of Item
    // Update Purchased Status of Item for a Specific Date
    public void updateItemPurchasedStatus(String itemName, String date, boolean isPurchased) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_IS_PURCHASED, isPurchased ? 1 : 0);
        db.update(TABLE_GROCERY, values, COLUMN_NAME + "=? AND " + COLUMN_DATE + "=?",
                new String[]{itemName, date});
        db.close();
    }

    // Check if Item is Purchased
    // Check if Item is Purchased for a Specific Date
    public boolean isItemPurchased(String itemName, String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_GROCERY,
                new String[]{COLUMN_IS_PURCHASED},
                COLUMN_NAME + "=? AND " + COLUMN_DATE + "=?",
                new String[]{itemName, date},
                null, null, null);

        boolean isPurchased = false;
        if (cursor.moveToFirst()) {
            isPurchased = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_PURCHASED)) == 1;
        }
        cursor.close();
        db.close();
        return isPurchased;
    }


    // Check if Item Exists for Given Date
    public boolean isItemExistsForDate(String itemName, String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_GROCERY,
                new String[]{COLUMN_NAME},
                COLUMN_NAME + "=? AND " + COLUMN_DATE + "=?",
                new String[]{itemName, date},
                null, null, null);

        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }


    // Get Grocery Items for Week (Today + Next 7 Days)
    public Map<String, Map<String, List<String>>> getGroceryItemsForWeek() {
        Map<String, Map<String, List<String>>> weeklyGroceryMap = new HashMap<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Get today's date
        String todayDate = getTodayDate();

        // Get the date 7 days from today
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.add(java.util.Calendar.DAY_OF_YEAR, 7);
        String endDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.getTime());

        // Query for items between today and the next 7 days
        String query = "SELECT " + COLUMN_NAME + ", " + COLUMN_CATEGORY + ", " + COLUMN_DATE + ", " + COLUMN_IS_PURCHASED +
                " FROM " + TABLE_GROCERY +
                " WHERE " + COLUMN_DATE + " BETWEEN ? AND ? ORDER BY " + COLUMN_DATE + " ASC";

        Cursor cursor = db.rawQuery(query, new String[]{todayDate, endDate});

        if (cursor.moveToFirst()) {
            do {
                String itemName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME));
                String category = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY));
                String date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE));
                boolean isPurchased = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_PURCHASED)) == 1;

                // Log the data for debugging
                System.out.println("Category: " + category + ", Item: " + itemName + ", Date: " + date + ", Purchased: " + isPurchased);

                // Group by Category then by Date
                if (!weeklyGroceryMap.containsKey(category)) {
                    weeklyGroceryMap.put(category, new HashMap<>());
                }
                Map<String, List<String>> dateMap = weeklyGroceryMap.get(category);

                // Group by Date under the same Category
                if (!dateMap.containsKey(date)) {
                    dateMap.put(date, new ArrayList<>());
                }
                dateMap.get(date).add(itemName + "|" + isPurchased); // Append purchased status to the item name
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return weeklyGroceryMap;
    }



    public Map<String, Map<String, List<String>>> getGroceryItemsForWeekUnpurchased() {
        Map<String, Map<String, List<String>>> weeklyGroceryMap = new HashMap<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Get today's date
        String todayDate = getTodayDate();

        // Get the date 7 days from today
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.add(java.util.Calendar.DAY_OF_YEAR, 7);
        String endDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.getTime());

        // Query for unpurchased items between today and the next 7 days
        String query = "SELECT " + COLUMN_NAME + ", " + COLUMN_CATEGORY + ", " + COLUMN_DATE +
                " FROM " + TABLE_GROCERY +
                " WHERE " + COLUMN_DATE + " BETWEEN ? AND ? " +
                " AND " + COLUMN_IS_PURCHASED + " = 0" +   // Only Unpurchased Items
                " ORDER BY " + COLUMN_DATE + " ASC";

        Cursor cursor = db.rawQuery(query, new String[]{todayDate, endDate});

        if (cursor.moveToFirst()) {
            do {
                String itemName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME));
                String category = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY));
                String date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE));

                // Group by Category then by Date
                if (!weeklyGroceryMap.containsKey(category)) {
                    weeklyGroceryMap.put(category, new HashMap<>());
                }
                Map<String, List<String>> dateMap = weeklyGroceryMap.get(category);

                // Group by Date under the same Category
                if (!dateMap.containsKey(date)) {
                    dateMap.put(date, new ArrayList<>());
                }
                dateMap.get(date).add(itemName);

            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return weeklyGroceryMap;
    }


    public void removeGroceryItem(String itemName, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_GROCERY, COLUMN_NAME + "=? AND " + COLUMN_DATE + "=?", new String[]{itemName, date});
        db.close();
    }

    public void clearGroceryData() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_GROCERY);
        db.close();
    }

    // Get Total Count of Grocery Items
    // Get Total Count of Grocery Items for the Week (Today + Next 7 Days)
    public int getWeeklyGroceryItemCount() {
        SQLiteDatabase db = this.getReadableDatabase();

        // Get today's date
        String todayDate = getTodayDate();

        // Get the date 7 days from today
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.add(java.util.Calendar.DAY_OF_YEAR, 7);
        String endDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.getTime());

        // Query for total count between today and the next 7 days
        String query = "SELECT COUNT(*) FROM " + TABLE_GROCERY +
                " WHERE " + COLUMN_DATE + " BETWEEN ? AND ?";

        Cursor cursor = db.rawQuery(query, new String[]{todayDate, endDate});

        int totalCount = 0;
        if (cursor.moveToFirst()) {
            totalCount = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return totalCount;
    }


    public boolean hasGroceryDataForWeek() {
        SQLiteDatabase db = this.getReadableDatabase();

        // Get today's date
        String todayDate = getTodayDate();

        // Get the date 7 days from today
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.add(java.util.Calendar.DAY_OF_YEAR, 7);
        String endDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.getTime());

        // Query to check if there is any data between today and the next 7 days
        String query = "SELECT COUNT(*) FROM " + TABLE_GROCERY +
                " WHERE " + COLUMN_DATE + " BETWEEN ? AND ?";

        Cursor cursor = db.rawQuery(query, new String[]{todayDate, endDate});

        boolean hasData = false;
        if (cursor.moveToFirst()) {
            hasData = cursor.getInt(0) > 0;
        }
        cursor.close();
        db.close();
        return hasData;
    }

    /**
     * Get grocery items for a specific day, grouped by category
     * 
     * @param date The specific date to retrieve items for
     * @return A map with categories as keys and maps of dates to list of items as values
     */
    public Map<String, Map<String, List<String>>> getGroceryItemsForDay(String date) {
        Map<String, Map<String, List<String>>> dailyGroceryMap = new HashMap<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Query for all items for the specified date
        String query = "SELECT " + COLUMN_NAME + ", " + COLUMN_CATEGORY + ", " + COLUMN_DATE + ", " + COLUMN_IS_PURCHASED +
                " FROM " + TABLE_GROCERY +
                " WHERE " + COLUMN_DATE + " = ?" +
                " ORDER BY " + COLUMN_CATEGORY + " ASC";

        Cursor cursor = db.rawQuery(query, new String[]{date});

        if (cursor.moveToFirst()) {
            do {
                String itemName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME));
                String category = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY));
                boolean isPurchased = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_PURCHASED)) == 1;

                // Group by Category
                if (!dailyGroceryMap.containsKey(category)) {
                    dailyGroceryMap.put(category, new HashMap<>());
                }
                
                Map<String, List<String>> dateMap = dailyGroceryMap.get(category);

                // Use the category as the key in the inner map
                if (!dateMap.containsKey(date)) {
                    dateMap.put(date, new ArrayList<>());
                }
                
                // Add item to the list with purchase status
                dateMap.get(date).add(itemName + (isPurchased ? " (Purchased)" : ""));
                
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return dailyGroceryMap;
    }

    // Update Grocery Item
    public void updateGroceryItem(String oldName, String newName, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, newName);
        db.update(TABLE_GROCERY, values, 
                COLUMN_NAME + "=? AND " + COLUMN_DATE + "=?",
                new String[]{oldName, date});
        db.close();
    }

    // Update Grocery Item Category
    public void updateGroceryItemCategory(String itemName, String newCategory, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CATEGORY, newCategory);
        db.update(TABLE_GROCERY, values,
                COLUMN_NAME + "=? AND " + COLUMN_DATE + "=?",
                new String[]{itemName, date});
        db.close();
    }

    // Get all categories
    public List<String> getAllCategories() {
        List<String> categories = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(true, TABLE_GROCERY,
                new String[]{COLUMN_CATEGORY},
                null, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                String category = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY));
                if (!categories.contains(category)) {
                    categories.add(category);
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return categories;
    }

}
