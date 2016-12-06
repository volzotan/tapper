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

    private TimeSeries[] templates = new TimeSeries[]{}; //TODO: create templates for this array

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
        }

        //TODO: convert index to gesture
        switch (minDistIndex) {
            default:
                super.passProcessedElement(GestureType.NOTHING);
                break;
        }
    }
}
