package de.volzo.tapper.GestureDetector.DTW;

/**
 * Created by tassilokarge on 05.12.16.
 */

public class AbsoluteFilter extends StreamElement<Double> {
    AbsoluteFilter(Consumer<Double> absoluteConsumer) {
        super(absoluteConsumer);
    }

    public void absolute(double input) {
        super.passProcessedElement(Math.abs(input));
    }
}
