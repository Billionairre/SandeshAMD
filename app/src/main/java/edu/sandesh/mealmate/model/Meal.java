package edu.sandesh.mealmate.model;


public class Meal {

    private String mealType;

    private Recipe recipe;



    public Meal(Recipe recipe, String mealType) {
        this.recipe =   recipe;
        this.mealType = mealType;
    }

    public Recipe getRecipe(){
        return recipe;
    }

    public String getMealName() {
        return recipe.getRecipeName();
    }

    public String getMealType() {
        return mealType;
    }

    public String getMealImage() {
        return recipe.getPhotoUrl();
    }
}
