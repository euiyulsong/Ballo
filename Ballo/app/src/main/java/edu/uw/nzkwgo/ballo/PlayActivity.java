package edu.uw.nzkwgo.ballo;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import org.w3c.dom.Text;

public class PlayActivity extends AppCompatActivity implements SensorEventListener, Ballo.Events {

    private static final String TAG = "Play";

    private DrawingSurfaceView view;

    // variables for shake detection
    private static final int SHAKE_THRESHOLD = 3; // m/S**2
    private static final int MIN_TIME_BETWEEN_SHAKES_MILLISECS = 1000;
    private long mLastShakeTime;
    private SensorManager mSensorMgr;
    private Ballo ballo;
    private TextView playVal;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ballo.destroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        ballo = Ballo.getBallo(this);
        ballo.setEventHandler(this);

        view = (DrawingSurfaceView)findViewById(R.id.drawingView);
        ballo.cy = view.getHeight() - (view.getHeight() / 3);

        mSensorMgr = (SensorManager) getSystemService(SENSOR_SERVICE);
        // Listen for shakes

        Sensor accelerometer = mSensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometer != null) {
            mSensorMgr.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }

        playVal = (TextView) findViewById(R.id.playVal);
        playVal.setText("Hunger: " + ballo.getHunger() + " Happiness: " + ballo.getHappiness() + " Strength: " + ballo.getStrength());

        findViewById(R.id.homeBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(PlayActivity.this, HomeActivity.class));
            }
        });
        onUpdate();
    }
    @Override

    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            long curTime = System.currentTimeMillis();
            if ((curTime - mLastShakeTime) > MIN_TIME_BETWEEN_SHAKES_MILLISECS) {

                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];

                double acceleration = Math.sqrt(Math.pow(x, 2) +
                        Math.pow(y, 2) +
                        Math.pow(z, 2)) - SensorManager.GRAVITY_EARTH;

                if (acceleration > SHAKE_THRESHOLD) {
                    mLastShakeTime = curTime;
                    //Shook
                    view.ballo = ballo;
                    view.ballo.cy = view.getHeight() - (view.getHeight() / 3);
                    bounceAnim(acceleration);
                    ballo.bounce();
                    Ballo.saveBallo(this, ballo);
                }
            }
        }
    }

    public void bounceAnim(double acceleration) {
        view.ballo.setImgURL("excited_ballo");
        ObjectAnimator upAnim = ObjectAnimator.ofFloat(view.ballo, "Cy", view.getHeight() - (2 * view.getHeight() / 3));
        upAnim.setDuration(500);

        ObjectAnimator downAnim = ObjectAnimator.ofFloat(view.ballo, "Cy", view.getHeight() - (view.getHeight() / 3));
        downAnim.setDuration(400);
        downAnim.addListener(new AnimatorListenerAdapter() {
             @Override
             public void onAnimationEnd(Animator animation) {
                 view.ballo.updateImg();
             }
        });

        AnimatorSet set = new AnimatorSet();
        set.playSequentially(upAnim, downAnim);
        set.start();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Ignore
    }

    @Override
    public void onPause() {
        super.onPause();
        mSensorMgr.unregisterListener(this);
    }

    @Override
    public void onUpdate() {
        if (ballo == null) {
            return;
        }
      
        if (ballo.isDead()) {
            mSensorMgr.unregisterListener(this);
            startActivity(new Intent(PlayActivity.this, StatsActivity.class));
        }

        TextView playVal = (TextView) findViewById(R.id.playVal);
        playVal.setText("Hunger: " + ballo.getHunger() + " Happiness: " + ballo.getHappiness() + " Strength: " + ballo.getStrength());
        Ballo.saveBallo(this, ballo);
    }
}
