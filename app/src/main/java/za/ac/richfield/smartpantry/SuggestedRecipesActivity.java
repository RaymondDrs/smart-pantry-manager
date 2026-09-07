package za.ac.richfield.smartpantry;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

/**
 * The "Suggested Recipes" screen. Runs the strict-matching engine
 * (RecipeMatcher) against the current pantry and lists ONLY the recipes the
 * user can make right now. Recipes missing even one ingredient are excluded
 * from this list; those missing exactly one are shown separately, clearly
 * labelled, in the optional "Almost There" bonus section.
 */
public class SuggestedRecipesActivity extends AppCompatActivity {

    /** Marker object marking the "Almost There" section header row. */
    private static final Object HEADER_ALMOST = new Object();

    private DatabaseHelper db;
    private RecipeAdapter adapter;
    private TextView emptyView, tvSummary;
    private List<RecipeMatcher.MatchResult> results = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggested);

        db = new DatabaseHelper(this);
        tvSummary = findViewById(R.id.tvSummary);
        emptyView = findViewById(R.id.emptySuggested);

        RecyclerView rv = findViewById(R.id.rvSuggested);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecipeAdapter();
        rv.setAdapter(adapter);

        setupNav();
    }

    @Override
    protected void onResume() {
        super.onResume();
        reload();
    }

    /** Re-runs the matcher; called on entry and whenever the pantry changes. */
    private void reload() {
        List<PantryItem> pantry = db.getAllPantryItems();
        List<Recipe> recipes = db.getAllRecipes();
        results = RecipeMatcher.matchAll(recipes, pantry);

        // --- strict list: only recipes with every ingredient -------------
        List<RecipeMatcher.MatchResult> suggested = new ArrayList<>();
        List<RecipeMatcher.MatchResult> almost = new ArrayList<>();
        for (RecipeMatcher.MatchResult r : results) {
            if (r.fullyMatched) suggested.add(r);
            else if (r.isAlmostThere()) almost.add(r);
        }

        boolean showAlmost = getSharedPreferences("prefs", MODE_PRIVATE)
                .getBoolean("almost_there", true);

        if (showAlmost) {
            // The adapter shows the strict list first, then a clearly
            // labelled "Almost There" section, then the almost list.
            List<Object> rows = new ArrayList<>();
            rows.addAll(suggested);
            if (!almost.isEmpty()) {
                rows.add(HEADER_ALMOST);
                rows.addAll(almost);
            }
            adapter.submitList(rows);
        } else {
            adapter.submitList(new ArrayList<>(suggested));
        }

        tvSummary.setText(getString(R.string.suggested_count, suggested.size(), recipes.size()));
        emptyView.setVisibility(suggested.isEmpty() && (!showAlmost || almost.isEmpty())
                ? View.VISIBLE : View.GONE);
    }

    // ---------------------------------------------------------------------
    // adapter
    // ---------------------------------------------------------------------

    private class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.Row> {

        private List<Object> items = new ArrayList<>();
        private static final int TYPE_RECIPE = 0;
        private static final int TYPE_HEADER = 1;

        void submitList(List<Object> newItems) {
            items = newItems;
            notifyDataSetChanged();
        }

        @Override
        public int getItemViewType(int position) {
            return items.get(position) == HEADER_ALMOST ? TYPE_HEADER : TYPE_RECIPE;
        }

        @NonNull
        @Override
        public Row onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            int layout = viewType == TYPE_HEADER ? R.layout.item_section_header
                    : R.layout.item_recipe;
            View v = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
            return new Row(v, viewType);
        }

        @Override
        public void onBindViewHolder(@NonNull Row h, int position) {
            Object o = items.get(position);
            if (o == HEADER_ALMOST) {
                h.header.setText(R.string.almost_there_section);
                return;
            }
            RecipeMatcher.MatchResult r = (RecipeMatcher.MatchResult) o;
            h.name.setText(r.recipe.getName());
            h.meta.setText(getString(R.string.recipe_meta_fmt,
                    r.recipe.getCategory(), r.recipe.getRequirements().size()));
            if (r.fullyMatched) {
                h.badge.setVisibility(View.GONE);
            } else {
                // Almost-there rows carry an orange "1 missing" badge so the
                // two lists can never be confused.
                h.badge.setVisibility(View.VISIBLE);
                h.badge.setText(getString(R.string.missing_count_badge, r.missingCount()));
            }
            // The whole row opens RecipeDetailActivity with the recipe id.
            h.itemView.setOnClickListener(v -> {
                Intent it = new Intent(SuggestedRecipesActivity.this, RecipeDetailActivity.class);
                it.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, r.recipe.getId());
                startActivity(it);
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class Row extends RecyclerView.ViewHolder {
            final TextView name, meta, badge, header;

            Row(@NonNull View itemView, int viewType) {
                super(itemView);
                if (viewType == TYPE_HEADER) {
                    header = itemView.findViewById(R.id.tvSectionTitle);
                    name = null; meta = null; badge = null;
                } else {
                    header = null;
                    name = itemView.findViewById(R.id.tvRecipeName);
                    meta = itemView.findViewById(R.id.tvRecipeMeta);
                    badge = itemView.findViewById(R.id.tvBadge);
                }
            }
        }
    }

    private void setupNav() {
        BottomNavigationView nav = findViewById(R.id.bottomNav);
        nav.setSelectedItemId(R.id.nav_suggested);
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_suggested) {
                return true;
            } else if (id == R.id.nav_pantry) {
                startActivity(new Intent(this, MainActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });
    }
}
