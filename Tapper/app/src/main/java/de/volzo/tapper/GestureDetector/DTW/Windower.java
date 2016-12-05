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

    private int updateCountdown = (WINDOW_SIZE*SAMPLING_FREQUENCY)/1000;

    public Windower(Consumer<Double[]> windowConsumer) {
        super(windowConsumer);
    }

    public void updateData(Double dataPoint) {
        data.add(dataPoint);
        updateCountdown--;
        if (updateCountdown == 0) {
            super.passProcessedElement((Double[]) data.toArray());
            updateCountdown = (WINDOW_SHIFT * SAMPLING_FREQUENCY)/1000;
        }
    }
}
