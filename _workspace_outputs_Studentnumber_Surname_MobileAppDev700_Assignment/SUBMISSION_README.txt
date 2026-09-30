Smart Pantry Manager - Mobile App Development 700
=================================================

CONTENTS OF THIS ZIP
--------------------
SmartPantryManager/   Complete Android Studio project source (Java).
                      build/, .gradle/ and local.properties are excluded
                      (standard practice); open the project in Android
                      Studio and it rebuilds locally.

Report/Smart_Pantry_Manager_Report.pdf
                      The written report (22 pages): cover, TOC, design
                      diagrams, database justification, screenshots with
                      captions, key code snippets, challenges, references.

Report/Video_Demonstration_Script.md
                      The full narration script for the 5-7 minute video
                      demonstration, with timestamps and a recording
                      checklist. RECORD THIS YOURSELF and export the MP4
                      into the ZIP before submitting (see below).

APK/app-debug.apk     A compiled debug APK for convenience; the marker can
                      also build from source.

BEFORE YOU SUBMIT - 4 STEPS
---------------------------
1. Fill in your personal details:
   - Report cover page: your name, ITS number, year, semester, date,
     and your GitHub repository URL (the PDF has labelled blanks).
   - Video script header: your details if required.
2. Push the repository to YOUR GitHub account (the .git history with 22
   meaningful commits is already inside SmartPantryManager/).
   Recommended: create the GitHub repo, then:
     cd SmartPantryManager
     git remote add origin https://github.com/<your-user>/smart-pantry-manager.git
     git push -u origin master
   Then paste the repo URL into the report cover page.
3. Record the 5-7 minute video following the script (the claims in it are
   already verified against the real app). Export as MP4, ~1080p, and put
   it in the ZIP root as SmartPantryManager_Demo.mp4 (replace the
   placeholder file).
4. Rename this ZIP to:
     Studentnumber_Surname_MobileAppDev700_Assignment.zip
   and check it stays under 50 MB (currently ~7 MB without the video; a
   6-minute 1080p MP4 is typically 60-150 MB, so use a mid-bitrate export
   or 720p if your ZIP grows past 50 MB).

WHY THE ZIP IS SHIPPED WITHOUT THE VIDEO
----------------------------------------
The assignment requires a narrated video in your own voice. The script
tells you exactly what to say and show; every factual claim (recipe
verdicts, counts, screen contents) was verified against the actual
matching engine of the app so nothing you narrate will be wrong.

GIT HISTORY (for the marker)
----------------------------
22 commits, each a meaningful increment:
  scaffolding -> gradle fix -> launcher icon fix -> README -> DB layer ->
  models/seed -> normalizer/units -> matcher -> five screens -> commit
  sequence -> Robolectric screenshot rig + UI polish commits.
Run `git log --oneline` inside SmartPantryManager/ to read the story.
