package de.volzo.tapper.GestureDetector.DTW;

import com.chan.fastdtw.dtw.FastDTW;
import com.chan.fastdtw.timeseries.TimeSeries;

import java.util.function.Consumer;

import de.volzo.tapper.GestureDetector.GestureType;

import static com.chan.fastdtw.util.DistanceFunctionFactory.EUCLIDEAN_DIST_FN;

/**
 * Created by tassilokarge on 05.12.16.
 */

public class GestureAnalyzer {

    private TimeSeries[] templates = new TimeSeries[]{}; //TODO: create templates for this array

    private Consumer<GestureType> gestureConsumer;

    public GestureAnalyzer(Consumer<GestureType> gestureConsumer) {
        this.gestureConsumer = gestureConsumer;
    }

    public void updateData(Double[] windowX, Double[] windowY, Double[] windowZ) {
        TimeSeries timeSeries = new TimeSeries(windowX, windowY, windowZ);

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
                gestureConsumer.accept(GestureType.NOTHING);
                break;
        }
    }
}
