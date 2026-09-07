package za.ac.richfield.smartpantry;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.materialswitch.MaterialSwitch;

/**
 * Settings screen: expiring-soon alert toggle, units preference and an
 * about blurb. Settings live in SharedPreferences (lightweight key/value
 * pairs) while the pantry and recipes live in SQLite - a deliberate split:
 * structured relational data belongs in a database, not in preferences.
 */
public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);

        MaterialSwitch swExpiry = findViewById(R.id.swExpiryAlerts);
        boolean alertsOn = prefs.getBoolean("expiry_alerts", true);
        swExpiry.setChecked(alertsOn);
        updateExpirySummary(swExpiry, alertsOn);

        swExpiry.setOnCheckedChangeListener((btn, checked) -> {
            prefs.edit().putBoolean("expiry_alerts", checked).apply();
            updateExpirySummary(btn, checked);
        });

        MaterialSwitch swUnits = findViewById(R.id.swUnits);
        boolean mlPreferred = prefs.getBoolean("prefer_ml", false);
        swUnits.setChecked(mlPreferred);
        swUnits.setOnCheckedChangeListener((btn, checked) ->
                prefs.edit().putBoolean("prefer_ml", checked).apply());

        MaterialSwitch swAlmost = findViewById(R.id.swAlmostThere);
        boolean almostOn = prefs.getBoolean("almost_there", true);
        swAlmost.setChecked(almostOn);
        swAlmost.setOnCheckedChangeListener((btn, checked) ->
                prefs.edit().putBoolean("almost_there", checked).apply());

        setupNav();
    }

    private void updateExpirySummary(MaterialSwitch btn, boolean checked) {
        btn.setText(checked ? R.string.settings_expiry_summary_on
                : R.string.settings_expiry_summary_off);
    }

    private void setupNav() {
        BottomNavigationView nav = findViewById(R.id.bottomNav);
        nav.setSelectedItemId(R.id.nav_settings);
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_settings) {
                return true;
            } else if (id == R.id.nav_pantry) {
                startActivity(new Intent(this, MainActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_suggested) {
                startActivity(new Intent(this, SuggestedRecipesActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });
    }
}
