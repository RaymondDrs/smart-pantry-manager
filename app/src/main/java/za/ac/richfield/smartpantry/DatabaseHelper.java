package za.ac.richfield.smartpantry;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * The single source of truth for all persisted data: three tables in one
 * on-device SQLite database.
 *
 *   pantry_items        user's own ingredients (full CRUD from the UI)
 *   recipes             the seeded recipe collection (read-only from the UI)
 *   recipe_ingredients  one row per ingredient line of each recipe
 *
 * Chosen over Firebase/PostgreSQL because everything the app does is local
 * by design: no accounts, no sync, no server - the pantry lives in the
 * user's pocket and works fully offline (see report, section 3).
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "smart_pantry.db";
    public static final int DB_VERSION = 1;

    // ---- table: pantry_items ----
    public static final String T_PANTRY = "pantry_items";
    public static final String C_ID = "_id";
    public static final String C_NAME = "name";
    public static final String C_QTY = "quantity";
    public static final String C_UNIT = "unit";
    public static final String C_EXPIRY = "expiry_date";   // epoch millis, nullable

    // ---- table: recipes ----
    public static final String T_RECIPES = "recipes";
    public static final String R_ID = "_id";
    public static final String R_NAME = "name";
    public static final String R_CATEGORY = "category";
    public static final String R_STEPS = "steps";

    // ---- table: recipe_ingredients ----
    public static final String T_RECIPE_ING = "recipe_ingredients";
    public static final String RI_ID = "_id";
    public static final String RI_RECIPE_ID = "recipe_id";
    public static final String RI_NAME = "ingredient_name";   // canonical form
    public static final String RI_ORIGINAL = "original_name"; // as written in recipe
    public static final String RI_QTY = "quantity";
    public static final String RI_UNIT = "unit";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String pantry = "CREATE TABLE " + T_PANTRY + " ("
                + C_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + C_NAME + " TEXT NOT NULL, "
                + C_QTY + " REAL NOT NULL, "
                + C_UNIT + " TEXT NOT NULL, "
                + C_EXPIRY + " INTEGER)";   // null = no expiry set
        db.execSQL(pantry);

        String recipes = "CREATE TABLE " + T_RECIPES + " ("
                + R_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + R_NAME + " TEXT NOT NULL, "
                + R_CATEGORY + " TEXT NOT NULL, "
                + R_STEPS + " TEXT NOT NULL)";
        db.execSQL(recipes);

        String recipeIng = "CREATE TABLE " + T_RECIPE_ING + " ("
                + RI_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + RI_RECIPE_ID + " INTEGER NOT NULL, "
                + RI_NAME + " TEXT NOT NULL, "
                + RI_ORIGINAL + " TEXT, "
                + RI_QTY + " REAL NOT NULL, "
                + RI_UNIT + " TEXT NOT NULL, "
                + "FOREIGN KEY(" + RI_RECIPE_ID + ") REFERENCES " + T_RECIPES + "(" + R_ID + "))";
        db.execSQL(recipeIng);

        // Name lookup is the hot path for the matcher and duplicate checks.
        db.execSQL("CREATE INDEX idx_pantry_name ON " + T_PANTRY + "(" + C_NAME + ")");
        db.execSQL("CREATE INDEX idx_ri_recipe ON " + T_RECIPE_ING + "(" + RI_RECIPE_ID + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Version 1 is the first schema; nothing to migrate yet. Future
        // schema changes belong in switch(oldVersion) blocks here.
        db.execSQL("DROP TABLE IF EXISTS " + T_RECIPE_ING);
        db.execSQL("DROP TABLE IF EXISTS " + T_RECIPES);
        db.execSQL("DROP TABLE IF EXISTS " + T_PANTRY);
        onCreate(db);
    }

    // =====================================================================
    // PANTRY CRUD
    // =====================================================================

    /** CREATE: inserts a new pantry item; returns the new row id. */
    public long insertPantryItem(String rawName, double qty, String unit, Long expiry) {
        String canonical = IngredientNormalizer.normalize(rawName);
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(C_NAME, canonical);
        cv.put(C_QTY, qty);
        cv.put(C_UNIT, unit);
        cv.put(C_EXPIRY, expiry);
        return db.insert(T_PANTRY, null, cv);
    }

    /** READ: every pantry item, ordered alphabetically. */
    public List<PantryItem> getAllPantryItems() {
        List<PantryItem> items = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(T_PANTRY, null, null, null, null, null,
                C_NAME + " COLLATE NOCASE ASC");
        while (c.moveToNext()) {
            items.add(new PantryItem(
                    c.getLong(c.getColumnIndexOrThrow(C_ID)),
                    c.getString(c.getColumnIndexOrThrow(C_NAME)),
                    c.getDouble(c.getColumnIndexOrThrow(C_QTY)),
                    c.getString(c.getColumnIndexOrThrow(C_UNIT)),
                    c.isNull(c.getColumnIndexOrThrow(C_EXPIRY)) ? null
                            : c.getLong(c.getColumnIndexOrThrow(C_EXPIRY))));
        }
        c.close();
        return items;
    }

    /** READ: one pantry item by id (used by the Add/Edit screen). */
    public PantryItem getPantryItem(long id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(T_PANTRY, null, C_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null);
        PantryItem item = null;
        if (c.moveToFirst()) {
            item = new PantryItem(
                    c.getLong(c.getColumnIndexOrThrow(C_ID)),
                    c.getString(c.getColumnIndexOrThrow(C_NAME)),
                    c.getDouble(c.getColumnIndexOrThrow(C_QTY)),
                    c.getString(c.getColumnIndexOrThrow(C_UNIT)),
                    c.isNull(c.getColumnIndexOrThrow(C_EXPIRY)) ? null
                            : c.getLong(c.getColumnIndexOrThrow(C_EXPIRY)));
        }
        c.close();
        return item;
    }

    /** UPDATE: writes changes for an existing pantry item. */
    public int updatePantryItem(long id, String rawName, double qty, String unit, Long expiry) {
        String canonical = IngredientNormalizer.normalize(rawName);
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(C_NAME, canonical);
        cv.put(C_QTY, qty);
        cv.put(C_UNIT, unit);
        cv.put(C_EXPIRY, expiry);
        return db.update(T_PANTRY, cv, C_ID + "=?", new String[]{String.valueOf(id)});
    }

    /** DELETE: removes a pantry item permanently. */
    public int deletePantryItem(long id) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(T_PANTRY, C_ID + "=?", new String[]{String.valueOf(id)});
    }

    /** True when a pantry row already exists with the given canonical name. */
    public boolean pantryItemExists(String rawName, long ignoreId) {
        String canonical = IngredientNormalizer.normalize(rawName);
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(T_PANTRY, new String[]{C_ID},
                C_NAME + "=?" + (ignoreId > 0 ? " AND " + C_ID + "<>?" : ""),
                ignoreId > 0 ? new String[]{canonical, String.valueOf(ignoreId)}
                        : new String[]{canonical},
                null, null, null);
        boolean exists = c.moveToFirst();
        c.close();
        return exists;
    }

    // =====================================================================
    // RECIPES (read-only from the UI; seeded once by RecipeSeed)
    // =====================================================================

    /** READ: every recipe with its ingredient requirements attached. */
    public List<Recipe> getAllRecipes() {
        List<Recipe> recipes = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        Cursor c = db.query(T_RECIPES, null, null, null, null, null, R_NAME + " ASC");
        List<Long> ids = new ArrayList<>();
        List<String> names = new ArrayList<>();
        List<String> categories = new ArrayList<>();
        List<String> stepsList = new ArrayList<>();
        while (c.moveToNext()) {
            ids.add(c.getLong(c.getColumnIndexOrThrow(R_ID)));
            names.add(c.getString(c.getColumnIndexOrThrow(R_NAME)));
            categories.add(c.getString(c.getColumnIndexOrThrow(R_CATEGORY)));
            stepsList.add(c.getString(c.getColumnIndexOrThrow(R_STEPS)));
        }
        c.close();

        for (int i = 0; i < ids.size(); i++) {
            List<Recipe.Requirement> reqs = getRequirements(db, ids.get(i));
            recipes.add(new Recipe(ids.get(i), names.get(i), categories.get(i),
                    stepsList.get(i), reqs));
        }
        return recipes;
    }

    /** READ: one recipe by id, requirements included. */
    public Recipe getRecipe(long id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(T_RECIPES, null, R_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null);
        Recipe recipe = null;
        if (c.moveToFirst()) {
            String name = c.getString(c.getColumnIndexOrThrow(R_NAME));
            String category = c.getString(c.getColumnIndexOrThrow(R_CATEGORY));
            String steps = c.getString(c.getColumnIndexOrThrow(R_STEPS));
            c.close();
            recipe = new Recipe(id, name, category, steps, getRequirements(db, id));
        } else {
            c.close();
        }
        return recipe;
    }

    private List<Recipe.Requirement> getRequirements(SQLiteDatabase db, long recipeId) {
        List<Recipe.Requirement> reqs = new ArrayList<>();
        Cursor c = db.query(T_RECIPE_ING, null, RI_RECIPE_ID + "=?",
                new String[]{String.valueOf(recipeId)}, null, null, RI_ID + " ASC");
        while (c.moveToNext()) {
            String original = c.getString(c.getColumnIndexOrThrow(RI_ORIGINAL));
            reqs.add(new Recipe.Requirement(
                    c.getString(c.getColumnIndexOrThrow(RI_NAME)),
                    c.getDouble(c.getColumnIndexOrThrow(RI_QTY)),
                    c.getString(c.getColumnIndexOrThrow(RI_UNIT)),
                    original == null ? c.getString(c.getColumnIndexOrThrow(RI_NAME)) : original));
        }
        c.close();
        return reqs;
    }

    // =====================================================================
    // SEEDING SUPPORT (called by RecipeSeed on first run only)
    // =====================================================================

    /** True when the recipes table has no rows yet. */
    public boolean recipesTableEmpty() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT COUNT(*) FROM " + T_RECIPES, null);
        boolean empty = c.moveToFirst() && c.getLong(0) == 0;
        c.close();
        return empty;
    }

    /**
     * Inserts one complete recipe (row + its ingredient lines) inside a
     * transaction so a half-written recipe can never be left behind.
     */
    public long insertRecipe(Recipe recipe) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        long recipeId = -1;
        try {
            ContentValues rv = new ContentValues();
            rv.put(R_NAME, recipe.getName());
            rv.put(R_CATEGORY, recipe.getCategory());
            rv.put(R_STEPS, recipe.getSteps());
            recipeId = db.insert(T_RECIPES, null, rv);

            for (Recipe.Requirement req : recipe.getRequirements()) {
                ContentValues iv = new ContentValues();
                iv.put(RI_RECIPE_ID, recipeId);
                iv.put(RI_NAME, req.ingredientName);   // already canonical
                iv.put(RI_ORIGINAL, req.originalName);
                iv.put(RI_QTY, req.quantity);
                iv.put(RI_UNIT, req.unit);
                db.insert(T_RECIPE_ING, null, iv);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        return recipeId;
    }
}
