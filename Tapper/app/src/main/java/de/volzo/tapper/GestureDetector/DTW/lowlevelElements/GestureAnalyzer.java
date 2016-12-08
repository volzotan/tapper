package de.volzo.tapper.GestureDetector.DTW.lowlevelElements;

import com.chan.fastdtw.dtw.FastDTW;
import com.chan.fastdtw.timeseries.TimeSeries;

import java.util.ArrayList;

import de.volzo.tapper.GestureDetector.DTW.FilteringPipeline;
import de.volzo.tapper.GestureDetector.DTW.StreamPassthrough;
import de.volzo.tapper.GestureDetector.DTW.StreamReceiver;
import de.volzo.tapper.GestureDetector.GestureType;

import static com.chan.fastdtw.util.DistanceFunctionFactory.EUCLIDEAN_DIST_FN;

/**
 * Created by tassilokarge on 05.12.16.
 */

public class GestureAnalyzer extends StreamPassthrough<GestureType, Integer[][]> {

    private TimeSeries[] templates = new TimeSeries[]{
            new TimeSeries("assets/templates/nothing.csv", false),      //0
            new TimeSeries("assets/templates/doubletap1.csv", false),   //1
            new TimeSeries("assets/templates/doubletap2.csv", false),   //2
            new TimeSeries("assets/templates/doubletap3.csv", false),   //3
            new TimeSeries("assets/templates/doubletap4.csv", false),   //4
            new TimeSeries("assets/templates/pickupdrop1.csv", false),  //5
            new TimeSeries("assets/templates/pickupdrop2.csv", false),  //6
            new TimeSeries("assets/templates/pickupdrop3.csv", false),  //7
            new TimeSeries("assets/templates/pickupdrop4.csv", false),  //8
            new TimeSeries("assets/templates/pickupdrop5.csv", false),  //9
            new TimeSeries("assets/templates/sidetapbottom.csv", false),//10
            new TimeSeries("assets/templates/sidetapleft.csv", false),  //11
            new TimeSeries("assets/templates/sidetapright.csv", false), //12
            new TimeSeries("assets/templates/sidetaptop.csv", false),   //13
            new TimeSeries("assets/templates/shake1.csv", false),//14
            new TimeSeries("assets/templates/shake2.csv", false),  //15
            new TimeSeries("assets/templates/shake3.csv", false) //16
            //TODO: default cases, more gestures
    };

    public GestureAnalyzer(StreamReceiver<GestureType> gestureStreamReceiver) {
        super(gestureStreamReceiver);
        //filter raw templates
        filterRawTemplates();
    }
    @Override
    public void process(Integer[][] input) {
        super.emitElement(analyze(input));
    }

    private void filterRawTemplates() {
        //filter all time series
        for (int i = 0; i < templates.length; i++) {
            //collect filtered values from current time series in these objects
            ArrayList<Integer[]> filtered = new ArrayList<>(templates[i].size());
            //set up a new filtering pipeline for each time series (to have clean pipeline state)
            FilteringPipeline filter = new FilteringPipeline(filtered::add);
            //filter each value with filtering pipeline
            for (int j = 0; j < templates[i].size(); j++) {
                double[] vector = templates[i].getMeasurementVector(j);
                filter.process(new Double[]{vector[0], vector[1], vector[2]});
            }
            //replace time series with filtered time series
            Integer[][] d = new Integer[0][];
            templates[i] = new TimeSeries(filtered.toArray(d));
        }
    }


    private GestureType analyze(Integer[][] windowArrays) {

        TimeSeries timeSeries = new TimeSeries(windowArrays);

        int minDistIndex = -1;
        double minWarpDist = Double.MAX_VALUE;

        for (int i = 0; i < templates.length; i++) {
            //search radius 5 from the DTW paper. Allows low error with timeseries of up to 1000 points
            double dist = FastDTW.getWarpDistBetween(timeSeries, templates[i], 5, EUCLIDEAN_DIST_FN);
            if (dist < minWarpDist) {
                minWarpDist = dist;
                minDistIndex = i;
            }
            System.out.println("Warp distance " + i + " = " + dist);
        }

        switch (minDistIndex) {
            case 0:
                return GestureType.NOTHING;
            case 1:
            case 2:
            case 3:
            case 4:
                System.out.println("Doubletap");
                return GestureType.DOUBLETAP;
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
                System.out.println("Pickupdrop");
                return GestureType.PICKUPDROP;
            case 10:
            case 11:
            case 12:
            case 13:
                System.out.println("Sidetap");
                return GestureType.SIDETAP;
            case 14:
            case 15:
            case 16:
                System.out.println("Shake");
                return GestureType.SHAKE;
            default:
                System.out.println("default (this is a fault, fixme)");
                return GestureType.NOTHING;
        }
    }
}
