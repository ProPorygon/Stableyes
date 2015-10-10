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
import android.widget.ImageView;


public class MainActivity extends AppCompatActivity implements SensorEventListener, View.OnTouchListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;

    double dX, dY;
    float newdx, newdy, prevdx, prevdy;
    float maxdx, maxdy;
    int move;
    double[] accx, accy;
    int arrPtr;
    double[] response;
    float initx, inity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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

        accx = new double[100];
        accy = new double[100];
        arrPtr=0;
        response = new double[100];

        double k = 1;
        for(int i=0; i<100; i++){
            accx[i]=0;
            accy[i]=0;
            response[99-i] = i*Math.pow(Math.E, -i*Math.sqrt(k));
        }

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
    }


    @Override
    public boolean onTouch(View view, MotionEvent event) {

        switch (event.getActionMasked()) {

            case MotionEvent.ACTION_DOWN:

                dX = view.getX() - event.getRawX();
                dY = view.getY() - event.getRawY();
                break;

            case MotionEvent.ACTION_MOVE:

                view.animate()
                        .x(event.getRawX() + (float)dX)
                        .y(event.getRawY() + (float) dY)
                        .setDuration(0)
                        .start();
                break;
            default:
                return false;
        }
        return true;
    }

    public void stabilize(){
        ImageView view = (ImageView)findViewById(R.id.imageView);

        //if(move==1)
        //    view.animate().x(dx).y(dy).setDuration(0).start();

        //Y(t) = H(t) * -A(t)
        //H(t) = response(t)

        for(int i=0; i< 100; i++){
            dX += response[i]*accx[i];
            dY += response[i]*accy[i]*-1;
        }
        dX *= 10;
        dY *= 10;
        Log.e("Change", "Dx: "+dX+" Dy: "+dY);
        view.animate().x(initx+(float)dX).y(inity+(float)dY).setDuration(0).start();
        dX=0;
        dY=0;

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
            addToAccArrays(x,y);

            double shake = checkIfShaking();

            //if(shake>3)
                stabilize();
            //Log.d("MAX VALUE", "Max X: " + maxdx + " Max Y: " + maxdy);

            Log.d("VALUE", "X: " + x + " Y: " + y + " Z: " + z + " Shake: " + shake);
            stabilize();
        }

    }

    private double checkIfShaking() {

        //take the absolute value of the last 35(variable) values of acceleration, average them,
        // then add the x & y components to check against a threshold
        double sum=0;
        for(int i=65; i<100 ; i++){
            sum += Math.abs(accx[i])+Math.abs(accy[i]);
        }
        sum = sum/35;//avg the values by dividing the sum by 35
        return sum;
    }

    private void addToAccArrays(float x, float y) {
        if(arrPtr<99){
            accx[arrPtr]=x;
            accy[arrPtr]=y;
            arrPtr++;
        }
        else{
            for(int i = 0; i<99; i++){
                accx[i] = accx[i+1];
                accy[i] = accy[i+1];
            }
            accx[99]=x;
            accy[99]=y;
        }
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
