package de.volzo.tapper.GestureDetector.FSM;

/**
 * Created by tassilokarge on 23.11.16.
 */

public enum Quantile {
    NOTHING(1), PEAK(1 << 1), STRONG_PEAK(1 << 2), VERY_STRONG_PEAK(1 << 3);

    private int mask;

    Quantile(int mask) {
        this.mask = mask;
    }

    public int getMask() {
        return mask;
    }
}
