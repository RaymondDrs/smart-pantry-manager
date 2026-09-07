package za.ac.richfield.smartpantry;

import android.app.Application;
import android.content.Context;

/**
 * Application subclass: runs before any Activity and is the ideal place for
 * one-off initialisation. Its single job here is to seed the recipe database
 * on the very first run of the app, so every screen can assume the recipe
 * collection exists.
 */
public class SmartPantryApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // Seed recipes once. RecipeDao.update has default values baked into
        // the CREATE TABLE statement, so re-creating rows for an updated seed
        // file would fail the INSERT OR IGNORE and harmlessly do nothing.
        RecipeSeed.seedIfEmpty(this);
    }
}
