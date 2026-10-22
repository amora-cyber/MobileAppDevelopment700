package com.example.smartpantry.model;

import java.util.ArrayList;
import java.util.List;

public class Recipe {
    private long id;
    private String name;
    private String method;
    private List<RecipeIngredient> ingredients;

    public Recipe() {
        ingredients = new ArrayList<>();
    }

    public Recipe(long id, String name, String method) {
        this.id = id;
        this.name = name;
        this.method = method;
        this.ingredients = new ArrayList<>();
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public List<RecipeIngredient> getIngredients() { return ingredients; }
    public void setIngredients(List<RecipeIngredient> ingredients) { this.ingredients = ingredients; }

    public void addIngredient(String name, double quantity, String unit) {
        ingredients.add(new RecipeIngredient(name, quantity, unit));
    }

    public static class RecipeIngredient {
        private String name;
        private double quantity;
        private String unit;

        public RecipeIngredient(String name, double quantity, String unit) {
            this.name = name;
            this.quantity = quantity;
            this.unit = unit;
        }

        public String getName() { return name; }
        public double getQuantity() { return quantity; }
        public String getUnit() { return unit; }

        @Override
        public String toString() {
            if (quantity > 0) {
                return quantity + " " + unit + " " + name;
            }
            return name;
        }
    }
}
