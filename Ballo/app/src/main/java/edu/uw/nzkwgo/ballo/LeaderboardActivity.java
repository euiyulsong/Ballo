package edu.uw.nzkwgo.ballo;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import edu.uw.nzkwgo.ballo.leaderboard.LeaderboardListAdapter;
import edu.uw.nzkwgo.ballo.leaderboard.LeaderboardUtil;

public class LeaderboardActivity extends AppCompatActivity {
    private static class LeaderboardListAdapterWrapper {
        @Nullable
        public LeaderboardListAdapter adapter;
    }

    private final LeaderboardListAdapterWrapper adapterWrapper;
    private Button backBtn;
    private Ballo ballo;

    public LeaderboardActivity() {
        adapterWrapper = new LeaderboardListAdapterWrapper();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        final ArrayList<LeaderboardUtil.LeaderboardEntry> data = new ArrayList<>();

        ListView list = (ListView) findViewById(R.id.leaderboardList);
        synchronized (adapterWrapper) {
            adapterWrapper.adapter =
                    new LeaderboardListAdapter(this, R.layout.leaderboard_element, data);
            list.setAdapter(adapterWrapper.adapter);
        }

        LeaderboardUtil.getLeaderboardEntries(new LeaderboardUtil.LeaderboardFetchCallback() {
            @Override
            public void onFetch(LeaderboardUtil.LeaderboardEntry[] entries,
                    @Nullable String error) {
                if (error != null) {
                    Toast.makeText(LeaderboardActivity.this,
                            "Couldn't load the leaderboard: " + error, Toast.LENGTH_SHORT).show();
                    return;
                }

                for (LeaderboardUtil.LeaderboardEntry entry : entries) {
                    data.add(entry);
                }
                notifyDataSetChanged();
            }
        });

        backBtn = (Button) findViewById(R.id.leadBackBtn);
        ballo = Ballo.getBallo(this);

        if (ballo.isDead()) {
            backBtn.setText("Stats");
            backBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(LeaderboardActivity.this, StatsActivity.class));
                }
            });
        } else {
            backBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(LeaderboardActivity.this, HomeActivity.class));
                }
            });
        }

    }

    private void notifyDataSetChanged() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                synchronized (adapterWrapper) {
                    if (adapterWrapper.adapter != null) {
                        adapterWrapper.adapter.notifyDataSetChanged();
                    }
                }
            }
        });
    }
}
