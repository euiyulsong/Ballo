package edu.uw.nzkwgo.ballo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class StatsActivity extends AppCompatActivity implements Ballo.Events {
    private TextView age;
    private TextView bounce;
    private TextView distance;
    private TextView fed;
    private TextView lowestHappiness;
    private TextView highestStrength;
    private TextView lowestHunger;
    private TextView lowestStrength;
    private TextView deathMessage;
    private Button returnBtn;
    private Button leaderboardBtn;

    private Ballo ballo;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ballo.destroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        // Get Ui Views
        age = (TextView) findViewById(R.id.stats_age);
        bounce = (TextView) findViewById(R.id.stats_bounce);
        distance = (TextView) findViewById(R.id.stats_distance);
        fed = (TextView) findViewById(R.id.stats_fed);
        lowestHappiness = (TextView) findViewById(R.id.stats_lowest_happiness);
        highestStrength = (TextView) findViewById(R.id.stats_highest_strength);
        lowestHunger = (TextView) findViewById(R.id.stats_lowest_hunger);
        lowestStrength = (TextView) findViewById(R.id.stats_lowest_strength);
        deathMessage = (TextView) findViewById(R.id.deathMessage);
        returnBtn = (Button) findViewById(R.id.returnBtn);
        leaderboardBtn = (Button) findViewById(R.id.leaderboardBtn);


        ballo = Ballo.getBallo(this);

        age.setText("" + ballo.getAge());
        bounce.setText("" + ballo.getTimesBounced());
        distance.setText("" + ballo.getDistanceWalked());
        fed.setText("" + ballo.getTimesFed());
        lowestHappiness.setText("" + ballo.getLowestHappiness());
        highestStrength.setText("" + ballo.getHighestStrength());
        lowestHunger.setText("" + ballo.getLowestHunger());
        lowestStrength.setText("" + ballo.getLowestStrength());

        if (ballo.isDead()) {
            deathMessage.setText(ballo.getStatusText());
            returnBtn.setText("Restart");
            returnBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Ballo newBallo = new Ballo();
                    Ballo.saveBallo(getApplicationContext(), newBallo);
                    startActivity(new Intent(StatsActivity.this, HomeActivity.class));
                }
            });
        } else {
            returnBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(StatsActivity.this, HomeActivity.class));
                }
            });
        }

        leaderboardBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(StatsActivity.this, LeaderboardActivity.class));
            }
        });
    }

    @Override
    public void onUpdate() {
        if (ballo == null) {
            return;
        }

        Ballo.saveBallo(this, ballo);
    }
}
