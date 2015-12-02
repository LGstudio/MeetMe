package cz.vutbr.fit.tam.meetme.release.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

import cz.vutbr.fit.tam.meetme.R;

/**
 * Created by Jakub on 22. 11. 2015.
 */
public class SensorService extends Service implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor accelerometer;
    private Sensor magnetometer;

    private float[] sensor_gravity;
    private float[] sensor_magnetic;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer  = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        if (accelerometer == null || magnetometer == null) {
            stopSelf();
            return START_NOT_STICKY;
        }

        mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);

        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSensorManager.unregisterListener(this);
        this.stopSelf();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        float[] orientation = new float[3];

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            sensor_gravity = lowPassFilter(event.values.clone(), sensor_gravity);
        }

        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            sensor_magnetic = lowPassFilter(event.values.clone(), sensor_magnetic);
        }

        if (sensor_gravity != null && sensor_magnetic != null) {

            float R[] = new float[9];
            float I[] = new float[9];

            if (SensorManager.getRotationMatrix(R, I, sensor_gravity, sensor_magnetic)) {
                SensorManager.getOrientation(R, orientation);
            }
        }

        float azimuth = (float) Math.round(Math.toDegrees((double) orientation[0]));

        Context context = getApplicationContext();

        Intent intent = new Intent(context.getString(R.string.wear_rotation_intent_filter));
        intent.putExtra(context.getString(R.string.rotation_x), Float.toString(azimuth));
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private float[] lowPassFilter(float[] input, float[] output) {

        float a = 0.3f;

        if (output == null || output.length == 0) {
            return input;
        }

        for (int i = 0; i < input.length; i++ ) {
            output[i] = output[i] + a * (input[i] - output[i]);
        }

        return output;
    }
}
