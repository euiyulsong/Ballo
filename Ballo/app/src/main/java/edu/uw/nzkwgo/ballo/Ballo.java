package edu.uw.nzkwgo.ballo;

import com.google.gson.Gson;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import edu.uw.nzkwgo.ballo.leaderboard.LeaderboardUtil;

/**
 * The pet.
 */
public class Ballo  {
    // Events Ballo may produce.
    public interface Events {
        // Triggers when Ballo has been updated in any way (image, happiness, hunger, strength, etc)
        void onUpdate();
    }

    private static final String BASIC_BALLO = "basic_ballo";
    private static final String UNHEALTHY_BALLO = "unhealthy_ballo";
    private static final String SAD_BALLO = "sad_ballo";
    private static final String HUNGRY_BALLO = "hungry_ballo";
    private static final String DEAD_BALLO = "dead_ballo";

    public double HAPPINESS_DECAY_PER_HOUR = 4;
    public double HUNGER_DECAY_PER_HOUR = 2;
    public double STRENGTH_DECAY_PER_HOUR = 1;

    private static final String BALLO_PREFERENCE_STATE_ID = "ballo-state-pref";
    private static final String BALLO_OBJECT_ID = "ballo";

    private static final long DECAY_TIMER_MS = 1000 * 60 * 5; // 5 minutes

    private static Gson gson;


    /**
     * @param ctx Pass the current activity
     * @return The user's current ballo
     */
    public static Ballo getBallo(Context ctx) {
        Ballo result = new Ballo();
        SharedPreferences pref =
                ctx.getSharedPreferences(BALLO_PREFERENCE_STATE_ID, Context.MODE_PRIVATE);
        String balloJson = pref.getString(BALLO_OBJECT_ID, "");

        if (balloJson.length() != 0) {
            try {
                result = getGson().fromJson(balloJson, Ballo.class);
            } catch (Exception e) {
                Toast.makeText(ctx, "Couldn't load your ballo :(", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
            System.out.println("Loaded ballo from storage");
        } else {
            System.out.println("A new ballo was created");
        }

        return result;
    }

    /**
     * Stores the given ballo if it's older than the previous.
     * @param ctx Pass the current activity
     * @param ballo Give me your ballo.
     */
    public static void saveBallo(Context ctx, Ballo ballo) {
        SharedPreferences pref =
                ctx.getSharedPreferences(BALLO_PREFERENCE_STATE_ID, Context.MODE_PRIVATE);
        pref.edit().putString(BALLO_OBJECT_ID, getGson().toJson(ballo)).apply();
        LeaderboardUtil.storeScore(ctx, ballo.getName(), ballo.getHighestStrength());
    }

    private static Gson getGson() {
        if (gson == null) {
            gson = new Gson();
        }
        return gson;
    }

    private String name;
    private double hunger;
    private double happiness;
    private double strength;
    private final Date birthdate;
    private double distanceWalked;
    private int timesFed;
    private int timesBounced;
    private double lowestHappiness;
    private double lowestStrength;
    private double lowestHunger;
    private double highestStrength;
    private String statusText;
    private String imgURL;
    private long lastDecayUpdateTime;
    public String difficulty;

    private transient Events eventHandler;
    private transient Timer decayTimer;

    //Bouncing Animation Variables
    public float cy;

    public Ballo(String name) {
        this.name = name;

        hunger = 100;
        happiness = 100;
        strength = 100;
        birthdate = new Date();
        distanceWalked = 0;
        timesFed = 0;
        timesBounced = 0;
        lowestHappiness = 100;
        lowestStrength = 100;
        lowestHunger = 100;
        highestStrength = 100;
        statusText = "Ballo is doing good! Ballo loves you.";
        imgURL = BASIC_BALLO;
        lastDecayUpdateTime = (new Date()).getTime();

        decayTimer = new Timer();
        TimerTask decayTask = new TimerTask() {
            @Override
            public void run() {
                // Decay
                decay();
            }
        };
        decayTimer.schedule(decayTask, 0, DECAY_TIMER_MS);

    }

    public Ballo() {
        this("Ballo");
    }

    public void feed() {
        if (hunger < 100) {
            setHunger(hunger + 10);
        } else {
            setHappiness(happiness - 5);
        }
        timesFed++;
    }

    public void bounce() {
        if (happiness < 100) {
            setHappiness(happiness + 2);
        } else {
            setStrength(strength - 1);
        }
        timesBounced++;
        //updateImg();
    }

    //Changes Ballo's stats based on a quarter mile's worth of walking.
    //Should be called after walking a quarter mile
    public void walk(double distance) {
        if (Double.isNaN(distance)) {
            return;
        }

        Log.v("BALLO", "walk method: " + distance);
        setStrength(strength + distance / 5);
        // easy: divide by 2
        // medium: divide by 10
        // hard: divide by 20
        setHunger(hunger - (distance/10));
        distanceWalked += distance;
    }

    /**
     * Decays the ballo's stats if the ballo isn't dead. This method should be called periodically.
     */
    public void decay() {
        if (isDead()) {
            return;
        }

        long currentTime = (new Date()).getTime();
        double elapsedHours = 1.0 * (currentTime - lastDecayUpdateTime) / 1000 / 60 / 60;

        setHappiness(happiness - (elapsedHours * HAPPINESS_DECAY_PER_HOUR));
        setHunger(hunger - (elapsedHours * HUNGER_DECAY_PER_HOUR));
        setStrength(strength - (elapsedHours * STRENGTH_DECAY_PER_HOUR));

        lastDecayUpdateTime = currentTime;
    }

    //returns whether or not Ballo is dead
    public boolean isDead() {
        return statusText.contains("died") || statusText.contains("death");
    }

    public int getHunger() {
        return (int) hunger;
    }

    public int getHappiness() {
        return (int) happiness;
    }

    public int getStrength() {
        return (int) strength;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatusText() {
        return statusText;
    }

    // To get the R.drawable version of the url, call
    // getResources().getIdentifier(ballo.getImgURL() , "drawable", getPackageName());
    public String getImgURL() {
        return imgURL;
    }

    //Returns the sprite to be used while Ballo is on a walk. See comment on getImgURL
    public String getExerciseURL() {
        if (this.strength >= 50) {
            return "healthy_exercise_ballo";
        } else {
            return "unhealthy_exercise_ballo";
        }
    }

    //Stat getter methods
        public int getTimesFed() {
            return timesFed;
        }

        public int getTimesBounced() {
            return timesBounced;
        }

        public int getLowestHappiness() {
            return (int) lowestHappiness;
        }

        public int getLowestStrength() {
            return (int) lowestStrength;
        }

        public int getLowestHunger() {
            return (int) lowestHunger;
        }

        public int getHighestStrength() {
            return (int) highestStrength;
        }

        public double getDistanceWalked() {
            return Math.round(distanceWalked * 100) / 100.0;
        }

        //returns Ballo's age in days
        public int getAge() {
            Date currentDate = new Date();
            long diff = this.birthdate.getTime() - currentDate.getTime();
            return (int) diff / (24 * 60 * 60 * 1000);
        }

    //Getter and Setter methods for ObjectAnimator

        public float getCy() {
            return cy;
        }

        public void setCy(float cy) {
            this.cy = cy;
        }

    public void setImgURL(String imgURL) {
        this.imgURL = imgURL;
        notifyUpdate();
    }

    //Sets hunger. Cannot exceed 100. Ballo dies when hunger drops below 0
    private void setHunger(double hunger) {
        if (Double.isNaN(hunger)) {
            return;
        }

        this.hunger = hunger;
        if (this.hunger > 100) {
            this.hunger = 100;
        } else if (this.hunger <= 0) {
            this.hunger = 0;
            kill(this.name + " starved to death. \nTip: Feed your Ballo so it doesn't starve!");
        } else if (this.hunger < 50) {
            updateImg();
        }

        Log.v("BALLO", "Set hunger to " + this.hunger);
        lowestHunger = Math.min(this.hunger, lowestHunger);
        notifyUpdate();
    }

    //Sets happiness. Cannot exceed 100. Ballo dies when happiness drops below 0
    private void setHappiness(double happiness) {
        if (Double.isNaN(happiness)) {
            return;
        }

        this.happiness = happiness;

        Log.v("BALLO", "happiness = " + happiness);

        if (this.happiness > 100) {
            this.happiness = 100;
        } else if (this.happiness <= 0) {
            this.happiness = 0;
            kill(this.name + " died of a broken heart. \nTip: Play with your Ballo to raise its happiness!");
        } else if (this.happiness < 50) {
            updateImg();
        }

        Log.v("BALLO", "Set happiness to " + this.happiness);

        lowestHappiness = Math.min(this.happiness, lowestHappiness);
        notifyUpdate();
    }

    //Sets strength. Ballo dies when strength drops below 0
    private void setStrength(double strength) {
        if (Double.isNaN(strength)) {
            return;
        }

        this.strength = strength;


        if (this.strength <= 0) {
            this.strength = 0;
            kill(this.name + " got too weak and died. Next time walk it more often!");
        } else if (this.strength < 50) {
            updateImg();
        }
        Log.v("BALLO", "Set strength to " + this.strength);

        highestStrength = Math.max(this.strength, highestStrength);
        lowestStrength = Math.min(this.strength, lowestStrength);
        notifyUpdate();
    }

    //Kills ballo, setting his death message to the passed string
    private void kill(String status) {
        this.statusText = status;
        imgURL = DEAD_BALLO;
        notifyUpdate();
    }

    //Updates Ballo's sprite to reflect his lowest stat under 50
    public void updateImg() {
        if (hunger > 50 && happiness > 50 && strength > 50) {
            imgURL = BASIC_BALLO;
            statusText = this.name + " is doing good! " + this.name + " loves you.";

        } else if (hunger <= 0 || happiness <= 0 || strength <= 0) {
            imgURL = DEAD_BALLO;
        } else if (hunger < 50 && hunger <= happiness && hunger <= strength) {
            imgURL = HUNGRY_BALLO;
            statusText = this.name + " is starving. Feed it some food.";
        } else if (happiness < 50 && happiness <= hunger && happiness <= strength) {
            imgURL = SAD_BALLO;
            statusText = this.name + " is feeling blue. You should play with it.";
        } else if (strength < 50 && strength <= hunger && strength <= happiness) {
            imgURL = UNHEALTHY_BALLO;
            statusText = this.name + " is looking weak. Maybe take it outside!";
        }
        notifyUpdate();
    }

    private void notifyUpdate() {
        if (eventHandler != null) {
            eventHandler.onUpdate();
        }
    }

    /**
     * Sets the event handler.
     * @param eventHandler
     */
    public void setEventHandler(Events eventHandler) {
        this.eventHandler = eventHandler;
    }

    public void clearEventHandler() {
        this.eventHandler = null;
    }

    public void destroy() {
        if (decayTimer != null) {
            decayTimer.cancel();
            decayTimer.purge();
            decayTimer = null;
        }
    }
}
