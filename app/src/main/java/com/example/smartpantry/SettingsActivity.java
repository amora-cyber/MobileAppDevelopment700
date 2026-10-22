package com.example.smartpantry;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.smartpantry.database.DatabaseHelper;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class SettingsActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "SmartPantryPrefs";
    private static final String KEY_EXPIRY_ALERTS = "expiry_alerts";
    private static final String KEY_UNITS = "preferred_units";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.settings);
        }

        SwitchMaterial switchAlerts = findViewById(R.id.switch_expiry_alerts);
        Spinner spinnerUnits = findViewById(R.id.spinner_units);
        Button btnClear = findViewById(R.id.btn_clear_pantry);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Expiry alerts toggle
        switchAlerts.setChecked(prefs.getBoolean(KEY_EXPIRY_ALERTS, true));
        switchAlerts.setOnCheckedChangeListener((buttonView, isChecked) ->
                prefs.edit().putBoolean(KEY_EXPIRY_ALERTS, isChecked).apply());

        // Units spinner
        String[] unitOptions = {"Metric (g, ml)", "Simple (piece, cup, tsp)"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, unitOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUnits.setAdapter(adapter);

        int savedUnit = prefs.getInt(KEY_UNITS, 0);
        spinnerUnits.setSelection(savedUnit);
        spinnerUnits.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                prefs.edit().putInt(KEY_UNITS, position).apply();
            }
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        // Clear pantry
        btnClear.setOnClickListener(v -> new AlertDialog.Builder(this)
                .setTitle("Clear pantry")
                .setMessage("This will delete ALL ingredients from your pantry. Continue?")
                .setPositiveButton("Clear", (d, w) -> {
                    new DatabaseHelper(this).clearAllPantry();
                    Toast.makeText(this, "Pantry cleared", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show());
    }
}
