package de.volzo.tapper.GestureDetector.DTW.lowlevelElements;

import org.apache.commons.collections4.queue.CircularFifoQueue;

import de.volzo.tapper.GestureDetector.DTW.streamSystem.StreamPassthrough;
import de.volzo.tapper.GestureDetector.DTW.streamSystem.StreamReceiver;

/**
 * Created by tassilokarge on 16.12.16.
 */

public class Trimmer extends StreamPassthrough<Double[], Double[]> {

    private final double threshold;
    private final int padding;

    private CircularFifoQueue<Double[]> previousValues;
    private int omitted = 0;
    private int after = 0;

    Trimmer(StreamReceiver<Double[]> trimmedReceiver, double threshold, int padding) {
        super(trimmedReceiver);
        this.threshold = threshold;
        this.padding = padding;
        previousValues = new CircularFifoQueue<>(padding);
    }

    @Override
    public void process(Double[] input) {

        Double maxInput = 0.0;
        for (int i = 0; i < input.length; i++) {
            maxInput = Math.max(Math.abs(input[i]), maxInput);
        }

        if (after < padding && maxInput <= threshold) {
            super.emitElement(input);
            after++;
        } else if (maxInput > threshold) {
            int numPrevious = Math.min(omitted, padding);
            for (int i = 0; i < numPrevious; i++) {
                emitElement(previousValues.get(padding - numPrevious + i));
            }
            emitElement(input);
            omitted = 0;
            after = 0;
        } else {
            omitted++;
        }

        previousValues.add(input);
    }
}
