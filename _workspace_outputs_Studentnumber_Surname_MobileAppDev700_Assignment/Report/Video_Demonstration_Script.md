# Smart Pantry Manager — Video Demonstration Script
**Module:** Mobile App Development 700 | **Length:** ~6 minutes | **Format:** screen recording with narrated voice-over

> **Every fact in this script was verified against the real matching engine** (see
> verification note at the end). The demo pantry below produces exactly the results
> narrated: 4 strict suggestions out of 20 recipes, with Basic Pancakes excluded from
> the strict list and shown in Almost There as missing only baking powder.

---

## Recording Setup (before you press record)

1. **Install the app** on your phone or an Android Virtual Device: open the cloned repo in Android Studio and press Run (the README has the full setup steps). Do a **fresh install** (uninstall first) so the pantry starts empty.
2. **Prepare the demo pantry** — you will type these 14 rows during the video (same pantry as Figures 4, 7 and 8 of the report, so the video matches the report). Keep this list on a second screen or printed:
   - Tomatoes — 3 — piece
   - Eggs — 6 — egg
   - Bread — 6 — slice
   - Butter — 0.5 — kg
   - Flour — 0.5 — kg
   - Milk — 1 — l
   - Sugar — 250 — g
   - Cheddar Cheese — 200 — g
   - Tinned Tuna — 1 — can
   - Mayonnaise — 200 — g
   - Black Pepper — 1 — g
   - Oats — 500 — g
   - Peanut Butter — 200 — g
   - Honey — 250 — ml
3. **What you will see with this pantry** (verified):
   - Strict list (4 of 20): **Cheese Toastie, No-Bake Oat Bars, Scrambled Eggs on Toast, Tuna Mayonnaise Sandwich**
   - Almost There (3): **Basic Pancakes** (missing only *baking powder*), **French Toast** (missing only *cinnamon*), **Classic Tomato Omelette** (missing only *half an onion*)
4. **Recording tools**: OBS Studio (free) or the phone/emulator's built-in screen recorder. Record at 1920×1080 minimum. Narrate **during** recording or add voice-over afterwards — either is fine; the marking guide wants clear narration.
5. Rehearse once: the whole flow takes ~6 minutes at a calm pace.

---

## SEGMENT 1 — GitHub Repository Walkthrough (0:00 – 1:00, ~1 minute)

> **On screen:** browser open at your GitHub repository URL.

**[0:00]** "Hello. This is the video demonstration for Smart Pantry Manager, my submission for Mobile App Development 700. In the next six minutes I'll walk you through the GitHub repository, a live demonstration of the app, and then explain the core concepts and my database choice."

**[0:10]** "First, the repository. Everything you see is on GitHub — this is the project home page." *(Point at README rendered on repo front page.)* "The README documents what the app does, why I chose SQLite, and the exact setup steps — clone, open in Android Studio, run."

**[0:20]** "Here is the commit history." *(Click "commits" — show the list.)* "I committed incrementally as I built: scaffolding first, then the database layer, the models, the matching engine, each screen one commit at a time, and bug fixes as separate commits with messages explaining what went wrong. You can read the project as a story from bottom to top."

**[0:35]** *(Scroll the commit list slowly so counts and messages are visible.)* "Each commit is meaningful — no 'update', no 'stuff' — the messages say what changed and why. The source structure is standard Android: one package with the five activities, the database helper, the matcher, the normaliser, and the unit conversion class."

**[0:48]** *(Open `app/src/main/java` folder tree.)* "Java throughout, no Kotlin, and the full source is exactly what you have in the submission ZIP. That's the repository — now let me show you the app running."

---

## SEGMENT 2 — Live App Demonstration (1:00 – 3:40, ~2 min 40 s)

> **On screen:** app fresh-installed, Pantry List screen visible (empty).

**[1:00]** "This is Smart Pantry Manager, freshly installed — the pantry starts empty, and the app invites me to add my first ingredient. Before I do, notice the bottom navigation: Pantry, Suggested, Settings — it's on every screen."

**[1:10]** *(Tap FAB → Add Ingredient form.)* "Adding is through this form. Name, quantity, and unit — the unit list covers grams, kilograms, millilitres, litres, pieces, eggs, slices, cloves, cans, bunches, and more. Expiry date is optional."

**[1:22]** *(Type the 14 demo rows. Type name → qty → pick unit → Save for each. Talk over it; you can speed up or note "I'll enter the rest off-camera".)* "I'm entering my actual stock — fourteen rows, and I'm being deliberately awkward about it. **Tomatoes**, plural, capitalised. **Eggs**. **Bread** in slices. **Butter — nought point five kilograms**. **Flour — half a kilo**. **Milk — one litre**. **Sugar** and **cheddar cheese** in grams. **Tinned tuna** in a can. **Mayonnaise**. **Black pepper**. **Oats**. **Peanut butter**. **Honey — 250 millilitres**. Watch what the app does with these."

**[1:50]** *(Back on Pantry List — point at rows.)* "The list shows my fourteen rows stored in SQLite, displayed through a RecyclerView with a custom adapter. Notice the app accepted my plural 'Tomatoes' and 'Eggs' spellings and my kilogram and litre quantities without complaint."

**[1:58]** *(Tap any row → Edit form.)* "Tapping a row opens it for editing — same form, values pre-filled, and now a Delete button appears. This is the update side of CRUD, and the delete side is right here behind a confirmation dialog."

**[2:08]** *(Demonstrate validation: try saving empty name → error appears; try "tomatoes" again → duplicate error.)* "Validation is inline: an empty name, a **duplicate** — notice it caught 'tomatoes' as a duplicate of the row I already have, because the normaliser folded the plural — and a non-positive quantity each get an error under the field. Nothing invalid reaches the database."

**[2:25]** *(Fix/re-save, then navigate: Suggested tab.)* "Now the interesting part — suggestions."

**[2:30]** *(Point at the count "You can make 4 of 20 recipes right now".)* "Four recipes out of twenty — **Cheese Toastie, No-Bake Oat Bars, Scrambled Eggs on Toast and Tuna Mayonnaise Sandwich**. And here's the proof the matching is **strict**: **Basic Pancakes** needs five ingredients — flour, milk, eggs, sugar, baking powder — and my pantry has **four** of the five. But it is **not** in the suggestions list. It's down here in **Almost There**, telling me exactly what I'm missing: baking powder. That section is the bonus feature — it can be turned off in Settings — and it never leaks into the main list, because the main list is populated only from recipes whose failure lists are empty."

**[2:52]** *(Tap "Scrambled Eggs on Toast" → detail screen.)* "Tapping a recipe shows its detail: the green banner says I have everything, each ingredient shows the amount needed with a **Have** flag — **3 eggs**, **2 slices bread**, **10 grams of butter** — and note: I typed butter as **half a kilogram** and the recipe asks for **10 grams**; the matcher converted the units before comparing. Below, the five method steps."

**[3:18]** *(Back to Pantry; kill the app completely — swipe from recents or Stop in Android Studio; relaunch.)* "One more proof — persistence. I've just killed the process entirely and relaunched. The pantry is **exactly** as I left it — the rows are in the app's private SQLite file on the device, not in memory. That's real database persistence, not a session store."

**[3:32]** *(Quick cut to Settings tab.)* "And Settings — three toggles: the expiring-soon badge, the units preference, and the Almost There section. These persist too, in SharedPreferences."

---

## SEGMENT 3 — Concept Explanation (3:40 – 5:30, ~1 min 50 s)

> **On screen:** RecipeMatcher.java in Android Studio.

**[3:40]** "Now the concepts. The heart of the app is the **strict-matching rule**: a recipe is suggested **only if every single ingredient** it requires is present in the pantry **in at least the required quantity**. Not four out of five — five out of five, or nothing. Here's how it works."

**[3:58]** *(Open RecipeMatcher.java, scroll to matchOne.)* "The matcher first indexes the pantry into a **HashMap** by canonical ingredient name — so every recipe lookup is constant time. Then for each recipe it walks the requirement list: find the pantry entry; if it's absent, that's a failure; if it's present but short on quantity, that's also a failure. One failure anywhere excludes the recipe from the strict list. The verdict object records *why* it failed — missing versus insufficient — and that same verdict powers the detail screen flags and the Almost There section, so the rule has exactly one implementation."

**[4:20]** *(Open IngredientNormalizer.java.)* "Naive string matching would fail on real users, so names are normalised before storage: trim, lowercase, fold whitespace, **plural rules** — tomatoes becomes tomato, cherries becomes cherry — and a **synonym table**: spaghetti and penne both fold to pasta, garbanzo beans become chickpea, rolled oats become oat. Both sides of every comparison are canonical, so the lookup is an exact-match on clean keys."

**[4:42]** *(Open Unit.java.)* "Units get the same treatment one level up. Every unit belongs to a family — **mass**, **volume**, or **count** — and carries a factor to its family base unit: grams, millilitres, pieces. So my half-kilo of butter versus a recipe's 10 grams is one multiply-and-divide through the base. A tiny epsilon absorbs floating-point noise. And when the two sides are in different families — my three tomatoes by count versus a recipe's 200 grams of tomato — the requirement is satisfied if the pantry quantity is positive: a documented, pragmatic judgement call, and the only softening of the strict rule."

**[5:08]** "Everything is **Java**, using explicit **Intents** with extras for navigation — the item id when editing, the recipe id when opening a detail — and every screen refreshes its data in **onResume()**, so coming back from a save always shows fresh data. The pantry list itself is a **RecyclerView** with a custom adapter bound to live SQLite rows. No maps, no location features, anywhere."

---

## SEGMENT 4 — Database Justification (5:30 – 6:00, ~30 seconds)

> **On screen:** DatabaseHelper.java.

**[5:30]** "Finally, the database. I chose **SQLite**, for three reasons. One: the data is entirely on-device — recipes and pantry rows — and offline-first, which fits an app used in a kitchen with patchy signal. Two: it's **relational** — recipes connect to their ingredient lines in a proper one-to-many relationship, and the matcher joins them — which a key-value store can't do naturally. Three: it's built into Android via SQLiteOpenHelper, so there's zero configuration for anyone who clones the repo — clone, run, and it works, with no Firebase project or cloud account to provision. The persistence proof you saw earlier — kill and relaunch, data intact — is the database doing its job. Thanks for watching."

**[6:00]** *(END — fade out.)*

---

## Delivery Checklist (after recording)

- [ ] Narration is clear and audible (record in a quiet room; do a 10-second mic test first)
- [ ] All 5 screens shown (Pantry, Add/Edit, Suggested, Recipe Detail, Settings)
- [ ] Full CRUD shown (add ✓, read ✓, update ✓, delete — mention or demo deleting one row)
- [ ] Strict-match proof shown explicitly (Basic Pancakes excluded + Almost There)
- [ ] Persistence proof shown (kill app → relaunch → data intact)
- [ ] Unit conversion proof shown (0.5 kg vs 10 g butter)
- [ ] GitHub commits list shown and scrolled
- [ ] Total length between 5 and 7 minutes (check with a watch — trim silence at start/end)

---

## Verification Note (internal — do not read aloud)

The claims in this script were machine-verified against the actual matching engine
strict suggestions = 4 of 20 (Cheese Toastie, No-Bake Oat Bars, Scrambled Eggs on Toast,
Tuna Mayonnaise Sandwich); Almost There = 3 (Basic Pancakes missing only 10 g baking
powder, French Toast missing only 2 g cinnamon, Classic Tomato Omelette missing only
half an onion). The Scrambled Eggs on Toast detail screen shows "3 eggs", "2 slices
bread", "10 g butter" with the green everything-available banner. Any change to the
demo pantry rows or to the seed recipes invalidates these claims — re-verify before
recording.
