package de.volzo.tapper.GestureDetector.FSM;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import de.volzo.tapper.GestureDetector.Detector;

import static de.volzo.tapper.GestureDetector.FSM.PickUpDropFSM.PickUpDropState.END;
import static de.volzo.tapper.GestureDetector.FSM.PickUpDropFSM.PickUpDropState.INIT;

/**
 * Created by tassilokarge on 25.11.16.
 */

public class PickUpDropFSM {

    public static long pickUpTimeout = 500;
    public static long dropTimeout = 20;

    ScheduledExecutorService pickUpTimer;
    ScheduledExecutorService dropTimer;

    private PickUpDropState currentPickUpDropState;

    //transition event matrix
    //transition if exact mask (event ^ transition == 0)
    //1 = nothing, 2 = peak, 4 = strong peak, 8 = very strong peak
    //in hightest digit: 1 = timeout
    int[][] tapTransitionXNOR = new int[][]{
            /*INIT */{0x0000, 0x0111, 0x0000, 0x0000, 0x0000},
            /*START*/{0x0000, 0x0111, 0x0114, 0x0000, 0x0000},
            /*PEAK */{0x1000, 0x0000, 0x0114, 0x0118, 0x0000},
            /*PEAK2*/{0x1000, 0x0000, 0x0000, 0x0118, 0x0111},
            /*END  */{0x0000, 0x0000, 0x0000, 0x0000, 0x0000}};

    //transition event matrix
    //transition if any overlap (event & transition != 0)
    //F = everything, E = more than nothing, C = more than peak, 8 = very strong peak, 6 = peak or strong peak, 4 = strong peak
    int[][] tapTransitionAND = new int[][]{
            /*INIT */{0xFEEE, 0x0000, 0x0000, 0x0000, 0x0000},
            /*START*/{0xFCC8, 0x0000, 0x0006, 0x0000, 0x0000},
            /*PEAK */{0xFCC0, 0x0000, 0x0006, 0x0008, 0x0000},
            /*PEAK2*/{0xFCC0, 0x0000, 0x0000, 0x000E, 0x0000},
            /*END  */{0xFFFF, 0x0000, 0x0000, 0x0000, 0x0000}};

    //Tasks executed on entering some state
    Runnable[] stateEnterAction = new Runnable[] {
            () -> {},
            () -> {},
            () -> {
                pickUpTimer = Executors.newSingleThreadScheduledExecutor();
                pickUpTimer.schedule(() -> this.stateTransition(
                        1 << Detector.Shift.TIMEOUT.getShift()),
                        pickUpTimeout, TimeUnit.MILLISECONDS);
            },
            () -> {
                dropTimer = Executors.newSingleThreadScheduledExecutor();
                dropTimer.schedule(() -> this.stateTransition(
                        1 << Detector.Shift.TIMEOUT.getShift()),
                        dropTimeout, TimeUnit.MILLISECONDS);
            },
            () -> {}};

    //Tasks executed on exiting some state
    Runnable[] stateExitAction = new Runnable[] {
            () -> {},
            () -> {},
            () -> {
                pickUpTimer.shutdownNow();
                pickUpTimer = null;
            },
            () -> {
                dropTimer.shutdownNow();
                dropTimer = null;
            },
            () -> {}};

    public enum PickUpDropState {
        INIT(0), START(1), PEAK(2), PEAK2(3), END(4);

        private int state;

        PickUpDropState(int state) {
            this.state = state;
        }

        public int getState() {
            return state;
        }

        public static PickUpDropState fromValue(int value) {
            {
                switch (value) {
                    case 0:
                        return INIT;
                    case 1:
                        return START;
                    case 2:
                        return PEAK;
                    case 3:
                        return PEAK2;
                    default:
                        return END;
                }
            }
        }
    }

    public final PickUpDropState finalState = END;

    public PickUpDropFSM() {
        this.currentPickUpDropState = INIT;
    }

    public void reset() {
        if (currentPickUpDropState != INIT) {
            stateExitAction[currentPickUpDropState.getState()].run();
        }
        this.currentPickUpDropState = INIT;
    }

    public boolean stateTransition(int event) {

        PickUpDropState formerTapState = null;
        PickUpDropState newTapState = null;

        for (int i = 0; i < tapTransitionAND.length; i++) {
            if ((event ^ tapTransitionXNOR[currentPickUpDropState.getState()][i]) == 0) {
                formerTapState = currentPickUpDropState;
                newTapState = PickUpDropState.fromValue(i);
                break;
            } else if ((event & tapTransitionAND[currentPickUpDropState.getState()][i]) != 0) {
                formerTapState = currentPickUpDropState;
                newTapState = PickUpDropState.fromValue(i);
                break;
            }
        }

        if (formerTapState != null && newTapState != currentPickUpDropState) {
            stateExitAction[formerTapState.getState()].run();
            currentPickUpDropState = newTapState;
            stateEnterAction[newTapState.getState()].run();
        }

        return currentPickUpDropState == finalState;
    }

    public PickUpDropState getCurrentPickUpDropState() {
        return currentPickUpDropState;
    }
}
