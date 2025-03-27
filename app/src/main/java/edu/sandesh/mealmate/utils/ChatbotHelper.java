package edu.sandesh.mealmate.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ChatbotHelper {
    
    private static final Random random = new Random();
    
    // Categories of responses
    private static final Map<String, List<String>> RESPONSES = new HashMap<>();
    
    static {
        // Recipe ideas
        List<String> recipeResponses = new ArrayList<>();
        recipeResponses.add("Have you tried making a Mediterranean salad? It's healthy and delicious with tomatoes, cucumbers, olives, and feta cheese!");
        recipeResponses.add("For a quick dinner, pasta with garlic and olive oil (Aglio e Olio) is simple and tasty. Just add some red pepper flakes for heat!");
        recipeResponses.add("How about trying a vegetable stir-fry with your favorite protein? It's versatile and you can use whatever you have in your fridge.");
        recipeResponses.add("Stuffed bell peppers are a great option - fill them with rice, ground meat, and beans for a complete meal.");
        recipeResponses.add("Have you considered making homemade pizza? You can customize the toppings based on what you have available.");
        RESPONSES.put("recipe", recipeResponses);
        
        // Meal planning
        List<String> planningResponses = new ArrayList<>();
        planningResponses.add("Try planning meals around a protein source, then add vegetables and grains to create balanced meals.");
        planningResponses.add("Batch cooking on weekends can save you time during busy weekdays. Try making soups, stews, or casseroles that reheat well.");
        planningResponses.add("Consider theme nights like Meatless Monday or Taco Tuesday to add variety to your meal plan.");
        planningResponses.add("Don't forget to include breakfast and snacks in your weekly plan to avoid last-minute unhealthy choices.");
        planningResponses.add("Using seasonal ingredients can help keep your meal planning fresh and cost-effective.");
        RESPONSES.put("planning", planningResponses);
        
        // Grocery shopping
        List<String> groceryResponses = new ArrayList<>();
        groceryResponses.add("Making a categorized grocery list can help you shop more efficiently and avoid forgetting items.");
        groceryResponses.add("Try to shop the perimeter of the store first where fresh produce, meats, and dairy are usually located.");
        groceryResponses.add("Buying in bulk for non-perishable items can save money in the long run.");
        groceryResponses.add("Don't shop when hungry! You're more likely to make impulse purchases of unhealthy foods.");
        groceryResponses.add("Check your inventory before shopping to avoid buying duplicates of items you already have.");
        RESPONSES.put("grocery", groceryResponses);
        
        // Healthy eating
        List<String> healthyResponses = new ArrayList<>();
        healthyResponses.add("Including a variety of colorful vegetables in your meals ensures you get different nutrients.");
        healthyResponses.add("Whole grains like brown rice and quinoa provide more fiber and nutrients than refined grains.");
        healthyResponses.add("Healthy fats from sources like avocados, nuts, and olive oil are important for a balanced diet.");
        healthyResponses.add("Try to limit processed foods and focus on whole, unprocessed ingredients for better nutrition.");
        healthyResponses.add("Staying hydrated is important for overall health - aim for at least 8 glasses of water daily.");
        RESPONSES.put("healthy", healthyResponses);
        
        // General queries
        List<String> generalResponses = new ArrayList<>();
        generalResponses.add("I'm your MealMate assistant! I can help with recipe ideas, meal planning, and grocery shopping tips.");
        generalResponses.add("You can ask me questions about meal planning, recipes, or grocery shopping for personalized advice.");
        generalResponses.add("Check out the Weekly Plan section to organize your meals for the week.");
        generalResponses.add("Would you like suggestions for quick meals, healthy options, or recipes using specific ingredients?");
        generalResponses.add("I'm here to make your meal planning easier! What specific help do you need today?");
        RESPONSES.put("general", generalResponses);
    }
    
    // Get a response based on the user's query
    public static String getResponse(String query) {
        query = query.toLowerCase();
        
        // Determine the appropriate category based on keywords
        if (containsAny(query, "recipe", "dish", "meal", "cook", "make", "prepare", "dinner", "lunch", "breakfast")) {
            return getRandomResponse("recipe");
        } else if (containsAny(query, "plan", "schedule", "weekly", "organize", "prep")) {
            return getRandomResponse("planning");
        } else if (containsAny(query, "grocery", "shop", "store", "buy", "purchase", "list")) {
            return getRandomResponse("grocery");
        } else if (containsAny(query, "healthy", "nutrition", "diet", "calorie", "vegetable", "fruit", "protein")) {
            return getRandomResponse("healthy");
        } else {
            return getRandomResponse("general");
        }
    }
    
    // Get a welcome message
    public static String getWelcomeMessage() {
        return "Hello! I'm your MealMate assistant. I can help with recipe ideas, meal planning tips, and grocery shopping advice. How can I assist you today?";
    }
    
    // Check if the query contains any of the keywords
    private static boolean containsAny(String query, String... keywords) {
        for (String keyword : keywords) {
            if (query.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
    
    // Get a random response from the specified category
    private static String getRandomResponse(String category) {
        List<String> responses = RESPONSES.get(category);
        if (responses != null && !responses.isEmpty()) {
            int index = random.nextInt(responses.size());
            return responses.get(index);
        } else {
            return RESPONSES.get("general").get(0);
        }
    }
} 