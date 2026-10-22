package com.example.smartpantry.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.smartpantry.model.PantryItem;
import com.example.smartpantry.model.Recipe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "smart_pantry.db";
    private static final int DATABASE_VERSION = 1;

    // Pantry table
    private static final String TABLE_PANTRY = "pantry";
    private static final String COL_P_ID = "id";
    private static final String COL_P_NAME = "name";
    private static final String COL_P_QTY = "quantity";
    private static final String COL_P_UNIT = "unit";
    private static final String COL_P_EXPIRY = "expiry_date";

    // Recipes table
    private static final String TABLE_RECIPES = "recipes";
    private static final String COL_R_ID = "id";
    private static final String COL_R_NAME = "name";
    private static final String COL_R_METHOD = "method";

    // Recipe ingredients table
    private static final String TABLE_RECIPE_ING = "recipe_ingredients";
    private static final String COL_RI_ID = "id";
    private static final String COL_RI_RECIPE_ID = "recipe_id";
    private static final String COL_RI_NAME = "name";
    private static final String COL_RI_QTY = "quantity";
    private static final String COL_RI_UNIT = "unit";

    // Simple synonym map for robust matching (SA context)
    private static final Map<String, String> SYNONYMS = new HashMap<>();
    static {
        SYNONYMS.put("tomatoes", "tomato");
        SYNONYMS.put("onions", "onion");
        SYNONYMS.put("potatoes", "potato");
        SYNONYMS.put("carrots", "carrot");
        SYNONYMS.put("eggs", "egg");
        SYNONYMS.put("chillies", "chilli");
        SYNONYMS.put("chilies", "chilli");
        SYNONYMS.put("wors", "boerewors");
        SYNONYMS.put("boerie", "boerewors");
        SYNONYMS.put("mieliemeal", "mielie meal");
        SYNONYMS.put("maize meal", "mielie meal");
        SYNONYMS.put("pap", "mielie meal");
        SYNONYMS.put("spinach", "spinach");
        SYNONYMS.put("morogo", "spinach");
    }

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_PANTRY + " (" +
                COL_P_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_P_NAME + " TEXT NOT NULL, " +
                COL_P_QTY + " REAL NOT NULL, " +
                COL_P_UNIT + " TEXT NOT NULL, " +
                COL_P_EXPIRY + " TEXT)");

        db.execSQL("CREATE TABLE " + TABLE_RECIPES + " (" +
                COL_R_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_R_NAME + " TEXT NOT NULL, " +
                COL_R_METHOD + " TEXT NOT NULL)");

        db.execSQL("CREATE TABLE " + TABLE_RECIPE_ING + " (" +
                COL_RI_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_RI_RECIPE_ID + " INTEGER NOT NULL, " +
                COL_RI_NAME + " TEXT NOT NULL, " +
                COL_RI_QTY + " REAL NOT NULL, " +
                COL_RI_UNIT + " TEXT NOT NULL, " +
                "FOREIGN KEY(" + COL_RI_RECIPE_ID + ") REFERENCES " + TABLE_RECIPES + "(" + COL_R_ID + "))");

        seedRecipes(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPE_ING);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PANTRY);
        onCreate(db);
    }

    // ==================== PANTRY CRUD ====================

    public long insertPantryItem(String name, double quantity, String unit, String expiry) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_P_NAME, normalise(name));
        cv.put(COL_P_QTY, quantity);
        cv.put(COL_P_UNIT, unit);
        cv.put(COL_P_EXPIRY, expiry);
        long id = db.insert(TABLE_PANTRY, null, cv);
        db.close();
        return id;
    }

    public int updatePantryItem(long id, String name, double quantity, String unit, String expiry) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_P_NAME, normalise(name));
        cv.put(COL_P_QTY, quantity);
        cv.put(COL_P_UNIT, unit);
        cv.put(COL_P_EXPIRY, expiry);
        int rows = db.update(TABLE_PANTRY, cv, COL_P_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
        return rows;
    }

    public int deletePantryItem(long id) {
        SQLiteDatabase db = getWritableDatabase();
        int rows = db.delete(TABLE_PANTRY, COL_P_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
        return rows;
    }

    public void clearAllPantry() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_PANTRY, null, null);
        db.close();
    }

    public List<PantryItem> getAllPantryItems() {
        List<PantryItem> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_PANTRY, null, null, null, null, null, COL_P_NAME + " ASC");
        if (c.moveToFirst()) {
            do {
                PantryItem item = new PantryItem(
                        c.getLong(c.getColumnIndexOrThrow(COL_P_ID)),
                        c.getString(c.getColumnIndexOrThrow(COL_P_NAME)),
                        c.getDouble(c.getColumnIndexOrThrow(COL_P_QTY)),
                        c.getString(c.getColumnIndexOrThrow(COL_P_UNIT)),
                        c.getString(c.getColumnIndexOrThrow(COL_P_EXPIRY))
                );
                list.add(item);
            } while (c.moveToNext());
        }
        c.close();
        db.close();
        return list;
    }

    // ==================== RECIPES ====================

    public List<Recipe> getAllRecipes() {
        List<Recipe> recipes = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_RECIPES, null, null, null, null, null, COL_R_NAME + " ASC");
        if (c.moveToFirst()) {
            do {
                long id = c.getLong(c.getColumnIndexOrThrow(COL_R_ID));
                Recipe r = new Recipe(id,
                        c.getString(c.getColumnIndexOrThrow(COL_R_NAME)),
                        c.getString(c.getColumnIndexOrThrow(COL_R_METHOD)));
                r.setIngredients(getIngredientsForRecipe(db, id));
                recipes.add(r);
            } while (c.moveToNext());
        }
        c.close();
        db.close();
        return recipes;
    }

    public Recipe getRecipeById(long id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_RECIPES, null, COL_R_ID + "=?", new String[]{String.valueOf(id)}, null, null, null);
        Recipe recipe = null;
        if (c.moveToFirst()) {
            recipe = new Recipe(id,
                    c.getString(c.getColumnIndexOrThrow(COL_R_NAME)),
                    c.getString(c.getColumnIndexOrThrow(COL_R_METHOD)));
            recipe.setIngredients(getIngredientsForRecipe(db, id));
        }
        c.close();
        db.close();
        return recipe;
    }

    private List<Recipe.RecipeIngredient> getIngredientsForRecipe(SQLiteDatabase db, long recipeId) {
        List<Recipe.RecipeIngredient> list = new ArrayList<>();
        Cursor c = db.query(TABLE_RECIPE_ING, null, COL_RI_RECIPE_ID + "=?",
                new String[]{String.valueOf(recipeId)}, null, null, null);
        if (c.moveToFirst()) {
            do {
                list.add(new Recipe.RecipeIngredient(
                        c.getString(c.getColumnIndexOrThrow(COL_RI_NAME)),
                        c.getDouble(c.getColumnIndexOrThrow(COL_RI_QTY)),
                        c.getString(c.getColumnIndexOrThrow(COL_RI_UNIT))
                ));
            } while (c.moveToNext());
        }
        c.close();
        return list;
    }

    // ==================== STRICT MATCHING (CORE LOGIC) ====================

    public List<Recipe> getStrictMatchingRecipes() {
        List<PantryItem> pantry = getAllPantryItems();
        Map<String, PantryItem> pantryMap = new HashMap<>();
        for (PantryItem item : pantry) {
            String key = normalise(item.getName());
            pantryMap.put(key, item);
        }

        List<Recipe> all = getAllRecipes();
        List<Recipe> matches = new ArrayList<>();

        for (Recipe recipe : all) {
            if (canMakeStrictly(recipe, pantryMap)) {
                matches.add(recipe);
            }
        }
        return matches;
    }

    private boolean canMakeStrictly(Recipe recipe, Map<String, PantryItem> pantryMap) {
        for (Recipe.RecipeIngredient needed : recipe.getIngredients()) {
            String key = normalise(needed.getName());
            PantryItem have = pantryMap.get(key);
            if (have == null) {
                return false; // missing ingredient
            }
            // Quantity check – same unit required for simplicity (robust enough for assignment)
            if (!have.getUnit().equalsIgnoreCase(needed.getUnit())) {
                // Allow simple equivalent units
                if (!unitsCompatible(have.getUnit(), needed.getUnit())) {
                    return false;
                }
            }
            if (have.getQuantity() < needed.getQuantity()) {
                return false; // not enough quantity
            }
        }
        return true;
    }

    private boolean unitsCompatible(String a, String b) {
        a = a.toLowerCase(Locale.ROOT);
        b = b.toLowerCase(Locale.ROOT);
        if (a.equals(b)) return true;
        // Very basic equivalences
        if ((a.equals("g") || a.equals("gram") || a.equals("grams")) &&
                (b.equals("g") || b.equals("gram") || b.equals("grams"))) return true;
        if ((a.equals("ml") || a.equals("millilitre")) &&
                (b.equals("ml") || b.equals("millilitre"))) return true;
        return false;
    }

    private String normalise(String name) {
        if (name == null) return "";
        String n = name.trim().toLowerCase(Locale.ROOT);
        // Remove plural 's' for simple cases
        if (n.endsWith("oes")) {
            n = n.substring(0, n.length() - 2); // tomatoes -> tomato
        } else if (n.endsWith("s") && n.length() > 3) {
            n = n.substring(0, n.length() - 1);
        }
        if (SYNONYMS.containsKey(n)) {
            return SYNONYMS.get(n);
        }
        return n;
    }

    // ==================== SEED DATA – 18 SOUTH AFRICAN RECIPES ====================

    private void seedRecipes(SQLiteDatabase db) {
        // 1. Pap & Wors
        long id = insertRecipe(db, "Pap & Wors",
                "1. Boil water with a pinch of salt.\n2. Slowly stir in mielie meal to make smooth pap.\n3. Fry or braai the boerewors until cooked.\n4. Slice onion and fry until soft.\n5. Serve pap with wors and fried onion.");
        addIng(db, id, "mielie meal", 250, "g");
        addIng(db, id, "boerewors", 400, "g");
        addIng(db, id, "onion", 1, "piece");
        addIng(db, id, "oil", 2, "tbsp");
        addIng(db, id, "salt", 1, "tsp");

        // 2. Classic Bobotie
        id = insertRecipe(db, "Classic Bobotie",
                "1. Fry onion until soft. Add beef mince and brown.\n2. Stir in curry powder, turmeric, vinegar and sugar.\n3. Soak bread in milk, squeeze and mix into mince.\n4. Place mixture in oven dish, top with bay leaves.\n5. Beat egg with remaining milk and pour over.\n6. Bake at 180°C for 30–40 minutes until golden.");
        addIng(db, id, "beef mince", 500, "g");
        addIng(db, id, "onion", 1, "piece");
        addIng(db, id, "curry powder", 2, "tbsp");
        addIng(db, id, "turmeric", 1, "tsp");
        addIng(db, id, "bread", 2, "piece");
        addIng(db, id, "milk", 250, "ml");
        addIng(db, id, "egg", 2, "piece");
        addIng(db, id, "bay leaves", 2, "piece");
        addIng(db, id, "vinegar", 2, "tbsp");
        addIng(db, id, "sugar", 1, "tbsp");

        // 3. Chicken Curry
        id = insertRecipe(db, "Chicken Curry",
                "1. Brown chicken pieces in oil.\n2. Add onion and garlic, fry until soft.\n3. Stir in curry powder and cook 1 minute.\n4. Add tomato and potato, cover with water.\n5. Simmer 30–40 minutes until chicken is tender.\n6. Season with salt and serve with rice or pap.");
        addIng(db, id, "chicken", 600, "g");
        addIng(db, id, "onion", 1, "piece");
        addIng(db, id, "garlic", 2, "piece");
        addIng(db, id, "curry powder", 2, "tbsp");
        addIng(db, id, "tomato", 2, "piece");
        addIng(db, id, "potato", 2, "piece");
        addIng(db, id, "oil", 2, "tbsp");

        // 4. Chakalaka
        id = insertRecipe(db, "Chakalaka",
                "1. Fry onion in oil until soft.\n2. Add grated carrot, chopped green beans and chilli.\n3. Stir in curry powder and chopped tomato.\n4. Cook 10–15 minutes until vegetables are soft but still have bite.\n5. Season and serve hot or cold with pap or bread.");
        addIng(db, id, "onion", 1, "piece");
        addIng(db, id, "carrot", 2, "piece");
        addIng(db, id, "green beans", 150, "g");
        addIng(db, id, "chilli", 1, "piece");
        addIng(db, id, "tomato", 2, "piece");
        addIng(db, id, "oil", 2, "tbsp");
        addIng(db, id, "curry powder", 1, "tbsp");

        // 5. Butternut Soup
        id = insertRecipe(db, "Butternut Soup",
                "1. Peel and cube butternut and potato.\n2. Fry onion and garlic until soft.\n3. Add butternut, potato and cover with water.\n4. Simmer until vegetables are soft.\n5. Blend until smooth, stir in milk, season and serve.");
        addIng(db, id, "butternut", 500, "g");
        addIng(db, id, "onion", 1, "piece");
        addIng(db, id, "garlic", 2, "piece");
        addIng(db, id, "potato", 1, "piece");
        addIng(db, id, "milk", 200, "ml");
        addIng(db, id, "oil", 1, "tbsp");
        addIng(db, id, "salt", 1, "tsp");

        // 6. Samp & Beans
        id = insertRecipe(db, "Samp & Beans",
                "1. Soak samp and beans overnight.\n2. Drain and cover with fresh water.\n3. Boil until soft (1–2 hours), topping up water as needed.\n4. Fry onion in oil and stir into the samp and beans.\n5. Season with salt and serve.");
        addIng(db, id, "samp", 250, "g");
        addIng(db, id, "beans", 200, "g");
        addIng(db, id, "onion", 1, "piece");
        addIng(db, id, "oil", 2, "tbsp");
        addIng(db, id, "salt", 1, "tsp");

        // 7. Tomato Bredie
        id = insertRecipe(db, "Tomato Bredie",
                "1. Brown beef mince or stewing beef in oil.\n2. Add onion and cook until soft.\n3. Stir in chopped tomato, potato and carrot.\n4. Add a little water, cover and simmer 45–60 minutes.\n5. Season well and serve with rice or pap.");
        addIng(db, id, "beef mince", 500, "g");
        addIng(db, id, "tomato", 4, "piece");
        addIng(db, id, "onion", 1, "piece");
        addIng(db, id, "potato", 2, "piece");
        addIng(db, id, "carrot", 1, "piece");
        addIng(db, id, "oil", 2, "tbsp");

        // 8. Spinach & Potato (Morogo style)
        id = insertRecipe(db, "Spinach & Potato",
                "1. Boil potato until almost soft.\n2. Fry onion in oil.\n3. Add washed spinach and cook until wilted.\n4. Add potato, season with salt and mix gently.\n5. Serve as a side or with pap.");
        addIng(db, id, "spinach", 300, "g");
        addIng(db, id, "potato", 2, "piece");
        addIng(db, id, "onion", 1, "piece");
        addIng(db, id, "oil", 2, "tbsp");
        addIng(db, id, "salt", 1, "tsp");

        // 9. Bunny Chow (Chicken)
        id = insertRecipe(db, "Bunny Chow (Chicken)",
                "1. Brown chicken in oil with onion.\n2. Add curry powder and tomato, cook 2 minutes.\n3. Add a little water and simmer until chicken is cooked.\n4. Hollow out a half loaf of bread.\n5. Spoon the curry into the bread and serve.");
        addIng(db, id, "chicken", 400, "g");
        addIng(db, id, "onion", 1, "piece");
        addIng(db, id, "curry powder", 2, "tbsp");
        addIng(db, id, "tomato", 2, "piece");
        addIng(db, id, "bread", 1, "piece");
        addIng(db, id, "oil", 2, "tbsp");

        // 10. Boerewors Roll
        id = insertRecipe(db, "Boerewors Roll",
                "1. Braai or fry boerewors until cooked.\n2. Slice onion and tomato.\n3. Lightly toast or warm the bread roll.\n4. Place wors in the roll, top with onion and tomato.\n5. Add sauce if desired and serve.");
        addIng(db, id, "boerewors", 300, "g");
        addIng(db, id, "onion", 1, "piece");
        addIng(db, id, "tomato", 1, "piece");
        addIng(db, id, "bread", 2, "piece");
        addIng(db, id, "oil", 1, "tbsp");

        // 11. Gem Squash with Butter
        id = insertRecipe(db, "Gem Squash with Butter",
                "1. Halve gem squash and remove seeds.\n2. Boil or steam until soft (15–20 min).\n3. Drain well.\n4. Add a knob of butter and a pinch of salt.\n5. Serve as a side dish.");
        addIng(db, id, "gem squash", 2, "piece");
        addIng(db, id, "butter", 30, "g");
        addIng(db, id, "salt", 0.5, "tsp");

        // 12. Sweet Potato Bake
        id = insertRecipe(db, "Sweet Potato Bake",
                "1. Peel and slice sweet potato.\n2. Layer in a baking dish with dots of butter.\n3. Sprinkle with a little sugar.\n4. Cover and bake at 180°C for 30–40 minutes until soft.\n5. Optional: finish uncovered for a golden top.");
        addIng(db, id, "sweet potato", 500, "g");
        addIng(db, id, "butter", 40, "g");
        addIng(db, id, "sugar", 2, "tbsp");

        // 13. Cabbage & Potato Stew
        id = insertRecipe(db, "Cabbage & Potato Stew",
                "1. Fry onion in oil until soft.\n2. Add cubed potato and a little water.\n3. Add shredded cabbage, cover and simmer until soft.\n4. Season with salt.\n5. Serve as a hearty side or light meal.");
        addIng(db, id, "cabbage", 400, "g");
        addIng(db, id, "potato", 3, "piece");
        addIng(db, id, "onion", 1, "piece");
        addIng(db, id, "oil", 2, "tbsp");
        addIng(db, id, "salt", 1, "tsp");

        // 14. Simple Fish & Potato
        id = insertRecipe(db, "Simple Fish & Potato",
                "1. Coat fish lightly in flour seasoned with salt.\n2. Fry in oil until golden and cooked through.\n3. Boil or fry potato until done.\n4. Serve fish with potato and a squeeze of lemon if available.");
        addIng(db, id, "fish", 400, "g");
        addIng(db, id, "potato", 3, "piece");
        addIng(db, id, "flour", 50, "g");
        addIng(db, id, "oil", 3, "tbsp");
        addIng(db, id, "salt", 1, "tsp");

        // 15. Egg & Tomato Breakfast
        id = insertRecipe(db, "Egg & Tomato Breakfast",
                "1. Fry onion and chopped tomato in oil until soft.\n2. Make wells and crack in the eggs.\n3. Cover and cook until eggs are done to your liking.\n4. Season with salt and serve with bread.");
        addIng(db, id, "egg", 3, "piece");
        addIng(db, id, "tomato", 2, "piece");
        addIng(db, id, "onion", 1, "piece");
        addIng(db, id, "oil", 1, "tbsp");
        addIng(db, id, "salt", 0.5, "tsp");

        // 16. Mielie Meal Porridge (Pap)
        id = insertRecipe(db, "Mielie Meal Porridge",
                "1. Bring water or milk to the boil with a pinch of salt.\n2. Slowly whisk in mielie meal to avoid lumps.\n3. Reduce heat and cook, stirring, until thick.\n4. Sweeten with sugar if desired.\n5. Serve hot for breakfast.");
        addIng(db, id, "mielie meal", 100, "g");
        addIng(db, id, "milk", 300, "ml");
        addIng(db, id, "salt", 0.5, "tsp");
        addIng(db, id, "sugar", 1, "tbsp");

        // 17. Carrot Salad
        id = insertRecipe(db, "Carrot Salad",
                "1. Grate the carrots.\n2. Finely slice the onion.\n3. Mix vinegar, sugar and oil for the dressing.\n4. Toss everything together.\n5. Chill before serving.");
        addIng(db, id, "carrot", 3, "piece");
        addIng(db, id, "onion", 0.5, "piece");
        addIng(db, id, "vinegar", 2, "tbsp");
        addIng(db, id, "sugar", 1, "tbsp");
        addIng(db, id, "oil", 2, "tbsp");

        // 18. Malva Pudding (basic)
        id = insertRecipe(db, "Malva Pudding",
                "1. Cream butter and sugar, beat in egg.\n2. Add flour, a pinch of salt, and milk alternately.\n3. Stir in vinegar.\n4. Pour into a greased dish and bake at 180°C for 30–35 min.\n5. Pour a simple cream sauce over while hot and serve.");
        addIng(db, id, "flour", 150, "g");
        addIng(db, id, "sugar", 150, "g");
        addIng(db, id, "egg", 1, "piece");
        addIng(db, id, "milk", 100, "ml");
        addIng(db, id, "butter", 50, "g");
        addIng(db, id, "vinegar", 1, "tbsp");
    }

    private long insertRecipe(SQLiteDatabase db, String name, String method) {
        ContentValues cv = new ContentValues();
        cv.put(COL_R_NAME, name);
        cv.put(COL_R_METHOD, method);
        return db.insert(TABLE_RECIPES, null, cv);
    }

    private void addIng(SQLiteDatabase db, long recipeId, String name, double qty, String unit) {
        ContentValues cv = new ContentValues();
        cv.put(COL_RI_RECIPE_ID, recipeId);
        cv.put(COL_RI_NAME, normalise(name));
        cv.put(COL_RI_QTY, qty);
        cv.put(COL_RI_UNIT, unit);
        db.insert(TABLE_RECIPE_ING, null, cv);
    }
}
