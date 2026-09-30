# Smart Pantry Manager

**Smart Pantry Manager** is an Android application (written in **Java**) that helps users reduce food waste by tracking the ingredients they actually have at home — their *pantry* — and suggesting recipes they can cook **using strictly those leftover ingredients**. No shopping trip required, and no recipe is suggested unless the user already has every single ingredient it needs, in at least the required quantity.

## The core idea: strict matching

The single most important piece of business logic in the app is the **strict-matching rule**:

> A recipe may only be shown as *suggested* if **every single ingredient** it requires is currently present in the user's pantry, **in at least the required quantity**.

- If a recipe needs 5 ingredients and the pantry has 4 of them, that recipe **must not** appear in the suggestions list.
- Partial matches, "almost there" recipes, or recipes requiring one missing ingredient are excluded from the main suggestions list (a separate, clearly labelled **"Almost There"** section lists recipes missing exactly one ingredient, and can be turned off in Settings).
- Matching is robust to real-world messiness: `tomato` vs `tomatoes` (singular/plural folding), `spaghetti` vs `pasta` (synonym folding), and `0.5 kg` vs `500 g` (unit-family conversion) all match correctly.

## Screens

| # | Screen | What it does |
|---|--------|--------------|
| 1 | **My Pantry** (`MainActivity`) | Home screen. RecyclerView list of pantry items from SQLite; add (FAB), edit (tap row), delete (bin icon + confirm); expiring-soon badges |
| 2 | **Add / Edit Ingredient** (`AddEditIngredientActivity`) | One form for create & update, chosen via Intent extra; input validation (non-empty unique name, positive numeric quantity); optional expiry date picker |
| 3 | **Suggested Recipes** (`SuggestedRecipesActivity`) | Runs the strict-matching engine against the current pantry; summary line; empty-state feedback; clearly separated "Almost There" section |
| 4 | **Recipe Detail** (`RecipeDetailActivity`) | Full ingredient list with per-line Have/Missing flags, banner, numbered method steps |
| 5 | **Settings** (`SettingsActivity`) | Expiring-soon alert toggle, units preference, "Almost There" toggle, about |

Navigation between all screens uses explicit **Intents** (data passed via extras), and every screen hosts the same **bottom navigation bar** (Pantry / Suggested / Settings).

## Database choice: SQLite (and why)

The app persists data with **on-device SQLite** via `SQLiteOpenHelper` (`DatabaseHelper.java`), chosen over Firebase and PostgreSQL:

- **The app is local by design.** There are no accounts, no sharing, no sync; the pantry lives in the user's pocket. A cloud database would add latency, cost, and a login flow that this app simply does not need.
- **It works fully offline.** Kitchen use-cases happen with spotty connectivity; SQLite runs in-process with zero network dependency.
- **It matches the module syllabus.** Persistent data storage with SQLiteOpenHelper is the approach taught in the module's persistent data chapter, and the relational shape (recipes → recipe_ingredients) is naturally relational.
- **Zero setup for the marker.** No Firebase project or Supabase instance to configure; clone and run.

### Data model

Three tables in `smart_pantry.db`:

```
pantry_items        (_id, name, quantity, unit, expiry_date?)
recipes             (_id, name, category, steps)
recipe_ingredients  (_id, recipe_id → recipes._id, ingredient_name, original_name, quantity, unit)
```

- **Create**: `insertPantryItem(...)` — from the Add/Edit form.
- **Read**: `getAllPantryItems()`, `getPantryItem(id)`, `getAllRecipes()`, `getRecipe(id)`.
- **Update**: `updatePantryItem(...)`.
- **Delete**: `deletePantryItem(id)` — from the row's bin icon or the edit screen.
- Data genuinely persists: the SQLite file survives app restarts, and the video demonstrates closing and reopening the app with the pantry intact.

## The 20 seeded recipes

The starter collection spans breakfast (4), lunch (4), supper (8) and snacks/baking (4): Scrambled Eggs on Toast, Basic Pancakes, Overnight Oats, French Toast, Classic Tomato Omelette, Tuna Mayonnaise Sandwich, Chickpea Salad, Cheese Toastie, Spaghetti Bolognese, Creamy Mushroom Pasta, Baked Potato Wedges, Vegetable Stir-Fry, Chicken and Rice, Tomato Soup, Rice and Beans, Oven-Baked Fish and Chips, No-Bake Oat Bars, Simple Mug Cake, Honey Yoghurt Fruit Bowl, and Chocolate Chip Cookies.

Ingredient names are seeded in their original, everyday spellings (e.g. *Tomatoes*, *eggs*) on purpose — `IngredientNormalizer` folds everything to a canonical form at insert time, which proves the matcher tolerates real-world messiness rather than relying on perfectly clean data.

## Setup & run

**Requirements:** Android Studio (Hedgehog or newer), JDK 17, an emulator or device running Android 7.0 (API 24) or newer.

1. Clone this repository.
2. Open the project folder in Android Studio and let Gradle sync (first sync downloads dependencies — internet required).
3. Select an emulator (or plug in a device with USB debugging) and press **Run**.
4. On first launch the app seeds its 20 recipes automatically; the pantry starts empty — tap **+** to add ingredients.
5. To verify persistence, add items, fully close the app from Recents, and relaunch — the pantry is still there.

**Project details:** `minSdk 24`, `targetSdk 34`, Java 8 source compatibility, Gradle 8.7 + AGP 8.5.2.

## Project layout

```
app/src/main/java/za/ac/richfield/smartpantry/
├── SmartPantryApp.java            – Application subclass; seeds recipes on first run
├── MainActivity.java              – Pantry list (RecyclerView + custom adapter, bottom nav, FAB)
├── AddEditIngredientActivity.java – Create/Update form with validation
├── SuggestedRecipesActivity.java  – Strict-matching results + "Almost There" section
├── RecipeDetailActivity.java      – Ingredients (Have/Missing) + method steps
├── SettingsActivity.java          – Toggles stored in SharedPreferences
├── DatabaseHelper.java            – SQLiteOpenHelper; all CRUD + schema
├── RecipeMatcher.java             – THE strict-matching engine
├── IngredientNormalizer.java      – Plural/synonym/punctuation folding
├── Unit.java                      – Unit families + conversions (g/kg, ml/l, tsp/tbsp, counts)
├── RecipeSeed.java                – The 20-recipe starter collection
├── Recipe.java / PantryItem.java  – Data models
```

## Repository owner

<Student Name> — Mobile App Development 700, Richfield Graduate Institute of Technology. Replace this line with your name and student number before submitting, and update the GitHub link in the written report.
