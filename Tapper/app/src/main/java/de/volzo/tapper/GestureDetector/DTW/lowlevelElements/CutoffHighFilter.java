package de.volzo.tapper.GestureDetector.DTW.lowlevelElements;

import de.volzo.tapper.GestureDetector.DTW.streamSystem.StreamPassthrough;
import de.volzo.tapper.GestureDetector.DTW.streamSystem.StreamReceiver;

/**
 * Created by tassilokarge on 05.12.16.
 */

public class CutoffHighFilter extends StreamPassthrough<Double, Double> {

    private final Double cutoffThreshold;

    public CutoffHighFilter(Double cutoffThreshold, StreamReceiver<Double> cutoffStreamReceiver) {
        super(cutoffStreamReceiver);
        this.cutoffThreshold = cutoffThreshold;
    }

    @Override
    public void process(Double input) {
        super.emitElement(cutoff(input));
    }

    /**
     * cuts off when certain threshold is not surpassed
     *
     * @param input the uncut input
     * @return the cut input
     */
    private Double cutoff(Double input) {
        // cutoffThreshold
        if (input > cutoffThreshold) {
            return cutoffThreshold;
        } else {
            return input;
        }
    }
}
