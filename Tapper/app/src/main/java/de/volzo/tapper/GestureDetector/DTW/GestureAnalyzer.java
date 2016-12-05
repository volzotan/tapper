package de.volzo.tapper.GestureDetector.DTW;

import com.chan.fastdtw.dtw.FastDTW;
import com.chan.fastdtw.timeseries.TimeSeries;

import de.volzo.tapper.GestureDetector.GestureType;

import static com.chan.fastdtw.util.DistanceFunctionFactory.EUCLIDEAN_DIST_FN;

/**
 * Created by tassilokarge on 05.12.16.
 */

public class GestureAnalyzer extends StreamElement<GestureType> {

    private TimeSeries[] templates = new TimeSeries[]{}; //TODO: create templates for this array

    public GestureAnalyzer(Consumer<GestureType> gestureConsumer) {
        super(gestureConsumer);
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
