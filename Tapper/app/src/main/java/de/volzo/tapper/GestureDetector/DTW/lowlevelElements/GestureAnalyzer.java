package de.volzo.tapper.GestureDetector.DTW.lowlevelElements;

import com.chan.fastdtw.dtw.FastDTW;
import com.chan.fastdtw.timeseries.TimeSeries;

import java.util.ArrayList;
import java.util.Arrays;

import de.volzo.tapper.GestureDetector.DTW.FilteringPipeline;
import de.volzo.tapper.GestureDetector.DTW.streamSystem.StreamPassthrough;
import de.volzo.tapper.GestureDetector.DTW.streamSystem.StreamReceiver;
import de.volzo.tapper.GestureDetector.GestureType;

import static com.chan.fastdtw.util.DistanceFunctionFactory.EUCLIDEAN_DIST_FN;

/**
 * Created by tassilokarge on 05.12.16.
 */

public class GestureAnalyzer extends StreamPassthrough<GestureType, Number[][]> {

    private TimeSeries[] templates = new TimeSeries[]{
            new TimeSeries("assets/templates1s/nothing.csv", false, true, ','),      //0
            new TimeSeries("assets/templates1s/doubletap1.csv", false, true, ','),   //1
            new TimeSeries("assets/templates1s/doubletap2.csv", false, true, ','),   //2
            new TimeSeries("assets/templates1s/doubletap3.csv", false, true, ','),   //3
            new TimeSeries("assets/templates1s/doubletap4.csv", false, true, ','),   //4
            new TimeSeries("assets/templates1s/pickupdrop1.csv", false, true, ','),  //5
            new TimeSeries("assets/templates1s/pickupdrop2.csv", false, true, ','),  //6
            new TimeSeries("assets/templates1s/pickupdrop3.csv", false, true, ','),  //7
            new TimeSeries("assets/templates1s/pickupdrop4.csv", false, true, ','),  //8
            new TimeSeries("assets/templates1s/pickupdrop5.csv", false, true, ','),  //9
            new TimeSeries("assets/templates1s/sidetapbottom.csv", false, true, ','),//10
            new TimeSeries("assets/templates1s/sidetapleft.csv", false, true, ','),  //11
            new TimeSeries("assets/templates1s/sidetapright.csv", false, true, ','), //12
            new TimeSeries("assets/templates1s/sidetaptop.csv", false, true, ','),   //13
            new TimeSeries("assets/templates1s/shake2.csv", false, true, ','),       //14
            new TimeSeries("assets/templates1s/shake3.csv", false, true, ','),       //15
            new TimeSeries("assets/templates1s/tap2.csv", false, true, ','),         //16
            new TimeSeries("assets/templates1s/tap4.csv", false, true, ','),         //17
            new TimeSeries("assets/templates1s/tap5.csv", false, true, ','),         //18
            //TODO: default cases, more gestures
    };

    private int[] templatesUsed = new int[]{0,3,4,8,9,10,13,15,16,17};

    //diagnostic properties
    private long[] distances = new long[19];
    private int[] templateRecognizedFrequency = new int[19];

    public GestureAnalyzer(StreamReceiver<GestureType> gestureStreamReceiver) {
        super(gestureStreamReceiver);
        //filter raw templates
        filterRawTemplates();
    }
    @Override
    public void process(Number[][] input) {
        GestureType analyzed = analyze(input);
        if (analyzed != GestureType.NOTHING) {
            System.out.println(Arrays.toString(distances));
        }
        super.emitElement(analyze(input));
    }

    private void filterRawTemplates() {
        //filter all time series
        for (int i = 0; i < templates.length; i++) {
            //collect filtered values from current time series in these objects
            ArrayList<Number[]> filtered = new ArrayList<>(templates[i].size());
            //set up a new filtering pipeline for each time series (to have clean pipeline state)
            FilteringPipeline filter = new FilteringPipeline(filtered::add);
            //filter each value with filtering pipeline
            for (int j = 0; j < templates[i].size(); j++) {
                double[] vector = templates[i].getMeasurementVector(j);
                filter.process(new Double[]{vector[0], vector[1], vector[2]});
            }
            //replace time series with filtered time series
            Number[][] d = new Number[0][];
            templates[i] = new TimeSeries(filtered.toArray(d));
        }
    }


    private GestureType analyze(Number[][] windowArrays) {

        TimeSeries timeSeries = new TimeSeries(windowArrays);

        int minDistIndex = -1;
        double minWarpDist = Double.MAX_VALUE;

        for (int i = 0; i < templatesUsed.length; i++) {
            //search radius 5 from the DTW paper. Allows low error with timeseries of up to 1000 points
            double dist = FastDTW.getWarpDistBetween(timeSeries, templates[templatesUsed[i]], 5, EUCLIDEAN_DIST_FN);
            distances[templatesUsed[i]] = Math.round(dist);
            if (dist < minWarpDist) {
                minWarpDist = dist;
                minDistIndex = templatesUsed[i];
            }
        }

        //if (minDistIndex != 0) {
        //    templateRecognizedFrequency[minDistIndex]++;
        //    System.out.println(Arrays.toString(templateRecognizedFrequency));
        //}

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
                System.out.println("Shake");
                return GestureType.SHAKE;
            case 16:
            case 17:
            case 18:
                System.out.println("Tap");
                return GestureType.TAP;
            default:
                System.out.println("default (this is a fault, fixme)");
                return GestureType.NOTHING;
        }
    }
}
