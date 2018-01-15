package edu.uw.nzkwgo.ballo.leaderboard;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import edu.uw.nzkwgo.ballo.R;

public class LeaderboardListAdapter extends ArrayAdapter<LeaderboardUtil.LeaderboardEntry> {
    private static class ViewHolder {
        TextView name;
        TextView score;
    }

    public LeaderboardListAdapter(@NonNull Context context,
            @LayoutRes int resource,
            @NonNull List<LeaderboardUtil.LeaderboardEntry> objects) {
        super(context, resource, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // Get data item
        LeaderboardUtil.LeaderboardEntry data = getItem(position);
        if (data == null) {
            throw new ArrayIndexOutOfBoundsException();
        }

        // Are we reusing?
        if (convertView == null) {
            // Inflate
            convertView = LayoutInflater
                    .from(getContext())
                    .inflate(R.layout.leaderboard_element, parent, false);

            // Use viewholder pattern to cache references to views
            ViewHolder v = new ViewHolder();
            v.name = (TextView) convertView.findViewById(R.id.leaderboardElementName);
            v.score = (TextView) convertView.findViewById(R.id.leaderboardElementScore);
            convertView.setTag(v);
        }

        // Update views
        ViewHolder v = (ViewHolder) convertView.getTag();
        v.name.setText(data.name);
        v.score.setText(NumberFormat.getNumberInstance(Locale.US).format(data.score));

        return convertView;
    }
}
