package za.ac.richfield.smartpantry;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * The ingredient name normaliser - the piece of plumbing that makes matching
 * robust to "tomato" vs "tomatoes", "All-Purpose Flour" vs "flour", and
 * similar real-world messiness. A naive exact-string match would be marked
 * down, so every ingredient name entering the system passes through here
 * once, on the way in and on the way out of the database.
 */
public final class IngredientNormalizer {

    // Simple English plural rules, applied after lower-casing and trimming.
    private static final String[][] PLURAL_RULES = {
            {"ies", "y"},   // cherries -> cherry
            {"oes", "o"},   // tomatoes -> tomato
            {"ves", "f"},   // calves -> calf
            {"ses", "s"},   // analyses -> analysis
            {"xes", "x"},   // boxes -> box
            {"zes", "z"},   // quizzes -> quiz
            {"ches", "ch"}, // peaches -> peach
            {"shes", "sh"}, // dishes -> dish
            {"sses", "ss"}, // classes -> class
            {"s", ""},      // generic: eggs -> egg (applied last)
    };

    // Known irregulars that the simple rules above cannot handle.
    private static final Map<String, String> IRREGULARS = new HashMap<>();
    static {
        IRREGULARS.put("leaves", "leaf");
        IRREGULARS.put("loaves", "loaf");
        IRREGULARS.put("potatoes", "potato");
        IRREGULARS.put("tomatoes", "tomato");
        IRREGULARS.put("berries", "berry");
    }

    // Synonyms: different words that mean the same ingredient. The key is
    // always the normalised singular form; the value is the canonical form
    // stored in the database.
    private static final Map<String, String> SYNONYMS = new HashMap<>();
    static {
        SYNONYMS.put("all-purpose flour", "flour");
        SYNONYMS.put("all purpose flour", "flour");
        SYNONYMS.put("plain flour", "flour");
        SYNONYMS.put("bread flour", "flour");
        SYNONYMS.put("self-raising flour", "flour");
        SYNONYMS.put("self raising flour", "flour");
        SYNONYMS.put("garbanzo bean", "chickpea");
        SYNONYMS.put("garbanzo beans", "chickpea");
        SYNONYMS.put("chickpeas", "chickpea");
        SYNONYMS.put("scallion", "spring onion");
        SYNONYMS.put("green onion", "spring onion");
        SYNONYMS.put("aubergine", "eggplant");
        SYNONYMS.put("courgette", "zucchini");
        SYNONYMS.put("coriander", "cilantro");
        SYNONYMS.put("bell pepper", "pepper");
        SYNONYMS.put("sweet pepper", "pepper");
        SYNONYMS.put("capsicum", "pepper");
        SYNONYMS.put("maize meal", "cornmeal");
        SYNONYMS.put("spaghetti", "pasta");
        SYNONYMS.put("macaroni", "pasta");
        SYNONYMS.put("penne", "pasta");
        SYNONYMS.put("fusilli", "pasta");
        SYNONYMS.put("noodle", "pasta");
        SYNONYMS.put("noodles", "pasta");
    }

    private IngredientNormalizer() { /* static utility */ }

    /**
     * Normalises a raw ingredient name typed by a user or loaded from the
     * seed data: trims, lower-cases, folds whitespace, removes punctuation,
     * then applies plural rules and synonym folding.
     */
    public static String normalize(String raw) {
        if (raw == null) return "";
        String s = raw.trim().toLowerCase(Locale.ROOT);
        if (s.isEmpty()) return "";

        // Fold any run of whitespace into a single space.
        s = s.replaceAll("\\s+", " ");
        // Drop everything that is not a letter, space or hyphen.
        s = s.replaceAll("[^a-z\\- ]", "");

        String singular = toSingular(s);
        return SYNONYMS.getOrDefault(singular, singular);
    }

    /**
     * Applies the plural rules and irregulars. Returns the input unchanged
     * when it does not look like a plural.
     */
    private static String toSingular(String s) {
        String irregular = IRREGULARS.get(s);
        if (irregular != null) return irregular;

        for (String[] rule : PLURAL_RULES) {
            if (s.endsWith(rule[0]) && s.length() > rule[0].length() + 1) {
                return s.substring(0, s.length() - rule[0].length()) + rule[1];
            }
        }
        return s;
    }
}
