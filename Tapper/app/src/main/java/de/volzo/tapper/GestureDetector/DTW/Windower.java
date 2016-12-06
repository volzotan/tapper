package de.volzo.tapper.GestureDetector.DTW;

import org.apache.commons.collections4.queue.CircularFifoQueue;

/**
 * Created by tassilokarge on 04.12.16.
 */

public class Windower extends StreamElement<Double[]> {
    /** window size in ms */
    public static final int WINDOW_SIZE = 2000;
    /** the shift of the sliding window in ms */
    public static final int WINDOW_SHIFT = 200;
    /** the number of samples per second */
    public static final int SAMPLING_FREQUENCY = 100;

    private CircularFifoQueue<Double> data = new CircularFifoQueue<>(WINDOW_SIZE);

    private int updateCountdown = (WINDOW_SHIFT*SAMPLING_FREQUENCY)/1000;

    public Windower(Consumer<Double[]> windowConsumer) {
        super(windowConsumer);
    }

    public void addDataPoint(Double dataPoint) {
        data.add(dataPoint);
        updateCountdown--;
        final Double[] d = new Double[0];
        if (updateCountdown == 0) {
            super.passProcessedElement(data.toArray(d));
            updateCountdown = (WINDOW_SHIFT * SAMPLING_FREQUENCY)/1000;
        }
    }
}
