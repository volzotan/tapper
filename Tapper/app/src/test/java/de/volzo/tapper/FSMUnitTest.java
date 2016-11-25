package de.volzo.tapper;

import de.volzo.tapper.GestureDetector.Detector;
import de.volzo.tapper.GestureDetector.Quantile;

/**
 * Created by tassilokarge on 24.11.16.
 */
public class FSMUnitTest {
    public static final int EVENT_NOTHING = Quantile.NOTHING.getMask() << Detector.Shift.X.getShift()
            | Quantile.NOTHING.getMask() << Detector.Shift.Y.getShift()
            | Quantile.NOTHING.getMask() << Detector.Shift.Z.getShift();
    public static final int EVENT_ONLY_Z_PEAK = Quantile.NOTHING.getMask() << Detector.Shift.X.getShift()
            | Quantile.NOTHING.getMask() << Detector.Shift.Y.getShift()
            | Quantile.PEAK.getMask() << Detector.Shift.Z.getShift();
    public static final int EVENT_XY_SHAKE = Quantile.PEAK.getMask() << Detector.Shift.X.getShift()
            | Quantile.PEAK.getMask() << Detector.Shift.Y.getShift()
            | Quantile.NOTHING.getMask() << Detector.Shift.Z.getShift();
    public static final int EVENT_STRONG_Z_PEAK = Quantile.NOTHING.getMask() << Detector.Shift.X.getShift()
            | Quantile.NOTHING.getMask() << Detector.Shift.Y.getShift()
            | Quantile.STRONG_PEAK.getMask() << Detector.Shift.Z.getShift();
    public static final int EVENT_TAP = 1 << Detector.Shift.TAP.getShift();
}
