package course.examples.compass;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.ImageView;

public class CompassActivity extends Activity implements SensorEventListener {

	// Sensors & SensorManager
	private Sensor accelerometer;
	private Sensor magnetometer;
	private SensorManager mSensorManager;

	// Storage for Sensor readings
	private float[] mGravity = null;
	private float[] mGeomagnetic = null;

	// View showing the compass arrow
	private ImageView mArrowImage;

	//filter old data

    private float mFilterOldValue =0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mArrowImage = findViewById(R.id.arrowImage);

		// Get a reference to the SensorManager
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

		// Get a reference to the accelerometer
		accelerometer = mSensorManager
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

		// Get a reference to the magnetometer
		magnetometer = mSensorManager
				.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

		// Exit unless both sensors are available
		if (null == accelerometer || null == magnetometer)
			finish();

	}

	@Override
	protected void onResume() {
		super.onResume();


		// Register for sensor updates

		mSensorManager.registerListener(this, accelerometer,
				SensorManager.SENSOR_DELAY_NORMAL);

		mSensorManager.registerListener(this, magnetometer,
				SensorManager.SENSOR_DELAY_NORMAL);
	}

	@Override
	protected void onPause() {
		super.onPause();

		// Unregister all sensors
		mSensorManager.unregisterListener(this);

	}

	@Override
	public void onSensorChanged(SensorEvent event) {

		// Acquire accelerometer event data
		
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

			mGravity = new float[3];
			System.arraycopy(event.values, 0, mGravity, 0, 3);

		} 
		
		// Acquire magnetometer event data
		
		else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {

			mGeomagnetic = new float[3];
			System.arraycopy(event.values, 0, mGeomagnetic, 0, 3);

		}

		// If we have readings from both sensors then
		// use the readings to compute the device's orientation
		// and then update the display.

		if (mGravity != null && mGeomagnetic != null) {

			float rotationMatrix[] = new float[9];

			// Users the accelerometer and magnetometer readings
			// to compute the device's rotation with respect to
			// a real world coordinate system

			boolean success = SensorManager.getRotationMatrix(rotationMatrix,
					null, mGravity, mGeomagnetic);

			if (success) {

				float orientationMatrix[] = new float[3];

				// Returns the device's orientation given
				// the rotationMatrix

				SensorManager.getOrientation(rotationMatrix, orientationMatrix);

				// Get the rotation, measured in radians, around the Z-axis
				// Note: This assumes the device is held flat and parallel
				// to the ground

				float rotationInRadians = orientationMatrix[0];

				// Convert from radians to degrees
				float rotationInDegrees = (float) Math.toDegrees(rotationInRadians);

				//rotate view in-reverse after filtering
				float rotationShift = lowPassFilter(-rotationInDegrees);

				// redraw image
				mArrowImage.setRotation(rotationShift);

				// Reset sensor event data arrays
				mGravity = mGeomagnetic = null;

			}
		}

	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// N/A
	}

	//simple iir low pass filter
	private float lowPassFilter(float newValue){
	    float alpha =0.2f;
	    float calculatedValue = newValue*alpha+ mFilterOldValue *(1-alpha);
	    mFilterOldValue = calculatedValue;
	    return calculatedValue;
    }
}
