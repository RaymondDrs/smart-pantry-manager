package za.ac.richfield.smartpantry;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * Recipe detail screen: full ingredient list (each line marked "Have" or
 * "Missing" against the current pantry) and the preparation steps. Opened
 * from the Suggested Recipes list via an Intent extra carrying the id.
 */
public class RecipeDetailActivity extends AppCompatActivity {

    public static final String EXTRA_RECIPE_ID = "recipe_id";

    private IngredientAdapter adapter;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        db = new DatabaseHelper(this);

        long id = getIntent().getLongExtra(EXTRA_RECIPE_ID, -1L);
        Recipe recipe = id > 0 ? db.getRecipe(id) : null;
        if (recipe == null) {
            finish();
            return;
        }

        TextView title = findViewById(R.id.tvRecipeTitle);
        TextView category = findViewById(R.id.tvRecipeCategory);
        TextView banner = findViewById(R.id.tvBanner);

        title.setText(recipe.getName());
        category.setText(recipe.getCategory());

        // Ingredient lines with per-line have/missing verdicts.
        List<Line> lines = new ArrayList<>();
        List<PantryItem> pantry = db.getAllPantryItems();
        for (Recipe.Requirement req : recipe.getRequirements()) {
            lines.add(new Line(req, RecipeMatcher.describe(req),
                    pantryHas(req, pantry)));
        }

        // Banner: "You have everything" (green) or "Missing N" (orange).
        int missing = 0;
        for (Line l : lines) if (!l.has) missing++;
        if (missing == 0) {
            banner.setText(R.string.you_have_all);
            banner.setBackgroundColor(ContextCompat.getColor(this, R.color.success_green));
        } else {
            banner.setText(getString(R.string.you_are_missing, missing));
            banner.setBackgroundColor(ContextCompat.getColor(this, R.color.accent_orange));
        }
        banner.setVisibility(View.VISIBLE);

        RecyclerView rv = findViewById(R.id.rvIngredients);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new IngredientAdapter(lines);
        rv.setAdapter(adapter);

        // Method steps.
        TextView steps = findViewById(R.id.tvSteps);
        steps.setText(recipe.getSteps());
    }

    /** Re-checks one requirement against the current pantry (live verdict). */
    private boolean pantryHas(Recipe.Requirement req, List<PantryItem> pantry) {
        for (PantryItem item : pantry) {
            if (item.getName().equals(req.ingredientName)) {
                return RecipeMatcher.hasEnough(item, req);
            }
        }
        return false;
    }

    // ---------------------------------------------------------------------
    // adapter
    // ---------------------------------------------------------------------

    private class IngredientAdapter extends RecyclerView.Adapter<IngredientAdapter.Row> {

        private final List<Line> lines;

        IngredientAdapter(List<Line> lines) { this.lines = lines; }

        @NonNull
        @Override
        public Row onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_ingredient_line, parent, false);
            return new Row(v);
        }

        @Override
        public void onBindViewHolder(@NonNull Row h, int position) {
            Line l = lines.get(position);
            h.req.setText(l.display);
            if (l.has) {
                h.flag.setText(R.string.have);
                h.flag.setTextColor(ContextCompat.getColor(RecipeDetailActivity.this, R.color.success_green));
            } else {
                h.flag.setText(R.string.missing);
                h.flag.setTextColor(ContextCompat.getColor(RecipeDetailActivity.this, R.color.error_red));
            }
        }

        @Override
        public int getItemCount() { return lines.size(); }

        class Row extends RecyclerView.ViewHolder {
            final TextView req, flag;

            Row(@NonNull View itemView) {
                super(itemView);
                req = itemView.findViewById(R.id.tvReq);
                flag = itemView.findViewById(R.id.tvFlag);
            }
        }
    }

    /** One displayable ingredient line plus its verdict. */
    private static class Line {
        final Recipe.Requirement requirement;
        final String display;
        final boolean has;

        Line(Recipe.Requirement requirement, String display, boolean has) {
            this.requirement = requirement;
            this.display = display;
            this.has = has;
        }
    }
}
