package com.example.smartpantry;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartpantry.database.DatabaseHelper;

import java.util.Calendar;
import java.util.Locale;

public class AddEditIngredientActivity extends AppCompatActivity {

    private EditText etName, etQuantity;
    private Spinner spinnerUnit;
    private TextView tvSelectedDate;
    private long itemId = -1;
    private String selectedDate = null;

    private final String[] UNITS = {"g", "ml", "tsp", "tbsp", "cup", "piece", "whole"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_ingredient);

        etName = findViewById(R.id.et_name);
        etQuantity = findViewById(R.id.et_quantity);
        spinnerUnit = findViewById(R.id.spinner_unit);
        tvSelectedDate = findViewById(R.id.tv_selected_date);
        Button btnSave = findViewById(R.id.btn_save);
        Button btnPickDate = findViewById(R.id.btn_pick_date);

        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, UNITS);
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUnit.setAdapter(unitAdapter);

        Intent intent = getIntent();
        if (intent.hasExtra("ITEM_ID")) {
            itemId = intent.getLongExtra("ITEM_ID", -1);
            etName.setText(intent.getStringExtra("NAME"));
            etQuantity.setText(String.valueOf(intent.getDoubleExtra("QUANTITY", 0)));
            String unit = intent.getStringExtra("UNIT");
            if (unit != null) {
                for (int i = 0; i < UNITS.length; i++) {
                    if (UNITS[i].equalsIgnoreCase(unit)) {
                        spinnerUnit.setSelection(i);
                        break;
                    }
                }
            }
            selectedDate = intent.getStringExtra("EXPIRY");
            if (selectedDate != null && !selectedDate.isEmpty()) {
                tvSelectedDate.setText(selectedDate);
            }
            setTitle(R.string.edit_ingredient);
        } else {
            setTitle(R.string.add_ingredient);
        }

        btnPickDate.setOnClickListener(v -> showDatePicker());
        btnSave.setOnClickListener(v -> saveItem());
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
                    tvSelectedDate.setText(selectedDate);
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void saveItem() {
        String name = etName.getText().toString().trim();
        String qtyStr = etQuantity.getText().toString().trim();

        if (name.isEmpty()) {
            etName.setError("Name is required");
            etName.requestFocus();
            return;
        }
        if (qtyStr.isEmpty()) {
            etQuantity.setError("Quantity is required");
            etQuantity.requestFocus();
            return;
        }

        double quantity;
        try {
            quantity = Double.parseDouble(qtyStr);
            if (quantity <= 0) {
                etQuantity.setError("Quantity must be greater than 0");
                return;
            }
        } catch (NumberFormatException e) {
            etQuantity.setError("Enter a valid number");
            return;
        }

        String unit = spinnerUnit.getSelectedItem().toString();
        DatabaseHelper db = new DatabaseHelper(this);

        if (itemId == -1) {
            db.insertPantryItem(name, quantity, unit, selectedDate);
            Toast.makeText(this, "Ingredient added", Toast.LENGTH_SHORT).show();
        } else {
            db.updatePantryItem(itemId, name, quantity, unit, selectedDate);
            Toast.makeText(this, "Ingredient updated", Toast.LENGTH_SHORT).show();
        }
        finish();
    }
}
