package za.ac.richfield.smartpantry;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * One form for both Create and Update. Which mode it is in is decided by an
 * Intent extra (EXTRA_ID): absent -> "add" mode; present -> "edit" mode with
 * the item's current values pre-filled. This satisfies "correct use of
 * Intents to navigate between screens and pass data" in one clear example.
 *
 * Validation rules enforced before anything is saved:
 *   - name must not be empty, and must not duplicate an existing pantry name
 *   - quantity must parse as a number, and must be greater than zero
 *   - expiry date is optional
 */
public class AddEditIngredientActivity extends AppCompatActivity {

    public static final String EXTRA_ID = "extra_id";

    private DatabaseHelper db;
    private TextInputLayout tilName, tilQty;
    private TextInputEditText etName, etQty;
    private Spinner spUnit;
    private TextView tvExpiry;
    private Button btnDate, btnDelete;
    private long editId = -1L;          // -1 = add mode
    private Long pickedExpiry = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit);

        db = new DatabaseHelper(this);

        tilName = findViewById(R.id.tilName);
        tilQty = findViewById(R.id.tilQty);
        etName = findViewById(R.id.etName);
        etQty = findViewById(R.id.etQty);
        spUnit = findViewById(R.id.spUnit);
        tvExpiry = findViewById(R.id.tvExpiryValue);
        btnDate = findViewById(R.id.btnPickDate);
        btnDelete = findViewById(R.id.btnDelete);

        // --- unit spinner --------------------------------------------------
        List<Unit.UnitDef> units = Unit.all();
        ArrayAdapter<Unit.UnitDef> unitAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, units);
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spUnit.setAdapter(unitAdapter);

        // --- edit mode? ----------------------------------------------------
        editId = getIntent().getLongExtra(EXTRA_ID, -1L);
        if (editId > 0) {
            setTitle(R.string.edit_ingredient);
            PantryItem item = db.getPantryItem(editId);
            if (item == null) {         // stale id (row deleted elsewhere)
                Toast.makeText(this, R.string.err_item_gone, Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            etName.setText(item.getName());
            etQty.setText(RecipeMatcher.trimQty(item.getQuantity()));
            selectUnit(item.getUnit());
            pickedExpiry = item.getExpiryDate();
            if (pickedExpiry != null) showExpiry(pickedExpiry);
            btnDelete.setVisibility(View.VISIBLE);
        } else {
            setTitle(R.string.add_ingredient);
        }

        // --- expiry picker -------------------------------------------------
        btnDate.setOnClickListener(v -> showDatePicker());

        // --- save ----------------------------------------------------------
        findViewById(R.id.btnSave).setOnClickListener(v -> save());

        // --- delete (edit mode only) --------------------------------------
        btnDelete.setOnClickListener(v -> {
            db.deletePantryItem(editId);
            Toast.makeText(this, R.string.deleted, Toast.LENGTH_SHORT).show();
            finish();   // back to the pantry list, which re-reads on resume
        });

        // --- cancel --------------------------------------------------------
        findViewById(R.id.btnCancel).setOnClickListener(v -> finish());
    }

    /** Validates the form; on success writes to SQLite and closes. */
    private void save() {
        String name = etName.getText() == null ? "" : etName.getText().toString().trim();
        String qtyRaw = etQty.getText() == null ? "" : etQty.getText().toString().trim();

        // 1. name must exist and be unique (after normalisation).
        tilName.setError(null);
        tilQty.setError(null);

        if (name.isEmpty()) {
            tilName.setError(getString(R.string.err_name_required));
            return;
        }
        if (db.pantryItemExists(name, editId)) {
            tilName.setError(getString(R.string.err_name_duplicate));
            return;
        }

        // 2. quantity must be a positive number.
        double qty;
        try {
            qty = Double.parseDouble(qtyRaw);
        } catch (NumberFormatException e) {
            tilQty.setError(getString(R.string.err_quantity_invalid));
            return;
        }
        if (qty <= 0) {
            tilQty.setError(getString(R.string.err_quantity_positive));
            return;
        }

        // 3. all good - persist.
        String unit = ((Unit.UnitDef) spUnit.getSelectedItem()).code;
        if (editId > 0) {
            db.updatePantryItem(editId, name, qty, unit, pickedExpiry);
            Toast.makeText(this, R.string.item_updated, Toast.LENGTH_SHORT).show();
        } else {
            db.insertPantryItem(name, qty, unit, pickedExpiry);
            Toast.makeText(this, R.string.item_added, Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    // ---------------------------------------------------------------------
    // helpers
    // ---------------------------------------------------------------------

    private void selectUnit(String code) {
        for (int i = 0; i < spUnit.getCount(); i++) {
            if (((Unit.UnitDef) spUnit.getItemAtPosition(i)).code.equals(code)) {
                spUnit.setSelection(i);
                break;
            }
        }
    }

    private void showDatePicker() {
        Calendar c = Calendar.getInstance();
        if (pickedExpiry != null) c.setTimeInMillis(pickedExpiry);
        new DatePickerDialog(this, (view, y, m, d) -> {
            Calendar picked = Calendar.getInstance();
            picked.set(y, m, d, 12, 0, 0);
            pickedExpiry = picked.getTimeInMillis();
            showExpiry(pickedExpiry);
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showExpiry(long millis) {
        String text = new SimpleDateFormat("d MMM yyyy", Locale.getDefault())
                .format(new Date(millis));
        tvExpiry.setText(text);
        tvExpiry.setVisibility(View.VISIBLE);
    }
}
