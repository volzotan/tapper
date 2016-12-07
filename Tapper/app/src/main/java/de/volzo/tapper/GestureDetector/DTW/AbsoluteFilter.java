package de.volzo.tapper.GestureDetector.DTW;

/**
 * Created by tassilokarge on 05.12.16.
 */

public class AbsoluteFilter extends StreamPassthrough<Double, Double> {
    AbsoluteFilter(StreamReceiver<Double> absoluteStreamReceiver) {
        super(absoluteStreamReceiver);
    }

    @Override
    public void process(Double input) {
        super.emitElement(absolute(input));
    }

    private Double absolute(Double input) {
        return Math.abs(input);
    }
}
