package edu.uw.nzkwgo.ballo.leaderboard;

import com.google.gson.Gson;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.annotation.Nullable;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

import edu.uw.nzkwgo.ballo.R;

/**
 * Contains methods that interact with the leaderboard server.
 */
public class LeaderboardUtil {
    /**
     * Should be passed to #getLeaderboardEntries as the method is asynchronous.
     */
    public interface LeaderboardFetchCallback {
        /**
         * Called when the fetch completes.
         * @param entries The list of entries on the leaderboard ordered by highest score first.
         * @param error Set non-null if an error occured.
         */
        void onFetch(LeaderboardEntry[] entries, @Nullable String error);
    }

    /**
     * A simple java object (POJO) that contains the results of a leaderboard fetch.
     */
    public static class LeaderboardEntry {
        public String name;
        public int score;
    }

    /**
     * Used to send data to the leaderboard server.
     */
    private static class PostEntry extends LeaderboardEntry {
        public String deviceID;
    }

    private static final String LEADERBOARD_URL = "http://android.fru1t.me/";
    private static final String LEADERBOARD_DEVICE_ID_PREFERENCE_KEY = "leaderboard-device-id";
    private static Gson gson;

    /**
     * Stores the given score for the current player in an asynchronous fashion. The display name
     * can be non-unique and isn't used for identification purposes on the client or server-end.
     * @param displayName A name to display on the leaderboard. The max  length has no limit except
     * what we decide to set it at.
     * @param score The score metric to store.
     */
    public static void storeScore(Context ctx, String displayName, int score) {
        // Grab the device ID, or create one and store it if one doesn't exist already.
        SharedPreferences p = ctx.getSharedPreferences(
                ctx.getString(R.string.leaderboard_preferences_file_key), Context.MODE_PRIVATE);
        String deviceId =
                p.getString(LEADERBOARD_DEVICE_ID_PREFERENCE_KEY, UUID.randomUUID().toString());
        p.edit().putString(LEADERBOARD_DEVICE_ID_PREFERENCE_KEY, deviceId).apply();

        // Create new score entry
        PostEntry e = new PostEntry();
        e.deviceID = deviceId;
        e.name = displayName;
        e.score = score;

        // Json marshall
        final String jsonString = getGson().toJson(e);

        // Submit score
        new AsyncTask<Object, Object, Object>() {
            @Override
            protected Object doInBackground(Object... objects) {
                HttpURLConnection client = null;
                try {
                    URL url = new URL(LEADERBOARD_URL);
                    client = (HttpURLConnection) url.openConnection();
                    client.setRequestMethod("POST");
                    client.setRequestProperty("Content-Type", "application/json");

                    OutputStream os = client.getOutputStream();
                    os.write(jsonString.getBytes());
                    os.close();

                    client.connect();

                    int status = client.getResponseCode();
                    if (status == HttpURLConnection.HTTP_OK) {
                        System.out.println("Successfully posted to leaderboards");
                    } else {
                        System.out.println("Couldn't post to leaderboards. Got status " + status);
                    }
                } catch (Exception e) {
                    System.out.println("Couldn't post to leaderboard.");
                    e.printStackTrace();
                } finally {
                    if (client != null) { client.disconnect(); }
                }

                return null;
            }
        }.execute("");
    }

    /**
     * Asynchronously polls the server to fetch the leaderboard.
     */
    public static void getLeaderboardEntries(final LeaderboardFetchCallback callback) {
        new AsyncTask<Object, Object, Object>() {
            @Override
            protected Object doInBackground(Object... objects) {
                HttpURLConnection client = null;
                try {
                    // Connect
                    URL url = new URL(LEADERBOARD_URL);
                    client = (HttpURLConnection) url.openConnection();
                    client.setRequestMethod("GET");

                    client.connect();

                    int status = client.getResponseCode();
                    if (status == HttpURLConnection.HTTP_OK) {
                        // Parse response
                        System.out.println("Successfully fetched leaderboards");
                        BufferedReader reader = new BufferedReader(
                                new InputStreamReader(client.getInputStream()));
                        StringBuilder sb = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            sb.append(line).append("\n");
                        }
                        reader.close();

                        // Process as json
                        LeaderboardEntry[] entries = getGson().fromJson(sb.toString(),
                                LeaderboardEntry[].class);

                        // Callback
                        callback.onFetch(entries, null);
                        // Uh oh
                    } else {
                        System.out.println("Couldn't fetch leaderboards. Got status " + status);
                        callback.onFetch(new LeaderboardEntry[]{},
                                "Couldn't fetch leaderboards: " + status);
                    }
                } catch (Exception e) {
                    // Uh oh x2
                    System.out.println("Couldn't GET leaderboard.");
                    e.printStackTrace();
                    callback.onFetch(new LeaderboardEntry[]{}, "??");
                } finally {
                    if (client != null) { client.disconnect(); }
                }

                return null;
            }
        }.execute("");
    }

    private static Gson getGson() {
        if (gson == null) {
            gson = new Gson();
        }
        return gson;
    }
}
