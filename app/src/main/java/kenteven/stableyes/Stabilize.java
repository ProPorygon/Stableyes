package kenteven.stableyes;

import android.content.Context;
import android.widget.ImageView;

import java.util.ArrayList;
/**
 * Created by bhuvan-venkatesh on 10/10/2015.
 */
public class Stabilize {

    static public Context context;
    static double dX, dY;
    static ArrayList<Double> accx = new ArrayList<>(), accy = new ArrayList<>();
    static float initx, inity;
    static double[] response;
    static int arrPtr;

    public static void init()
    {
        arrPtr=0;
        response = new double[100];

        double k = 1;
        for(int i=0; i<100; i++){
            accx.add(0.0);
            accy.add(0.0);
            response[99-i] = i*Math.pow(Math.E, -i*Math.sqrt(k));
        }
    }

    public static void updateVariables()
    {
        //if(move==1)
        //    view.animate().x(dx).y(dy).setDuration(0).start();

        //Y(t) = H(t) * -A(t)
        //H(t) = response(t)

        for(int i=0; i< 100; i++){
            Stabilize.dX += Stabilize.response[i]*Stabilize.accx.get(i);
            Stabilize.dY += Stabilize.response[i]*Stabilize.accy.get(i)*-1;
        }
        Stabilize.dX *= 10;
        Stabilize.dY *= 10;
    }

    public static double checkIfShaking() {

        //take the absolute value of the last 35(variable) values of acceleration, average them,
        // then add the x & y components to check against a threshold
        double sum=0;
        for(int i=65; i<100 ; i++){
            sum += Math.abs(accx.get(i))+Math.abs(accy.get(i));
        }
        sum = sum/35;//avg the values by dividing the sum by 35
        return sum;
    }

    public static void addToAccArrays(float x, float y) {
        if(accx.size() > 99) //accx and accy have the same size at all times
        {
            accx.remove(0);
            accy.remove(0);
        }

        accx.add(new Double(x));
        accy.add(new Double(y));
    }
}
