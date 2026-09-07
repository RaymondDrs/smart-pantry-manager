package za.ac.richfield.smartpantry;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

/**
 * RecipeSeed: the 20-recipe starter collection that ships inside the app
 * and is loaded into SQLite on first run.
 *
 * All ingredient names in this file are stored in their ORIGINAL, real-world
 * spelling (e.g. "Tomatoes", "eggs") on purpose: IngredientNormalizer folds
 * them to a canonical form at insert time, proving the matcher tolerates
 * plural/synonym differences rather than relying on perfectly clean data.
 */
public final class RecipeSeed {

    private RecipeSeed() { /* static data holder */ }

    /** Inserts the starter collection if (and only if) the table is empty. */
    public static void seedIfEmpty(Context ctx) {
        DatabaseHelper db = new DatabaseHelper(ctx);
        if (db.recipesTableEmpty()) {
            for (Recipe r : recipes()) {
                db.insertRecipe(r);
            }
        }
        db.close();
    }

    /** Builds the full collection as Java objects. */
    public static List<Recipe> recipes() {
        List<Recipe> list = new ArrayList<>();
        // -----------------------------------------------------------------
        // BREAKFAST (4)
        // -----------------------------------------------------------------
        list.add(new Recipe(
                "Scrambled Eggs on Toast",
                "Breakfast",
                "1. Beat the eggs with a pinch of salt and pepper.\n"
                        + "2. Melt butter in a non-stick pan over medium heat.\n"
                        + "3. Pour in the eggs and stir gently until just set.\n"
                        + "4. Toast the bread slices and butter them.\n"
                        + "5. Spoon the eggs onto the toast and serve.",
                reqs(
                        "eggs|3|egg",
                        "bread|2|slice",
                        "butter|10|g"
                )));

        list.add(new Recipe(
                "Basic Pancakes",
                "Breakfast",
                "1. Whisk flour, sugar, salt, egg and milk into a smooth batter.\n"
                        + "2. Rest the batter for 5 minutes.\n"
                        + "3. Heat a lightly oiled pan over medium heat.\n"
                        + "4. Pour in a ladle of batter and cook until bubbles form.\n"
                        + "5. Flip and cook the other side until golden.",
                reqs(
                        "flour|200|g",
                        "milk|300|ml",
                        "egg|1|egg",
                        "sugar|15|g",
                        "baking powder|10|g"
                )));

        list.add(new Recipe(
                "Overnight Oats",
                "Breakfast",
                "1. Combine oats and milk in a jar.\n"
                        + "2. Stir in honey and chia seeds.\n"
                        + "3. Seal and refrigerate overnight.\n"
                        + "4. Top with banana slices before eating.",
                reqs(
                        "rolled oats|100|g",
                        "milk|250|ml",
                        "honey|15|ml",
                        "banana|1|pc",
                        "chia seeds|10|g"
                )));

        list.add(new Recipe(
                "French Toast",
                "Breakfast",
                "1. Whisk eggs, milk, sugar and cinnamon together.\n"
                        + "2. Soak each bread slice in the mixture for 20 seconds per side.\n"
                        + "3. Fry in butter over medium heat until golden on both sides.\n"
                        + "4. Dust with sugar and serve warm.",
                reqs(
                        "bread|4|slice",
                        "egg|2|egg",
                        "milk|60|ml",
                        "butter|15|g",
                        "cinnamon|2|g"
                )));
        // -----------------------------------------------------------------
        // LUNCH (4)
        // -----------------------------------------------------------------
        list.add(new Recipe(
                "Classic Tomato Omelette",
                "Lunch",
                "1. Beat the eggs with salt and pepper.\n"
                        + "2. Fry the chopped tomatoes and onion until soft.\n"
                        + "3. Pour the eggs over the vegetables.\n"
                        + "4. Cook over low heat until set.\n"
                        + "5. Fold and serve.",
                reqs(
                        "egg|3|egg",
                        "tomatoes|2|pc",
                        "butter|10|g",
                        "onion|0.5|pc"
                )));

        list.add(new Recipe(
                "Tuna Mayonnaise Sandwich",
                "Lunch",
                "1. Drain the tuna and flake it into a bowl.\n"
                        + "2. Mix with mayonnaise and a grind of black pepper.\n"
                        + "3. Spread over one slice of bread.\n"
                        + "4. Top with the second slice, cut and serve.",
                reqs(
                        "tinned tuna|1|can",
                        "mayonnaise|30|g",
                        "bread|2|slice",
                        "black pepper|1|g"
                )));

        list.add(new Recipe(
                "Chickpea Salad",
                "Lunch",
                "1. Rinse and drain the chickpeas.\n"
                        + "2. Chop the tomato, cucumber and onion into chunks.\n"
                        + "3. Toss everything with olive oil and lemon juice.\n"
                        + "4. Season with salt and pepper; serve immediately.",
                reqs(
                        "chickpeas|400|g",
                        "tomato|1|pc",
                        "cucumber|0.5|pc",
                        "onion|0.5|pc",
                        "olive oil|15|ml",
                        "lemon|0.5|pc"
                )));

        list.add(new Recipe(
                "Cheese Toastie",
                "Lunch",
                "1. Butter the outside of both bread slices.\n"
                        + "2. Layer the cheese between them.\n"
                        + "3. Fry in a dry pan over medium heat, 2-3 minutes per side.\n"
                        + "4. Press down gently while frying for an even melt.\n"
                        + "5. Cut in half and serve hot.",
                reqs(
                        "bread|2|slice",
                        "cheddar cheese|60|g",
                        "butter|10|g"
                )));
        // -----------------------------------------------------------------
        // SUPPER (8)
        // -----------------------------------------------------------------
        list.add(new Recipe(
                "Spaghetti Bolognese",
                "Supper",
                "1. Brown the mince in a hot pan; drain excess fat.\n"
                        + "2. Add diced onion and garlic; cook until soft.\n"
                        + "3. Stir in chopped tomatoes, tomato paste and oregano.\n"
                        + "4. Simmer 20 minutes; season to taste.\n"
                        + "5. Cook the spaghetti per packet instructions.\n"
                        + "6. Serve sauce over pasta; top with cheese.",
                reqs(
                        "spaghetti|250|g",
                        "minced beef|400|g",
                        "tomatoes|400|g",
                        "onion|1|pc",
                        "garlic|2|clove",
                        "tomato paste|2|tbsp",
                        "cheddar cheese|50|g"
                )));

        list.add(new Recipe(
                "Creamy Mushroom Pasta",
                "Supper",
                "1. Fry sliced mushrooms and garlic in butter until golden.\n"
                        + "2. Pour in the cream; simmer 3 minutes.\n"
                        + "3. Season with salt and black pepper.\n"
                        + "4. Toss with cooked pasta and serve.",
                reqs(
                        "pasta|250|g",
                        "mushrooms|250|g",
                        "butter|20|g",
                        "cream|200|ml",
                        "garlic|2|clove"
                )));

        list.add(new Recipe(
                "Baked Potato Wedges",
                "Supper",
                "1. Cut potatoes into wedges (skin on).\n"
                        + "2. Toss with olive oil, salt and paprika.\n"
                        + "3. Spread on a tray in a single layer.\n"
                        + "4. Bake at 200 C for 35 minutes, turning once.",
                reqs(
                        "potatoes|800|g",
                        "olive oil|30|ml",
                        "paprika|5|g",
                        "salt|5|g"
                )));

        list.add(new Recipe(
                "Vegetable Stir-Fry",
                "Supper",
                "1. Heat the oil in a wok until shimmering.\n"
                        + "2. Fry garlic and ginger for 30 seconds.\n"
                        + "3. Add carrot and pepper; stir-fry 3 minutes.\n"
                        + "4. Add cabbage and soy sauce; toss 2 minutes more.\n"
                        + "5. Serve on its own or with rice.",
                reqs(
                        "cabbage|300|g",
                        "carrot|2|pc",
                        "pepper|1|pc",
                        "garlic|3|clove",
                        "ginger|10|g",
                        "soy sauce|30|ml",
                        "cooking oil|30|ml"
                )));

        list.add(new Recipe(
                "Chicken and Rice",
                "Supper",
                "1. Season chicken pieces with salt and paprika.\n"
                        + "2. Brown the chicken in oil, both sides.\n"
                        + "3. Add chopped onion and rice; stir 1 minute.\n"
                        + "4. Pour in stock; cover and simmer 20 minutes.\n"
                        + "5. Rest 5 minutes off the heat before serving.",
                reqs(
                        "chicken pieces|500|g",
                        "rice|250|g",
                        "onion|1|pc",
                        "chicken stock|500|ml",
                        "paprika|5|g",
                        "cooking oil|20|ml"
                )));

        list.add(new Recipe(
                "Tomato Soup",
                "Supper",
                "1. Soften the onion and garlic in butter.\n"
                        + "2. Add chopped tomatoes and stock; simmer 15 minutes.\n"
                        + "3. Blend until smooth (or leave chunky).\n"
                        + "4. Stir in cream; season and serve.",
                reqs(
                        "tomatoes|600|g",
                        "onion|1|pc",
                        "garlic|2|clove",
                        "butter|20|g",
                        "vegetable stock|400|ml",
                        "cream|50|ml"
                )));

        list.add(new Recipe(
                "Rice and Beans",
                "Supper",
                "1. Rinse the rice until the water runs clear.\n"
                        + "2. Cook rice in double its volume of water, covered, 15 minutes.\n"
                        + "3. Separately, fry onion and garlic; add the beans and cumin.\n"
                        + "4. Mash lightly, season, and fold into the rice.",
                reqs(
                        "rice|200|g",
                        "beans|400|g",
                        "onion|1|pc",
                        "garlic|2|clove",
                        "cumin|3|g"
                )));

        list.add(new Recipe(
                "Oven-Baked Fish and Chips",
                "Supper",
                "1. Heat the oven to 220 C.\n"
                        + "2. Toss potato wedges and fish fillets in oil and lemon juice.\n"
                        + "3. Season with salt and pepper.\n"
                        + "4. Bake the chips 25 minutes; add the fish for the last 12.\n"
                        + "5. Serve with lemon wedges.",
                reqs(
                        "fish fillets|400|g",
                        "potatoes|600|g",
                        "olive oil|40|ml",
                        "lemon|0.5|pc",
                        "salt|5|g"
                )));
        // -----------------------------------------------------------------
        // SNACKS & BAKING (4)
        // -----------------------------------------------------------------
        list.add(new Recipe(
                "No-Bake Oat Bars",
                "Snack",
                "1. Melt the peanut butter and honey together.\n"
                        + "2. Stir in the oats until fully coated.\n"
                        + "3. Press into a lined tin and chill 2 hours.\n"
                        + "4. Cut into bars and store in an airtight box.",
                reqs(
                        "rolled oats|250|g",
                        "peanut butter|150|g",
                        "honey|80|ml"
                )));

        list.add(new Recipe(
                "Simple Mug Cake",
                "Snack",
                "1. Mix flour, sugar, cocoa, baking powder and salt in a mug.\n"
                        + "2. Add milk and oil; whisk with a fork until smooth.\n"
                        + "3. Microwave on high for 90 seconds.\n"
                        + "4. Cool 2 minutes before eating.",
                reqs(
                        "flour|40|g",
                        "sugar|40|g",
                        "cocoa powder|15|g",
                        "milk|60|ml",
                        "baking powder|5|g"
                )));

        list.add(new Recipe(
                "Honey Yoghurt Fruit Bowl",
                "Snack",
                "1. Spoon the yoghurt into a bowl.\n"
                        + "2. Slice the banana over it.\n"
                        + "3. Drizzle with honey and sprinkle with chia seeds.",
                reqs(
                        "yoghurt|200|g",
                        "banana|1|pc",
                        "honey|15|ml",
                        "chia seeds|5|g"
                )));

        list.add(new Recipe(
                "Chocolate Chip Cookies",
                "Snack",
                "1. Cream butter and sugar until pale.\n"
                        + "2. Beat in egg and vanilla.\n"
                        + "3. Fold in flour, bicarbonate of soda and chocolate chips.\n"
                        + "4. Roll into balls; bake at 180 C for 10-12 minutes.",
                reqs(
                        "flour|225|g",
                        "butter|150|g",
                        "sugar|150|g",
                        "egg|1|egg",
                        "chocolate chips|100|g",
                        "vanilla extract|5|ml"
                )));
        return list;
    }

    /** Parses "name|qty|unit" strings into Requirement objects. */
    private static List<Recipe.Requirement> reqs(String... lines) {
        List<Recipe.Requirement> out = new ArrayList<>();
        for (String line : lines) {
            String[] parts = line.split("\\|");
            String original = parts[0].trim();
            double qty = Double.parseDouble(parts[1].trim());
            String unit = parts[2].trim();
            out.add(new Recipe.Requirement(
                    IngredientNormalizer.normalize(original), qty, unit, original));
        }
        return out;
    }
}
