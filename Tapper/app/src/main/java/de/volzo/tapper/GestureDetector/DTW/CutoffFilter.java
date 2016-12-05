package de.volzo.tapper.GestureDetector.DTW;

/**
 * Created by tassilokarge on 05.12.16.
 */

public class CutoffFilter extends StreamElement<Double> {

    CutoffFilter(Consumer<Double> cutoffConsumer) {
        super(cutoffConsumer);
    }

    /**
     * cuts off when certain threshold is not surpassed
     *
     * @param input the uncut input
     * @param cutoffThreshold the threshold under which everything is zero
     * @return the cut input
     */
    public void cutoff(Double input, double cutoffThreshold) {
        // cutoff
        if (input < cutoffThreshold) {
            super.passProcessedElement(0d);
        } else {
            super.passProcessedElement(input);
        }
    }
}
