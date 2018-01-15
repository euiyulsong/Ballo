package edu.uw.nzkwgo.ballo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;
import java.util.Timer;
import java.util.TimerTask;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;



/**
 * The home screen of the Ballo app/game. This screen shows a player their current Ballo's hunger,
 * happiness, and strength. The user may choose to exercise, feed, or play with the Ballo, or visit
 * the ballo's stats or leaderboard.
 */
public class HomeActivity extends AppCompatActivity implements Ballo.Events {
    private Ballo ballo;
    private TextView name;
    private ProgressBar hunger;
    private ProgressBar happiness;
    private TextView strength;
    private ImageView balloAvatar;
    private TextView status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Get UI views
        name = (TextView) findViewById(R.id.homeName);
        hunger = (ProgressBar) findViewById(R.id.hungerVal);
        happiness = (ProgressBar) findViewById(R.id.happinessVal);
        strength = (TextView) findViewById(R.id.strengthVal);
        balloAvatar = (ImageView) findViewById(R.id.ballo);
        status = (TextView) findViewById(R.id.status);


        // Set button onclick actions
        findViewById(R.id.feedBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: Implement feed action
                ballo.feed();
                ProgressBar hungerBar = (ProgressBar)findViewById(R.id.hungerVal);
                hungerBar.setProgress(ballo.getHunger());
            }
        });
        findViewById(R.id.walkBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(HomeActivity.this, WalkActivity.class));
            }
        });
        findViewById(R.id.playBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(HomeActivity.this, PlayActivity.class));
            }
        });

        // Don't re-instantiate if ballo already exists (can occur if the screen orientation
        // changes, the phone is put to sleep, etc).
        if (ballo == null) {
            ballo = Ballo.getBallo(this);
        }

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        String text = settings.getString("listpreference_level", "default");
        String name = settings.getString("name_pref", "default");
        ballo.setName(name);

        if (text.equals("1")) {
            ballo.HAPPINESS_DECAY_PER_HOUR = 4;
            ballo.HUNGER_DECAY_PER_HOUR = 2;
            ballo.STRENGTH_DECAY_PER_HOUR = 1;
        } else if (text.equals("2")) {
            ballo.HAPPINESS_DECAY_PER_HOUR = 8;
            ballo.HUNGER_DECAY_PER_HOUR = 4;
            ballo.STRENGTH_DECAY_PER_HOUR = 2;
        } else {
            ballo.HAPPINESS_DECAY_PER_HOUR = 16;
            ballo.HUNGER_DECAY_PER_HOUR = 8;
            ballo.STRENGTH_DECAY_PER_HOUR = 4;
        }
        ballo.saveBallo(this, ballo);

        // Load ballo if one exists
        ballo.setEventHandler(this);
        onUpdate();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ballo.clearEventHandler();
        ballo.destroy();
        ballo = null;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_stats: {
                Toast.makeText(this, "Stats", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(HomeActivity.this, StatsActivity.class));
                return true;
            }
            case R.id.item_leader_board: {
                Toast.makeText(this, "Leaderboard", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(HomeActivity.this, LeaderboardActivity.class));
                return true;
            }
            case R.id.item_settings: {
                Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(HomeActivity.this, SettingActivity.class));
                return true;
            }
            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate menu resource file.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }
    // Updates the screen to reflect ballo's current state.

    @Override
    public void onUpdate() {
        if (ballo == null) {
            return;
        }

        if (ballo.isDead()) {
            startActivity(new Intent(HomeActivity.this, StatsActivity.class));
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                hunger.setProgress(ballo.getHunger());
                happiness.setProgress(ballo.getHappiness());
                strength.setText(String.valueOf(ballo.getStrength()));
                name.setText(ballo.getName());
                status.setText(ballo.getStatusText());
                balloAvatar.setImageResource(getResources()
                        .getIdentifier(ballo.getImgURL(), "drawable", getPackageName()));
            }
        });
        Ballo.saveBallo(this, ballo);
    }
}
