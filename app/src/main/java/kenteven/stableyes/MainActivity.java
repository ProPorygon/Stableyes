package kenteven.stableyes;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.widget.ImageView;


public class MainActivity extends AppCompatActivity implements SensorEventListener, View.OnTouchListener
{

    private SensorManager sensorManager;
    private Sensor accelerometer;

    float newdx, newdy, prevdx, prevdy;
    float maxdx, maxdy;
    int move;
    boolean isAnimating;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        isAnimating=false;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        move = 0;
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Use this to toggle movement", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                move = 1-move;
                maxdx=0;
                maxdy=0;
            }
        });

        maxdx=0;
        maxdy=0;

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        sensorManager.registerListener(this, accelerometer, sensorManager.SENSOR_DELAY_GAME);

        ImageView img = (ImageView) findViewById(R.id.imageView);
        img.setImageResource(R.drawable.random);
        float initx = img.getX();
        float inity = img.getY();
        //img.setOnTouchListener(this);
        Stabilize.init();
    }


    @Override
    public boolean onTouch(View view, MotionEvent event) {

        switch (event.getActionMasked()) {

            case MotionEvent.ACTION_DOWN:

                Stabilize.dX = view.getX() - event.getRawX();
                Stabilize.dY = view.getY() - event.getRawY();
                break;

            case MotionEvent.ACTION_MOVE:

                view.animate()
                        .x(event.getRawX() + (float)Stabilize.dX)
                        .y(event.getRawY() + (float) Stabilize.dY)
                        .setDuration(0)
                        .start();
                break;
            default:
                return false;
        }
        return true;
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor mySensor = event.sensor;
        if(mySensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            if(x<0.1&&x>-0.1)
                x=0;
            if(y<0.1&&y>-0.1)
                y=0;
            if(z<0.1&&z>-0.1)
                z=0;

            //if(Math.abs(x)>maxdx)
            //    maxdx=Math.abs(x);
            //if(Math.abs(y)>maxdy)
            //    maxdy=Math.abs(y);


            //add each x and y value to the circular queue.
            Stabilize.addToAccArrays(x, y);

            //double shake = Stabilize.checkIfShaking();

            //if(shake>3)
            stabilize();
            //Log.d("MAX VALUE", "Max X: " + maxdx + " Max Y: " + maxdy);

            //Log.d("VALUE", "X: " + x + " Y: " + y + " Z: " + z + " Shake: " + shake);
        }

    }

    public void stabilize(){
        ImageView view = (ImageView)findViewById(R.id.imageView);
        Stabilize.updateVariables();
        //Log.e("Change", "Dx: "+dX+" Dy: "+dY);
        ViewPropertyAnimator animation = view.animate();
        animation.x(Stabilize.initx + (float) Stabilize.dX).y(Stabilize.inity + (float) Stabilize.dY);
        animation.setDuration(0);
        Stabilize.dX=0;
        Stabilize.dY=0;

    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, sensorManager.SENSOR_DELAY_NORMAL);
    }
}
