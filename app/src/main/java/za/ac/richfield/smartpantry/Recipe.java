package za.ac.richfield.smartpantry;

import java.util.ArrayList;
import java.util.List;

/**
 * A row of the recipes table plus its ingredient lines from
 * recipe_ingredients. One recipe = name, category, per-ingredient
 * requirements (name, quantity, unit) and ordered preparation steps.
 */
public class Recipe {

    private long id;
    private String name;
    private String category;          // e.g. "Breakfast", "Supper", "Snack"
    private String steps;             // numbered steps separated by \n
    private List<Requirement> requirements;

    /**
     * One required ingredient line. Quantity/unit is what the recipe needs;
     * the pantry side of the comparison happens in RecipeMatcher.
     */
    public static class Requirement {
        public final String ingredientName; // canonical (normalised) name
        public final double quantity;
        public final String unit;
        public final String originalName;   // as written in the recipe, for display

        public Requirement(String ingredientName, double quantity, String unit, String originalName) {
            this.ingredientName = ingredientName;
            this.quantity = quantity;
            this.unit = unit;
            this.originalName = originalName;
        }
    }

    public Recipe(long id, String name, String category, String steps, List<Requirement> requirements) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.steps = steps;
        this.requirements = requirements == null ? new ArrayList<>() : requirements;
    }

    /** Creates a not-yet-persisted recipe (used only by the seed loader). */
    public Recipe(String name, String category, String steps, List<Requirement> requirements) {
        this(-1, name, category, steps, requirements);
    }

    public long getId() { return id; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public String getSteps() { return steps; }
    public List<Requirement> getRequirements() { return requirements; }
}
