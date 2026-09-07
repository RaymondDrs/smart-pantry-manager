package za.ac.richfield.smartpantry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * The STRICT-MATCHING ENGINE - the single most important piece of business
 * logic in the assignment (see brief, section 2.3).
 *
 * A recipe qualifies as "suggested" ONLY when every single ingredient it
 * requires is present in the pantry, in at least the required quantity.
 * Partial matches, "almost there" recipes, or recipes requiring even one
 * missing ingredient are EXCLUDED from the main suggestions list.
 *
 * How a single requirement is judged (in order):
 *   1. The requirement's ingredient name is already canonical (seeded data
 *      is normalised at insert; user input is normalised in DatabaseHelper).
 *   2. We look the name up in the pantry map. No pantry entry -> missing.
 *   3. If there is a pantry entry, we compare quantities. Units in the same
 *      family are converted to a common base (see Unit.convert) before
 *      comparing, so "0.5 kg flour" satisfies "200 g flour".
 *   4. If units are in different families (e.g. pantry has 3 tomatoes by
 *      count, recipe wants 200 g tomato), the requirement is treated as
 *      satisfied when the pantry quantity is positive - a pragmatic
 *      judgement call, documented in the report, which avoids punishing
 *      users for the very common mass/count overlap in home cooking.
 */
public class RecipeMatcher {

    /** A recipe plus the verdict of the strict matcher for one pantry. */
    public static class MatchResult {
        public final Recipe recipe;
        public final boolean fullyMatched;
        public final List<Recipe.Requirement> missing;      // not in pantry at all
        public final List<Recipe.Requirement> insufficient; // in pantry, but not enough

        MatchResult(Recipe recipe, boolean fullyMatched,
                    List<Recipe.Requirement> missing,
                    List<Recipe.Requirement> insufficient) {
            this.recipe = recipe;
            this.fullyMatched = fullyMatched;
            this.missing = missing;
            this.insufficient = insufficient;
        }

        /** Number of unsatisfied requirements (for the "Almost There" list). */
        public int missingCount() {
            return missing.size() + insufficient.size();
        }

        public boolean isAlmostThere() {
            return !fullyMatched && missingCount() == 1;
        }
    }

    /**
     * Runs the strict rule over every recipe against the given pantry.
     *
     * @param recipes the full recipe collection
     * @param pantry  the user's current pantry items
     * @return one MatchResult per recipe, in the same order
     */
    public static List<MatchResult> matchAll(List<Recipe> recipes, List<PantryItem> pantry) {
        // Index the pantry by canonical ingredient name for O(1) lookups.
        Map<String, PantryItem> pantryByName = new HashMap<>();
        for (PantryItem item : pantry) {
            PantryItem existing = pantryByName.get(item.getName());
            // Two pantry rows can hold the same ingredient in different units
            // (e.g. 200 g flour and 0.5 kg flour). Keep the entry with the
            // larger base-quantity so splitting stock across rows never
            // wrongly fails a recipe.
            if (existing == null || baseQuantity(item) >= baseQuantity(existing)) {
                pantryByName.put(item.getName(), item);
            }
        }

        List<MatchResult> results = new ArrayList<>(recipes.size());
        for (Recipe recipe : recipes) {
            results.add(matchOne(recipe, pantryByName));
        }
        return results;
    }

    /** Applies the strict rule to a single recipe against a pantry index. */
    static MatchResult matchOne(Recipe recipe, Map<String, PantryItem> pantryByName) {
        List<Recipe.Requirement> missing = new ArrayList<>();
        List<Recipe.Requirement> insufficient = new ArrayList<>();
        boolean fullyMatched = true;

        for (Recipe.Requirement req : recipe.getRequirements()) {
            PantryItem pantryItem = pantryByName.get(req.ingredientName);
            if (pantryItem == null) {
                missing.add(req);
                fullyMatched = false;
                continue;
            }
            if (!hasEnough(pantryItem, req)) {
                insufficient.add(req);
                fullyMatched = false;
            }
        }
        return new MatchResult(recipe, fullyMatched, missing, insufficient);
    }

    /**
     * Quantity comparison with unit conversion. Returns true when the pantry
     * holds at least the required amount of the ingredient.
     */
    static boolean hasEnough(PantryItem pantryItem, Recipe.Requirement req) {
        // Same unit code -> direct comparison.
        if (pantryItem.getUnit().equals(req.unit)) {
            return pantryItem.getQuantity() + EPSILON >= req.quantity;
        }

        // Same family, different units -> convert pantry into recipe units.
        Double converted = Unit.convert(pantryItem.getQuantity(), pantryItem.getUnit(), req.unit);
        if (converted != null) {
            return converted + EPSILON >= req.quantity;
        }

        // Different families (e.g. count vs mass): see class javadoc - treat
        // any positive pantry amount as satisfying the requirement.
        return pantryItem.getQuantity() > 0;
    }

    /** Convert any pantry item into its family's base unit for comparisons. */
    private static double baseQuantity(PantryItem item) {
        Double base = Unit.convert(item.getQuantity(), item.getUnit(), baseUnitOf(item));
        return base == null ? item.getQuantity() : base;
    }

    private static String baseUnitOf(PantryItem item) {
        switch (Unit.byCode(item.getUnit()).family) {
            case MASS:   return Unit.GRAM;
            case VOLUME: return Unit.MILLILITRE;
            default:     return Unit.PIECE;
        }
    }

    /** Tiny tolerance so 0.1+0.2 style float noise never fails a match. */
    private static final double EPSILON = 1e-9;

    /** Formats a requirement for display, e.g. "200 g flour", "3 eggs",
     *  "2 slices bread" or "1 tomato" - as natural as a recipe card. */
    public static String describe(Recipe.Requirement req) {
        String unitLabel = Unit.labelOf(req.unit);
        String name = req.originalName;
        String nameKey = IngredientNormalizer.normalize(name);

        // Count units sometimes name the ingredient itself: "3 egg eggs"
        // reads badly, so when the unit label IS the ingredient (egg, clove,
        // slice, ...) the unit is dropped and the display becomes "3 eggs".
        if (unitLabel != null && !unitLabel.isEmpty()
                && nameKey.equals(IngredientNormalizer.normalize(unitLabel))) {
            return trimQty(req.quantity) + " " + name;
        }
        // A generic "piece" adds nothing the name does not: "1 tomato" and
        // "0.5 cucumber" beat "1 pc tomato" and "0.5 pc cucumber".
        if (Unit.PIECE.equals(req.unit)) {
            return trimQty(req.quantity) + " " + name;
        }
        // Pluralise other count units when more than one is needed:
        // "2 slices bread", "2 cloves garlic", "2 cans tinned tuna".
        if (req.quantity > 1.5 && Unit.byCode(req.unit).family == Unit.Family.COUNT) {
            return trimQty(req.quantity) + " " + pluralOf(unitLabel) + " " + name;
        }
        return trimQty(req.quantity) + " " + unitLabel + " " + name;
    }

    /** Simple plural for count-unit labels: slice -> slices, bunch -> bunches. */
    private static String pluralOf(String label) {
        if (label == null || label.isEmpty()) return label;
        if (label.endsWith("ch") || label.endsWith("sh") || label.endsWith("s")
                || label.endsWith("x")) return label + "es";
        return label + "s";
    }

    /** Formats a quantity without trailing zeros: 2.0 -> "2", 0.5 -> "0.5". */
    public static String trimQty(double d) {
        if (d == Math.floor(d)) return String.valueOf((long) d);
        String s = String.format(Locale.US, "%.2f", d);
        if (s.endsWith("0")) s = s.substring(0, s.length() - 1);  // 0.50 -> 0.5
        return s;
    }
}
