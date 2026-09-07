package za.ac.richfield.smartpantry;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Looper;
import android.view.View;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;
import org.robolectric.Shadows;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

/**
 * NOT a unit test in the usual sense - a screenshot renderer. It boots each
 * Activity of the real app under Robolectric in NATIVE graphics mode (the
 * real Android rendering pipeline running on the JVM), with the real
 * DatabaseHelper, the real first-run RecipeSeed and a demo pantry, then
 * saves what the framework actually rendered as PNG screenshots.
 */
@RunWith(RobolectricTestRunner.class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = 34, qualifiers = "w411dp-h891dp-420dpi")
public class ScreenshotRenderer {

    private static final String OUT_DIR = "build/screenshots";

    private static final String[][] DEMO_PANTRY = {
            {"Tomatoes",        "3",    "pc"},
            {"Eggs",            "6",    "egg"},
            {"Bread",           "6",    "slice"},
            {"Butter",          "0.5",  "kg"},
            {"Flour",           "0.5",  "kg"},
            {"Milk",            "1",    "l"},
            {"Sugar",           "250",  "g"},
            {"Cheddar Cheese",  "200",  "g"},
            {"Tinned Tuna",     "1",    "can"},
            {"Mayonnaise",      "200",  "g"},
            {"Black Pepper",    "1",    "g"},
            {"Oats",            "500",  "g"},
            {"Peanut Butter",   "200",  "g"},
            {"Honey",           "250",  "ml"},
    };

    private String outDir() {
        String d = System.getProperty("user.dir") + File.separator + OUT_DIR;
        new File(d).mkdirs();
        return d;
    }

    private void seedPantry(Context ctx) {
        DatabaseHelper db = new DatabaseHelper(ctx);
        db.getWritableDatabase().delete(DatabaseHelper.T_PANTRY, null, null);
        for (String[] row : DEMO_PANTRY) {
            db.insertPantryItem(row[0], Double.parseDouble(row[1]), row[2], null);
        }
        db.close();
    }

    private void snap(Activity activity, String filename) throws Exception {
        View root = activity.getWindow().getDecorView();
        Shadows.shadowOf(Looper.getMainLooper()).idle();

        if (root.getWidth() == 0 || root.getHeight() == 0) {
            android.util.DisplayMetrics dm = activity.getResources().getDisplayMetrics();
            root.measure(
                    View.MeasureSpec.makeMeasureSpec(dm.widthPixels, View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(dm.heightPixels, View.MeasureSpec.EXACTLY));
            root.layout(0, 0, dm.widthPixels, dm.heightPixels);
        }
        root.invalidate();

        Bitmap bmp = Bitmap.createBitmap(root.getWidth(), root.getHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmp);
        root.draw(c);
        File f = new File(outDir(), filename);
        try (FileOutputStream out = new FileOutputStream(f)) {
            bmp.compress(Bitmap.CompressFormat.PNG, 100, out);
        }
        System.out.println("SAVED " + f.getAbsolutePath() + " (" + bmp.getWidth()
                + "x" + bmp.getHeight() + ")");
    }

    @Test
    public void render01_pantryEmpty() throws Exception {
        ActivityController<MainActivity> c = Robolectric.buildActivity(MainActivity.class);
        c.setup();
        snap(c.get(), "01_pantry_empty.png");
        c.get().finish();
    }

    @Test
    public void render02_pantryWithItems() throws Exception {
        ActivityController<MainActivity> c = Robolectric.buildActivity(MainActivity.class);
        c.setup();
        seedPantry(c.get());
        c.resume();          // re-fires onResume -> reload() picks up new rows
        snap(c.get(), "02_pantry_items.png");
        c.get().finish();
    }

    @Test
    public void render03_addForm() throws Exception {
        ActivityController<AddEditIngredientActivity> c =
                Robolectric.buildActivity(AddEditIngredientActivity.class);
        c.setup();
        snap(c.get(), "03_add_ingredient.png");
        c.get().finish();
    }

    @Test
    public void render04_editForm() throws Exception {
        DatabaseHelper db = new DatabaseHelper(RuntimeEnvironment.getApplication());
        db.getWritableDatabase().delete(DatabaseHelper.T_PANTRY, null, null);
        long id = db.insertPantryItem("Tomatoes", 3, "pc", null);
        db.close();
        Intent i = new Intent(RuntimeEnvironment.getApplication(),
                AddEditIngredientActivity.class);
        i.putExtra(AddEditIngredientActivity.EXTRA_ID, id);
        ActivityController<AddEditIngredientActivity> c =
                Robolectric.buildActivity(AddEditIngredientActivity.class, i);
        c.setup();
        snap(c.get(), "04_edit_ingredient.png");
        c.get().finish();
    }

    @Test
    public void render05_suggested() throws Exception {
        ActivityController<SuggestedRecipesActivity> c =
                Robolectric.buildActivity(SuggestedRecipesActivity.class);
        c.setup();
        seedPantry(c.get());
        c.resume();          // re-run the matcher against the seeded pantry
        snap(c.get(), "05_suggested_recipes.png");
        c.get().finish();
    }

    @Test
    public void render06_recipeDetail() throws Exception {
        seedPantry(RuntimeEnvironment.getApplication());
        DatabaseHelper db = new DatabaseHelper(RuntimeEnvironment.getApplication());
        List<Recipe> recipes = db.getAllRecipes();
        long recipeId = -1;
        for (Recipe r : recipes) {
            if (r.getName().equals("Scrambled Eggs on Toast")) recipeId = r.getId();
        }
        db.close();
        Intent i = new Intent(RuntimeEnvironment.getApplication(),
                RecipeDetailActivity.class);
        i.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, recipeId);
        ActivityController<RecipeDetailActivity> c =
                Robolectric.buildActivity(RecipeDetailActivity.class, i);
        c.setup();
        snap(c.get(), "06_recipe_detail.png");
        c.get().finish();
    }

    @Test
    public void render07_settings() throws Exception {
        ActivityController<SettingsActivity> c =
                Robolectric.buildActivity(SettingsActivity.class);
        c.setup();
        snap(c.get(), "07_settings.png");
        c.get().finish();
    }
}
