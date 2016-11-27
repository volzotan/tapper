package de.volzo.tapper.GestureDetector.FSM;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static de.volzo.tapper.GestureDetector.FSM.SideTapFSM.sideTapTimeout;

/**
 * Created by tassilokarge on 23.11.16.
 */

public class SideTapFSMUnitTest extends FSMUnitTest {

    private SideTapFSM fsm;

    @Before
    public void setupFSM() {
        this.fsm = new SideTapFSM();
    }

    //success

    @Test
    public void tap() {
        Assert.assertEquals("fresh machine should be in INIT state",
                SideTapFSM.SideTapState.INIT, fsm.getCurrentSideTapState());

        //Initial -> Start
        initialOrStartToStart();
        //Start -> Start
        initialOrStartToStart();

        //Start -> Peak
        startOrPeakToPeak();
        //Peak -> Peak
        startOrPeakToPeak();

        //Peak -> End
        peakToEndByNothing();
    }


    @Test
    public void twoTaps() {
        tap();
        //any possible state transition should lead from END to INIT state
        Assert.assertFalse("FSM should go to INIT after first tap", fsm.stateTransition(1));
        Assert.assertEquals("FSM should go to INIT after first tap",
                SideTapFSM.SideTapState.INIT, fsm.getCurrentSideTapState());
        tap();
    }

    //failures

    @Test
    public void zShake() {
        Assert.assertEquals("fresh machine should be in INIT state",
                SideTapFSM.SideTapState.INIT, fsm.getCurrentSideTapState());

        //should stay in init on XY-Shake
        toInitByVeryStrongZShake();

        //from START state
        //Initial -> Start
        initialOrStartToStart();
        //Interrupt with XY-Shake
        toInitByVeryStrongZShake();

        //from PEAK state
        //Initial -> Start
        initialOrStartToStart();
        //Start -> Peak
        startOrPeakToPeak();
        //Interrupt with XY-Shake
        toInitByVeryStrongZShake();

        //from END state
        tap();
        toInitByVeryStrongZShake();
    }

    @Test
    public void timeout() throws InterruptedException {
        //Initial -> Start
        initialOrStartToStart();
        //Start -> Peak
        startOrPeakToPeak();
        //Peak -> Init
        toInitByTimeout();
    }

    @Test
    public void tooStrongTap() {
        //from Start state
        //Initial -> Start
        initialOrStartToStart();
        //Start -> Peak
        toInitByVeryStrongXYMovement();

        //from Peak
        //Initial -> Start
        initialOrStartToStart();
        //Start -> Peak
        startOrPeakToPeak();
        //Peak -> Init
        toInitByVeryStrongXYMovement();
    }


    private void peakToEndByNothing() {
        Assert.assertTrue("FSM should now be in end state",
                fsm.stateTransition(
                        EVENT_NOTHING
                ));
        Assert.assertEquals("Machine should now be in END state",
                SideTapFSM.SideTapState.END, fsm.getCurrentSideTapState());
    }

    private void startOrPeakToPeak() {
        //Start -> Peak or Peak -> Peak
        Assert.assertFalse("FSM should not be in end state yet",
                fsm.stateTransition(
                        EVENT_PEAK_XY
                ));
        Assert.assertEquals("Machine should now be in Peak state",
                SideTapFSM.SideTapState.PEAK, fsm.getCurrentSideTapState());
    }

    private void initialOrStartToStart() {
        //Initial -> Start or Start -> Start
        Assert.assertFalse("FSM should not be in end state yet",
                fsm.stateTransition(
                        EVENT_NOTHING
                ));
        Assert.assertEquals("Machine should now be in Start state",
                SideTapFSM.SideTapState.START, fsm.getCurrentSideTapState());
    }

    private void toInitByVeryStrongZShake() {
        //Start -> Initial or Peak -> Initial
        Assert.assertFalse("FSM should not go to end state when shaking in X/Y axis",
                fsm.stateTransition(
                        EVENT_VERY_STRONG_PEAK_Z
                ));
        Assert.assertEquals("Machine should now be in INIT state",
                SideTapFSM.SideTapState.INIT, fsm.getCurrentSideTapState());
    }

    private void toInitByVeryStrongXYMovement() {
        //Start -> Initial or Peak -> Initial
        Assert.assertFalse("FSM should not go to end state when shaking in X/Y axis",
                fsm.stateTransition(
                        EVENT_VERY_STRONG_PEAK_XY
                ));
        Assert.assertEquals("Machine should now be in INIT state",
                SideTapFSM.SideTapState.INIT, fsm.getCurrentSideTapState());
    }

    private void toInitByTimeout() throws InterruptedException {
        //timeout can only occur on PEAK state
        Assert.assertEquals("FSM has to be in PEAK state for timeout",
                SideTapFSM.SideTapState.PEAK, fsm.getCurrentSideTapState());
        //timeout should occur after 15ms, additional 5ms account for threading delays
        Thread.sleep(sideTapTimeout + 5);
        Assert.assertEquals("Machine should now be in INIT state",
                SideTapFSM.SideTapState.INIT, fsm.getCurrentSideTapState());
    }

    @After
    public void tearDownFSM() {
        this.fsm = null;
    }
}
