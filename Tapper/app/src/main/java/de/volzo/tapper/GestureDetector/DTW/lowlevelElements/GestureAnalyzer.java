package de.volzo.tapper.GestureDetector.DTW.lowlevelElements;

import com.chan.fastdtw.dtw.FastDTW;
import com.chan.fastdtw.timeseries.TimeSeries;

import java.util.ArrayList;

import de.volzo.tapper.GestureDetector.DTW.FilteringPipeline;
import de.volzo.tapper.GestureDetector.DTW.streamSystem.StreamPassthrough;
import de.volzo.tapper.GestureDetector.DTW.streamSystem.StreamReceiver;
import de.volzo.tapper.GestureDetector.GestureType;

import static com.chan.fastdtw.util.DistanceFunctionFactory.MANHATTAN_DIST_FN;

/**
 * Created by tassilokarge on 05.12.16.
 */

public class GestureAnalyzer extends StreamPassthrough<GestureType, Number[][]> {

    /*
    private TimeSeries[] templates = new TimeSeries[]{
            new TimeSeries("assets/templatesShort/nothing.csv", false, true, ','),      //0
            new TimeSeries("assets/templatesShort/doubletap1.csv", false, true, ','),   //1
            new TimeSeries("assets/templatesShort/doubletap2.csv", false, true, ','),   //2
            new TimeSeries("assets/templatesShort/doubletap3.csv", false, true, ','),   //3
            new TimeSeries("assets/templatesShort/doubletap4.csv", false, true, ','),   //4
            new TimeSeries("assets/templatesShort/pickupdrop1.csv", false, true, ','),  //5
            new TimeSeries("assets/templatesShort/pickupdrop2.csv", false, true, ','),  //6
            new TimeSeries("assets/templatesShort/pickupdrop3.csv", false, true, ','),  //7
            new TimeSeries("assets/templatesShort/pickupdrop4.csv", false, true, ','),  //8
            new TimeSeries("assets/templatesShort/pickupdrop5.csv", false, true, ','),  //9
            new TimeSeries("assets/templatesShort/sidetapbottom.csv", false, true, ','),//10
            new TimeSeries("assets/templatesShort/sidetapleft.csv", false, true, ','),  //11
            new TimeSeries("assets/templatesShort/sidetapright.csv", false, true, ','), //12
            new TimeSeries("assets/templatesShort/sidetaptop.csv", false, true, ','),   //13
            new TimeSeries("assets/templatesShort/shake2.csv", false, true, ','),       //14
            new TimeSeries("assets/templatesShort/shake3.csv", false, true, ','),       //15
            new TimeSeries("assets/templatesShort/tap2.csv", false, true, ','),         //16
            new TimeSeries("assets/templatesShort/tap4.csv", false, true, ','),         //17
            new TimeSeries("assets/templatesShort/tap5.csv", false, true, ','),         //18
            //TODO: default cases, more gestures
    };

    private int[] templatesUsed = new int[]{0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18};

    //diagnostic properties
    private long[] distances = new long[19];
    private int[] templateRecognizedFrequency = new int[19];

    */

    private TimeSeries[] templates = new TimeSeries[GestureType.getAllPublicGestureTypes().length+1];

    private int[] templatesUsed = new int[]{0,1,2,3,4};

    public GestureAnalyzer(StreamReceiver<GestureType> gestureStreamReceiver) {
        super(gestureStreamReceiver);

        //nothing template
        templates[0] = new TimeSeries("NOTHING", false, false, ',');
        //one template for each type
        GestureType[] allGestureTypes = GestureType.getAllPublicGestureTypes();
        for (int i = 0; i < allGestureTypes.length; i++) {
            templates[i+1] = new TimeSeries(allGestureTypes[i].name(), false, false, ',');
        }
        //filter raw templates
        filterRawTemplates();
    }
    @Override
    public void process(Number[][] input) {
        GestureType analyzed = analyze(input);
        //if (analyzed != GestureType.NOTHING) {
        //    System.out.println("Dist: " + Arrays.toString(distances));
        //}
        super.emitElement(analyzed);
    }

    private void filterRawTemplates() {
        //filter all time series
        for (int i = 0; i < templates.length; i++) {
            //collect filtered values from current time series in these objects
            ArrayList<Number[]> filtered = new ArrayList<>();
            //set up a new filtering pipeline for each time series (to have clean pipeline state)
            FilteringPipeline filter = new FilteringPipeline(filtered::add);
            //trim the time series (dynamic time warping makes endless noise irrelevant)
            Trimmer trimmer = new Trimmer(filter, 0.5, 10);
            //filter each value with filtering pipeline
            for (int j = 0; j < templates[i].size(); j++) {
                double[] vector = templates[i].getMeasurementVector(j);
                trimmer.process(new Double[]{vector[0], vector[1], vector[2]});
            }
            System.out.println("filtered template length: " + filtered.size());
            //replace time series with filtered time series
            Number[][] d = new Number[0][];
            templates[i] = new TimeSeries(filtered.toArray(d));
        }
    }


    private GestureType analyze(Number[][] windowArrays) {

        TimeSeries timeSeries = new TimeSeries(windowArrays);

        double nothingDist = FastDTW.getWarpDistBetween(timeSeries, templates[0], 0, MANHATTAN_DIST_FN);
        if (nothingDist <= 5) {
            //for sure it´s nothing - early exit
            return GestureType.NOTHING;
        }

        //distances[0] = Math.round(nothingDist);
        int minDistIndex = 0;
        double minWarpDist = nothingDist;

        for (int i = 1; i < templatesUsed.length; i++) {
            //search radius 5 from the DTW paper. Allows low error with timeseries of up to 1000 points
            double dist = FastDTW.getWarpDistBetween(timeSeries, templates[templatesUsed[i]], 5, MANHATTAN_DIST_FN);
            //distances[templatesUsed[i]] = Math.round(dist);
            if (dist < minWarpDist) {
                minWarpDist = dist;
                minDistIndex = templatesUsed[i];
            }
            if (dist <= 8) {
                //early exit
                break;
            }
        }

        //if (minDistIndex != 0) {
        //    templateRecognizedFrequency[minDistIndex]++;
        //    System.out.println("Freq: " + Arrays.toString(templateRecognizedFrequency));
        //}

        if (minDistIndex > 0) {
            return GestureType.getAllPublicGestureTypes()[minDistIndex - 1];
        } else {
            return GestureType.NOTHING;
        }

        /*
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
        }*/
    }
}
