package com.example.smartpantry;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartpantry.database.DatabaseHelper;
import com.example.smartpantry.model.Recipe;

public class RecipeDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        long recipeId = getIntent().getLongExtra("RECIPE_ID", -1);
        if (recipeId == -1) {
            finish();
            return;
        }

        DatabaseHelper db = new DatabaseHelper(this);
        Recipe recipe = db.getRecipeById(recipeId);
        if (recipe == null) {
            finish();
            return;
        }

        TextView tvName = findViewById(R.id.tv_detail_name);
        TextView tvMethod = findViewById(R.id.tv_method);
        LinearLayout layoutIngredients = findViewById(R.id.layout_ingredients);

        tvName.setText(recipe.getName());
        tvMethod.setText(recipe.getMethod());

        layoutIngredients.removeAllViews();
        if (recipe.getIngredients() != null) {
            for (Recipe.RecipeIngredient ing : recipe.getIngredients()) {
                TextView tv = new TextView(this);
                tv.setText("• " + ing.toString());
                tv.setTextSize(15);
                tv.setPadding(0, 6, 0, 6);
                layoutIngredients.addView(tv);
            }
        }
    }
}
