package za.ac.richfield.smartpantry;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * The pantry list screen - the app's home. Shows every ingredient the user
 * has, drawn straight from SQLite through a RecyclerView + custom adapter.
 * Hosts the bottom navigation bar (Pantry / Suggested / Settings) that is
 * reused on every screen, and the FAB that opens the Add/Edit form.
 */
public class MainActivity extends AppCompatActivity {

    private PantryAdapter adapter;
    private DatabaseHelper db;
    private TextView emptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = new DatabaseHelper(this);

        // --- RecyclerView wiring -----------------------------------------
        RecyclerView rv = findViewById(R.id.rvPantry);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PantryAdapter();
        rv.setAdapter(adapter);

        emptyView = findViewById(R.id.emptyPantry);

        // --- Bottom navigation --------------------------------------------
        BottomNavigationView nav = findViewById(R.id.bottomNav);
        nav.setSelectedItemId(R.id.nav_pantry);  // highlight current tab
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_pantry) {
                return true;                       // already here
            } else if (id == R.id.nav_suggested) {
                startActivity(new Intent(this, SuggestedRecipesActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });

        // --- FAB -> Add/Edit form ----------------------------------------
        FloatingActionButton fab = findViewById(R.id.fabAdd);
        fab.setOnClickListener(v ->
                startActivity(new Intent(this, AddEditIngredientActivity.class)));
    }

    /**
     * Re-reads the pantry every time the screen comes back to the front, so
     * additions/edits made in AddEditIngredientActivity are reflected
     * immediately without any manual refresh. (onStart/onResume lifecycle.)
     */
    @Override
    protected void onResume() {
        super.onResume();
        reload();
    }

    private void reload() {
        List<PantryItem> items = db.getAllPantryItems();
        adapter.submitList(items);
        emptyView.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
    }

    // =====================================================================
    // ADAPTER + VIEW HOLDER (custom adapter over the pantry table)
    // =====================================================================

    /** Row view holder: name, quantity+unit, expiry line, edit/delete clicks. */
    private class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.Row> {

        private List<PantryItem> items = new ArrayList<>();

        @SuppressLint("NotifyDataSetChanged")
        void submitList(List<PantryItem> newItems) {
            items = newItems;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public Row onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_pantry, parent, false);
            return new Row(v);
        }

        @Override
        public void onBindViewHolder(@NonNull Row h, int position) {
            PantryItem item = items.get(position);

            h.name.setText(item.getName());
            h.qty.setText(getString(R.string.qty_unit_fmt,
                    RecipeMatcher.trimQty(item.getQuantity()), Unit.labelOf(item.getUnit())));

            // Expiry line: "Expires 12 Aug 2025" or a red "expiring soon"
            // badge when Settings has alerts on and the date is within 3 days.
            boolean expiring = item.isExpiringSoon(3);
            if (item.getExpiryDate() != null) {
                String date = new SimpleDateFormat("d MMM yyyy", Locale.getDefault())
                        .format(new Date(item.getExpiryDate()));
                if (expiring && prefsExpiryAlertsOn()) {
                    h.expiry.setText(getString(R.string.expiring_badge) + " · " + date);
                    h.expiry.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.error_red));
                } else {
                    h.expiry.setText(getString(R.string.expires_fmt, date));
                    h.expiry.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.text_secondary));
                }
                h.expiry.setVisibility(View.VISIBLE);
            } else {
                h.expiry.setVisibility(View.GONE);
            }

            // Edit: open the same form with the item's data pre-filled.
            h.itemView.setOnClickListener(v -> {
                Intent it = new Intent(MainActivity.this, AddEditIngredientActivity.class);
                it.putExtra(AddEditIngredientActivity.EXTRA_ID, item.getId());
                startActivity(it);
            });

            // Delete: confirm first, then remove and refresh the list.
            h.btnDelete.setOnClickListener(v -> confirmDelete(item));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class Row extends RecyclerView.ViewHolder {
            final TextView name, qty, expiry;
            final View btnDelete;

            Row(@NonNull View itemView) {
                super(itemView);
                name = itemView.findViewById(R.id.tvName);
                qty = itemView.findViewById(R.id.tvQty);
                expiry = itemView.findViewById(R.id.tvExpiry);
                btnDelete = itemView.findViewById(R.id.btnDelete);
            }
        }
    }

    private boolean prefsExpiryAlertsOn() {
        return getSharedPreferences("prefs", MODE_PRIVATE)
                .getBoolean("expiry_alerts", true);
    }

    private void confirmDelete(PantryItem item) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_delete_title)
                .setMessage(getString(R.string.confirm_delete_msg, item.getName()))
                .setPositiveButton(R.string.yes, (d, w) -> {
                    db.deletePantryItem(item.getId());
                    Toast.makeText(this, R.string.deleted, Toast.LENGTH_SHORT).show();
                    reload();
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }
}
