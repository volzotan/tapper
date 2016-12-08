package de.volzo.tapper.GestureDetector.DTW.lowlevelElements;

import de.volzo.tapper.GestureDetector.DTW.streamSystem.StreamPassthrough;
import de.volzo.tapper.GestureDetector.DTW.streamSystem.StreamReceiver;

/**
 * Created by tassilokarge on 05.12.16.
 */

public class AbsoluteFilter extends StreamPassthrough<Double, Double> {
    public AbsoluteFilter(StreamReceiver<Double> absoluteStreamReceiver) {
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
