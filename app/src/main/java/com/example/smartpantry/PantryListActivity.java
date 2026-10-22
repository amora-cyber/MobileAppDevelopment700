package com.example.smartpantry;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantry.adapter.PantryAdapter;
import com.example.smartpantry.database.DatabaseHelper;
import com.example.smartpantry.model.PantryItem;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class PantryListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView tvEmpty;
    private DatabaseHelper db;
    private PantryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantry_list);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        recyclerView = findViewById(R.id.recycler_pantry);
        tvEmpty = findViewById(R.id.tv_empty_pantry);
        FloatingActionButton fab = findViewById(R.id.fab_add);

        db = new DatabaseHelper(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        fab.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddEditIngredientActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPantry();
    }

    private void loadPantry() {
        List<PantryItem> items = db.getAllPantryItems();
        adapter = new PantryAdapter(items, new PantryAdapter.OnItemClickListener() {
            @Override
            public void onEdit(PantryItem item) {
                Intent intent = new Intent(PantryListActivity.this, AddEditIngredientActivity.class);
                intent.putExtra("ITEM_ID", item.getId());
                intent.putExtra("NAME", item.getName());
                intent.putExtra("QUANTITY", item.getQuantity());
                intent.putExtra("UNIT", item.getUnit());
                intent.putExtra("EXPIRY", item.getExpiryDate());
                startActivity(intent);
            }

            @Override
            public void onDelete(PantryItem item) {
                new AlertDialog.Builder(PantryListActivity.this)
                        .setTitle("Delete ingredient")
                        .setMessage("Remove \"" + item.getName() + "\" from your pantry?")
                        .setPositiveButton("Delete", (d, w) -> {
                            db.deletePantryItem(item.getId());
                            Toast.makeText(PantryListActivity.this, "Deleted", Toast.LENGTH_SHORT).show();
                            loadPantry();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });
        recyclerView.setAdapter(adapter);
        tvEmpty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
