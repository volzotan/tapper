package de.volzo.tapper.GestureDetector.DTW.lowlevelElements;

import org.apache.commons.collections4.queue.CircularFifoQueue;

import java.lang.reflect.Array;

import de.volzo.tapper.GestureDetector.DTW.streamSystem.StreamPassthrough;
import de.volzo.tapper.GestureDetector.DTW.streamSystem.StreamReceiver;

/**
 * Created by tassilokarge on 04.12.16.
 */

public class Windower<T> extends StreamPassthrough<T[], T> {

    /** window size in ms */
    private final int windowSizeMs;
    /** the shift of the sliding window in ms */
    private final int windowShiftMs;

    /** the number of samples per second */
    private final int samplesPerSec;

    /** the window size in samples */
    private final int windowSizeSamples;
    /** the shift size in samples */
    private final int windowShiftSamples;

    /** a fifo quque that holds the samples within windowSizeMs ms */
    private CircularFifoQueue<T> data;

    /** samples until next update countdown */
    private int updateCountdown;

    public Windower(int windowSizeMs, int windowShiftMs, int samplesPerSec, StreamReceiver<T[]> windowStreamReceiver) {
        super(windowStreamReceiver);
        this.windowSizeMs = windowSizeMs;
        this.windowShiftMs = windowShiftMs;
        this.samplesPerSec = samplesPerSec;
        this.windowSizeSamples = (windowSizeMs * samplesPerSec) / 1000;
        this.windowShiftSamples = (windowShiftMs * samplesPerSec) / 1000;
        data = new CircularFifoQueue<>(windowSizeSamples);
        resetCountdown();
    }

    @Override
    public void process(T input) {
        T[] window = addDataPoint(input);
        if (window != null) {
            super.emitElement(window);
        }
    }

    private T[] addDataPoint(T dataPoint) {
        data.add(dataPoint);
        updateCountdown--;
        T[] window = null;
        if (updateCountdown == 0) {
            T[] d =  (T[]) Array.newInstance(dataPoint.getClass(), 0);
            window = data.toArray(d);
            resetCountdown();
        }
        return window;
    }

    private void resetCountdown() {
        updateCountdown = windowShiftSamples;
    }
}
