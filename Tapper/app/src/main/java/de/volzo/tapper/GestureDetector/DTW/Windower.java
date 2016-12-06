package de.volzo.tapper.GestureDetector.DTW;

import org.apache.commons.collections4.queue.CircularFifoQueue;

/**
 * Created by tassilokarge on 04.12.16.
 */

public class Windower extends StreamElement<Double[]> {
    /** window size in ms */
    public static final int WINDOW_SIZE_MS = 2000;
    /** the shift of the sliding window in ms */
    public static final int WINDOW_SHIFT_MS = 1000;

    /** the number of samples per second */
    public static final int SAMPLES_PER_SEC = 100;

    /** the window size in samples */
    private static final int WINDOW_SIZE_SAMPLES = (WINDOW_SIZE_MS * SAMPLES_PER_SEC) / 1000;
    /** the shift size in samples */
    private static final int WINDOW_SHIFT_SAMPLES = (WINDOW_SHIFT_MS * SAMPLES_PER_SEC) / 1000;

    /** a fifo quque that holds the samples within WINDOW_SIZE_MS ms */
    private CircularFifoQueue<Double> data = new CircularFifoQueue<>(WINDOW_SIZE_SAMPLES);

    /** samples until next update countdown */
    private int updateCountdown = WINDOW_SHIFT_SAMPLES;

    public Windower(Consumer<Double[]> windowConsumer) {
        super(windowConsumer);
    }

    public void addDataPoint(Double dataPoint) {
        data.add(dataPoint);
        updateCountdown--;
        final Double[] d = new Double[0];
        if (updateCountdown == 0) {
            super.passProcessedElement(data.toArray(d));
            updateCountdown = WINDOW_SHIFT_SAMPLES;
        }
    }
}
