package de.volzo.tapper.GestureDetector.FSM;

import de.volzo.tapper.GestureDetector.Detector;
import de.volzo.tapper.GestureDetector.Quantile;

/**
 * Created by tassilokarge on 24.11.16.
 */
public class FSMUnitTest {
    public static final int EVENT_NOTHING = Quantile.NOTHING.getMask() << Detector.Shift.X.getShift()
            | Quantile.NOTHING.getMask() << Detector.Shift.Y.getShift()
            | Quantile.NOTHING.getMask() << Detector.Shift.Z.getShift();
    public static final int EVENT_PEAK_X = Quantile.PEAK.getMask() << Detector.Shift.X.getShift()
            | Quantile.NOTHING.getMask() << Detector.Shift.Y.getShift()
            | Quantile.NOTHING.getMask() << Detector.Shift.Z.getShift();
    public static final int EVENT_PEAK_Y = Quantile.NOTHING.getMask() << Detector.Shift.X.getShift()
            | Quantile.PEAK.getMask() << Detector.Shift.Y.getShift()
            | Quantile.NOTHING.getMask() << Detector.Shift.Z.getShift();
    public static final int EVENT_PEAK_Z = Quantile.NOTHING.getMask() << Detector.Shift.X.getShift()
            | Quantile.NOTHING.getMask() << Detector.Shift.Y.getShift()
            | Quantile.PEAK.getMask() << Detector.Shift.Z.getShift();
    public static final int EVENT_PEAK_XY = Quantile.PEAK.getMask() << Detector.Shift.X.getShift()
            | Quantile.PEAK.getMask() << Detector.Shift.Y.getShift()
            | Quantile.NOTHING.getMask() << Detector.Shift.Z.getShift();
    public static final int EVENT_STRONG_PEAK_X = Quantile.STRONG_PEAK.getMask() << Detector.Shift.X.getShift()
            | Quantile.NOTHING.getMask() << Detector.Shift.Y.getShift()
            | Quantile.NOTHING.getMask() << Detector.Shift.Z.getShift();
    public static final int EVENT_STRONG_PEAK_Y = Quantile.NOTHING.getMask() << Detector.Shift.X.getShift()
            | Quantile.STRONG_PEAK.getMask() << Detector.Shift.Y.getShift()
            | Quantile.NOTHING.getMask() << Detector.Shift.Z.getShift();
    public static final int EVENT_STRONG_PEAK_Z = Quantile.NOTHING.getMask() << Detector.Shift.X.getShift()
            | Quantile.NOTHING.getMask() << Detector.Shift.Y.getShift()
            | Quantile.STRONG_PEAK.getMask() << Detector.Shift.Z.getShift();
    public static final int EVENT_STRONG_PEAK_XY = Quantile.STRONG_PEAK.getMask() << Detector.Shift.X.getShift()
            | Quantile.STRONG_PEAK.getMask() << Detector.Shift.Y.getShift()
            | Quantile.NOTHING.getMask() << Detector.Shift.Z.getShift();
    public static final int EVENT_VERY_STRONG_PEAK_X = Quantile.VERY_STRONG_PEAK.getMask() << Detector.Shift.X.getShift()
            | Quantile.NOTHING.getMask() << Detector.Shift.Y.getShift()
            | Quantile.NOTHING.getMask() << Detector.Shift.Z.getShift();
    public static final int EVENT_VERY_STRONG_PEAK_Y = Quantile.NOTHING.getMask() << Detector.Shift.X.getShift()
            | Quantile.VERY_STRONG_PEAK.getMask() << Detector.Shift.Y.getShift()
            | Quantile.NOTHING.getMask() << Detector.Shift.Z.getShift();
    public static final int EVENT_VERY_STRONG_PEAK_Z = Quantile.NOTHING.getMask() << Detector.Shift.X.getShift()
            | Quantile.NOTHING.getMask() << Detector.Shift.Y.getShift()
            | Quantile.VERY_STRONG_PEAK.getMask() << Detector.Shift.Z.getShift();
    public static final int EVENT_VERY_STRONG_PEAK_XY = Quantile.VERY_STRONG_PEAK.getMask() << Detector.Shift.X.getShift()
            | Quantile.VERY_STRONG_PEAK.getMask() << Detector.Shift.Y.getShift()
            | Quantile.NOTHING.getMask() << Detector.Shift.Z.getShift();
    public static final int EVENT_TAP = 1 << Detector.Shift.TAP.getShift();
}
