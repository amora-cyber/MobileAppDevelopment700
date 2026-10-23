# Smart Pantry Manager – South African Edition

**Mobile App Development 700 – Practical Assignment**

A fully functional Android application written in **Java** that helps users reduce food waste by tracking pantry ingredients and suggesting recipes that can be made **strictly** from what is already available.

## Core Feature – Strict Matching

A recipe is only shown in “Suggested Recipes” when **every** required ingredient is present in the user’s pantry in at least the required quantity. Partial matches are deliberately excluded.

## Features

- Full pantry CRUD (Create, Read, Update, Delete)
- RecyclerView with custom adapters
- 18 pre-seeded authentic South African recipes
- Robust ingredient name normalisation (singular/plural + common SA synonyms)
- Recipe detail screen with ingredients + method
- Settings screen (expiry alerts toggle, unit preference, clear pantry)
- Bottom navigation
- Input validation
- Data persists after app restart (SQLite)

## Database Choice

**SQLite** via `SQLiteOpenHelper`  
Chosen because it is fully offline, covered in the module’s persistent data chapter, requires no external services, and guarantees the data survives app restarts.

## Screens

1. **Pantry List** – view / edit / delete ingredients + FAB to add
2. **Add / Edit Ingredient** – form with validation + optional expiry date
3. **Suggested Recipes** – only strict matches (empty-state message included)
4. **Recipe Detail** – full ingredients + preparation steps
5. **Settings** – preferences and clear-pantry option

## How to Run

1. Open the project in **Android Studio** (Hedgehog or newer recommended).
2. Let Gradle sync.
3. Connect a device or start an emulator (API 24+).
4. Click **Run**.

On first launch the 18 South African recipes are automatically seeded into the database.

## South African Recipes Included

1. Pap & Wors  
2. Classic Bobotie  
3. Chicken Curry  
4. Chakalaka  
5. Butternut Soup  
6. Samp & Beans  
7. Tomato Bredie  
8. Spinach & Potato (Morogo style)  
9. Bunny Chow (Chicken)  
10. Boerewors Roll  
11. Gem Squash with Butter  
12. Sweet Potato Bake  
13. Cabbage & Potato Stew  
14. Simple Fish & Potato  
15. Egg & Tomato Breakfast  
16. Mielie Meal Porridge  
17. Carrot Salad  
18. Malva Pudding  

## Project Structure

```
app/src/main/java/com/example/smartpantry/
├── MainActivity.java
├── PantryListActivity.java
├── AddEditIngredientActivity.java
├── SuggestedRecipesActivity.java
├── RecipeDetailActivity.java
├── SettingsActivity.java
├── adapter/
│   ├── PantryAdapter.java
│   └── RecipeAdapter.java
├── model/
│   ├── PantryItem.java
│   └── Recipe.java
└── database/
    └── DatabaseHelper.java   ← SQLite + seed data + strict matching logic
```

## Git History

Develop the project with meaningful commits from the start (minimum 10 recommended).  
Example commit messages:

- “Create project and basic package structure”
- “Add PantryItem and Recipe model classes”
- “Implement DatabaseHelper with SQLite tables”
- “Seed 18 South African recipes”
- “Build Pantry List screen with RecyclerView”
- “Add Add/Edit Ingredient form with validation”
- “Implement strict-matching logic”
- “Create Suggested Recipes and Recipe Detail screens”
- “Add Settings screen and bottom navigation”
- “Polish UI and empty states
