package za.ac.richfield.smartpantry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Central definition of the measurement units the app understands, plus the
 * conversion rules that make quantity comparison possible across units.
 *
 * Design notes
 * ------------
 * 1. Every unit belongs to a "family" (MASS, VOLUME, COUNT). Two units can
 *    only be converted into each other when they share a family.
 * 2. Each unit is stored internally relative to the family's base unit
 *    (grams for MASS, millilitres for VOLUME, pieces for COUNT), so any
 *    conversion a -> b is a single multiply/divide, never a lookup chain.
 *    e.g. 500 g -> kg : 500 * (1/1000) = 0.5 kg
 * 3. COUNT is special: piece == clove == slice == egg == stick == tin == can
 *    == bunch == sprig. They all mean "one countable thing of X". Comparing
 *    "2 eggs" against a recipe needing "1 egg" therefore works, and so does
 *    "1 clove garlic" vs "3 cloves garlic".
 */
public final class Unit {

    public enum Family { MASS, VOLUME, COUNT }

    // ---- unit codes (persisted in the DB) ----
    public static final String GRAM = "g";
    public static final String KILOGRAM = "kg";
    public static final String MILLILITRE = "ml";
    public static final String LITRE = "l";
    public static final String PIECE = "pc";
    public static final String CLOVE = "clove";
    public static final String SLICE = "slice";
    public static final String EGG = "egg";
    public static final String STICK = "stick";
    public static final String TIN = "tin";
    public static final String CAN = "can";
    public static final String BUNCH = "bunch";
    public static final String SPRIG = "sprig";
    public static final String TEASPOON = "tsp";
    public static final String TABLESPOON = "tbsp";

    public static final class UnitDef {
        public final String code;
        public final String label;        // human-readable, shown in spinner
        public final Family family;
        public final double toBaseFactor; // multiply a value in this unit by this to get the base unit

        UnitDef(String code, String label, Family family, double toBaseFactor) {
            this.code = code; this.label = label; this.family = family;
            this.toBaseFactor = toBaseFactor;
        }
    }

    private static final Map<String, UnitDef> UNITS = new HashMap<>();
    static {
        UNITS.put(GRAM,       new UnitDef(GRAM,       "g",            Family.MASS,   1.0));
        UNITS.put(KILOGRAM,   new UnitDef(KILOGRAM,   "kg",           Family.MASS,   1000.0));
        UNITS.put(MILLILITRE, new UnitDef(MILLILITRE, "ml",           Family.VOLUME, 1.0));
        UNITS.put(LITRE,      new UnitDef(LITRE,      "l",            Family.VOLUME, 1000.0));
        UNITS.put(TEASPOON,   new UnitDef(TEASPOON,   "tsp (5 ml)",   Family.VOLUME, 5.0));
        UNITS.put(TABLESPOON, new UnitDef(TABLESPOON, "tbsp (15 ml)", Family.VOLUME, 15.0));
        UNITS.put(PIECE,      new UnitDef(PIECE,      "piece",        Family.COUNT,  1.0));
        UNITS.put(CLOVE,      new UnitDef(CLOVE,      "clove",        Family.COUNT,  1.0));
        UNITS.put(SLICE,      new UnitDef(SLICE,      "slice",        Family.COUNT,  1.0));
        UNITS.put(EGG,        new UnitDef(EGG,        "egg",          Family.COUNT,  1.0));
        UNITS.put(STICK,      new UnitDef(STICK,      "stick",        Family.COUNT,  1.0));
        UNITS.put(TIN,        new UnitDef(TIN,        "tin",          Family.COUNT,  1.0));
        UNITS.put(CAN,        new UnitDef(CAN,        "can",          Family.COUNT,  1.0));
        UNITS.put(BUNCH,      new UnitDef(BUNCH,      "bunch",        Family.COUNT,  1.0));
        UNITS.put(SPRIG,      new UnitDef(SPRIG,      "sprig",        Family.COUNT,  1.0));
    }

    private Unit() { /* static utility class */ }

    /** Ordered list of units as they appear in the unit spinner. */
    public static List<UnitDef> all() {
        List<UnitDef> list = new ArrayList<>();
        list.add(UNITS.get(PIECE));
        list.add(UNITS.get(EGG));
        list.add(UNITS.get(CLOVE));
        list.add(UNITS.get(SLICE));
        list.add(UNITS.get(STICK));
        list.add(UNITS.get(TIN));
        list.add(UNITS.get(CAN));
        list.add(UNITS.get(BUNCH));
        list.add(UNITS.get(SPRIG));
        list.add(UNITS.get(GRAM));
        list.add(UNITS.get(KILOGRAM));
        list.add(UNITS.get(MILLILITRE));
        list.add(UNITS.get(LITRE));
        list.add(UNITS.get(TEASPOON));
        list.add(UNITS.get(TABLESPOON));
        return list;
    }

    /** Look up a unit by its persisted code; falls back to pieces. */
    public static UnitDef byCode(String code) {
        UnitDef def = UNITS.get(code);
        return def != null ? def : UNITS.get(PIECE);
    }

    /** Short display label for a code, e.g. "kg" -> "kg". */
    public static String labelOf(String code) {
        return byCode(code).label;
    }

    /**
     * Converts a quantity from one unit to another when they are in the same
     * family. Returns null when the units are not comparable (e.g. g vs ml),
     * letting the caller decide how to treat the mismatch.
     */
    public static Double convert(double value, String fromCode, String toCode) {
        UnitDef from = UNITS.get(fromCode);
        UnitDef to = UNITS.get(toCode);
        if (from == null || to == null || from.family != to.family) return null;
        double inBase = value * from.toBaseFactor;
        return inBase / to.toBaseFactor;
    }

    /**
     * Two unit codes are "compatible" when they belong to the same family.
     */
    public static boolean compatible(String a, String b) {
        UnitDef ua = UNITS.get(a);
        UnitDef ub = UNITS.get(b);
        return ua != null && ub != null && ua.family == ub.family;
    }
}
