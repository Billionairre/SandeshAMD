package edu.sandesh.mealmate.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IngredientConstants {

    // Category List
    public   List<String> CATEGORY_LIST = Arrays.asList(
            "🥦 Vegetables",
            "🍎 Fruits",
            "🌾 Grains & Legumes",
            "🍗 Proteins",
            "🧀 Dairy",
            "🌿 Herbs & Spices",
            "🛢️ Oils & Condiments"
    );

    // Ingredient Map as HashMap<String, List<String>>
           // ingredientMap.put("🆕 Others", new ArrayList<>()); // ✅ Allow dynamic user-added ingredients

    private  HashMap<String, List<String>> INGREDIENT_MAP;

     {
        INGREDIENT_MAP = new HashMap<>();
        INGREDIENT_MAP.put("🥦 Vegetables", Arrays.asList("🥕 Carrot", "🥦 Broccoli", "🌿 Spinach", "🍅 Tomato", "🧅 Onion", "🧄 Garlic", "🌶 Bell Pepper", "🥒 Zucchini", "🥬 Cabbage", "🥬 Kale", "🥗 Lettuce", "🥔 Cauliflower"));
        INGREDIENT_MAP.put("🍎 Fruits", Arrays.asList("🍏 Apple", "🍌 Banana", "🍊 Orange", "🍓 Strawberries", "🍇 Grapes", "🥭 Mango", "🍍 Pineapple", "🍋 Lemon/Lime"));
        INGREDIENT_MAP.put("🌾 Grains & Legumes", Arrays.asList("🍚 Rice", "🌾 Quinoa", "🥣 Oats", "🌰 Lentils", "🫘 Chickpeas", "🌽 Corn", "🥜 Peanuts"));
        INGREDIENT_MAP.put("🍗 Proteins", Arrays.asList("🍗 Chicken", "🥩 Beef", "🐖 Pork", "🐟 Fish", "🍳 Eggs", "🌱 Tofu", "🫘 Beans"));
        INGREDIENT_MAP.put("🧀 Dairy", Arrays.asList("🥛 Milk", "🍦 Yogurt", "🧀 Cheese", "🧈 Butter", "🥥 Coconut Milk", "🌱 Soy/Oat Milk"));
        INGREDIENT_MAP.put("🌿 Herbs & Spices", Arrays.asList("🌿 Basil", "🌿 Oregano", "🌿 Thyme", "🌿 Rosemary", "🧂 Salt", "🌶 Chili Powder", "🟠 Turmeric", "🟡 Ginger", "🟤 Cumin"));
        INGREDIENT_MAP.put("🛢️ Oils & Condiments", Arrays.asList("🫒 Olive Oil", "🥥 Coconut Oil", "🥫 Soy Sauce", "🔥 Hot Sauce", "🍯 Honey", "🥄 Mayonnaise", "🍶 Vinegar", "🧂 Salt", "🍚 Sugar"));
         INGREDIENT_MAP.put("🆕 Others", new ArrayList<>());


     }

    // Return as HashMap
    public  HashMap<String, List<String>> getIngredientMap() {
        return new HashMap<>(INGREDIENT_MAP);
    }
}
