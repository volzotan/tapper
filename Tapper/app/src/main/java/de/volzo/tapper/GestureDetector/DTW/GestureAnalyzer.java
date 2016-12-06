package de.volzo.tapper.GestureDetector.DTW;

import com.chan.fastdtw.dtw.FastDTW;
import com.chan.fastdtw.timeseries.TimeSeries;

import java.util.ArrayList;

import de.volzo.tapper.GestureDetector.GestureType;

import static com.chan.fastdtw.util.DistanceFunctionFactory.EUCLIDEAN_DIST_FN;

/**
 * Created by tassilokarge on 05.12.16.
 */

public class GestureAnalyzer extends StreamElement<GestureType> {

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
            new TimeSeries("assets/templates/sidetaptop.csv", false)   //13
            //TODO: default cases, more gestures
    };

    public GestureAnalyzer(Consumer<GestureType> gestureConsumer) {
        super(gestureConsumer);

        //filter raw templates
        filterRawTemplates();
    }

    private void filterRawTemplates() {
        //filter all time series
        for (int i = 0; i < templates.length; i++) {
            //collect filtered values from current time series in these objects
            ArrayList<Double> filteredXY = new ArrayList<>(templates[i].size());
            ArrayList<Double> filteredZ = new ArrayList<>(templates[i].size());
            //set up a new filtering pipeline for each time series (to have clean pipeline state)
            FilteringPipeline filter = new FilteringPipeline(filteredXY::add, filteredZ::add);
            //filter each value with filtering pipeline
            for (int j = 0; j < templates[i].size(); j++) {
                double[] vector = templates[i].getMeasurementVector(j);
                filter.filter(vector[0], vector[1], vector[2]);
            }
            //replace time series with filtered time series
            Double[] d = new Double[0];
            templates[i] = new TimeSeries(filteredXY.toArray(d), filteredZ.toArray(d));
        }
    }

    public void analyze(Double[]... windowArrays) {

        TimeSeries timeSeries = new TimeSeries(windowArrays);

        int minDistIndex = -1;
        double minWarpDist = Double.MAX_VALUE;

        for (int i = 0; i < templates.length; i++) {
            double dist = FastDTW.getWarpDistBetween(timeSeries, templates[i], EUCLIDEAN_DIST_FN);
            if (dist < minWarpDist) {
                minWarpDist = dist;
                minDistIndex = i;
            }
            //System.out.println("Warp distance " + i + " = " + dist);
        }

        //TODO: convert index to gesture
        switch (minDistIndex) {
            case 0:
                super.passProcessedElement(GestureType.NOTHING);
                break;
            case 1:
            case 2:
            case 3:
            case 4:
                super.passProcessedElement(GestureType.DOUBLETAP);
                System.out.println("Doubletap");
                break;
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
                super.passProcessedElement(GestureType.PICKUPDROP);
                System.out.println("Pickupdrop");
                break;
            case 10:
            case 11:
            case 12:
            case 13:
                super.passProcessedElement(GestureType.SIDETAP);
                System.out.println("Sidetap");
                break;
            default:
                super.passProcessedElement(GestureType.NOTHING);
                System.out.println("movearound");
                break;
        }
    }
}
