package de.volzo.tapper.GestureDetector.FSM;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static de.volzo.tapper.GestureDetector.FSM.FSMTap.tapTimeout;

/**
 * Created by tassilokarge on 23.11.16.
 */

public class FSMTapUnitTest extends FSMUnitTest {

    private FSMTap fsm;

    @Before
    public void setupFSM() {
        this.fsm = new FSMTap();
    }

    //success

    @Test
    public void tap() {
        Assert.assertEquals("fresh machine should be in INIT state",
                FSMTap.TapState.INIT, fsm.getCurrentTapState());

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
                FSMTap.TapState.INIT, fsm.getCurrentTapState());
        tap();
    }

    //failures

    @Test
    public void xyshake() {
        Assert.assertEquals("fresh machine should be in INIT state",
                FSMTap.TapState.INIT, fsm.getCurrentTapState());

        //should stay in init on XY-Shake
        toInitByXYShake();

        //from START state
        //Initial -> Start
        initialOrStartToStart();
        //Interrupt with XY-Shake
        toInitByXYShake();

        //from PEAK state
        //Initial -> Start
        initialOrStartToStart();
        //Start -> Peak
        startOrPeakToPeak();
        //Interrupt with XY-Shake
        toInitByXYShake();

        //from END state
        tap();
        toInitByXYShake();
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
        toInitByStrongZMovement();

        //from Peak
        //Initial -> Start
        initialOrStartToStart();
        //Start -> Peak
        startOrPeakToPeak();
        //Peak -> Init
        toInitByStrongZMovement();
    }


    private void peakToEndByNothing() {
        Assert.assertTrue("FSM should now be in end state",
                fsm.stateTransition(
                        EVENT_NOTHING
                ));
        Assert.assertEquals("Machine should now be in END state",
                FSMTap.TapState.END, fsm.getCurrentTapState());
    }

    private void startOrPeakToPeak() {
        //Start -> Peak or Peak -> Peak
        Assert.assertFalse("FSM should not be in end state yet",
                fsm.stateTransition(
                        EVENT_PEAK_Z
                ));
        Assert.assertEquals("Machine should now be in Peak state",
                FSMTap.TapState.PEAK, fsm.getCurrentTapState());
    }

    private void initialOrStartToStart() {
        //Initial -> Start or Start -> Start
        Assert.assertFalse("FSM should not be in end state yet",
                fsm.stateTransition(
                        EVENT_NOTHING
                ));
        Assert.assertEquals("Machine should now be in Start state",
                FSMTap.TapState.START, fsm.getCurrentTapState());
    }

    private void toInitByXYShake() {
        //Start -> Initial or Peak -> Initial
        Assert.assertFalse("FSM should not go to end state when shaking in X/Y axis",
                fsm.stateTransition(
                        EVENT_PEAK_XY
                ));
        Assert.assertEquals("Machine should now be in INIT state",
                FSMTap.TapState.INIT, fsm.getCurrentTapState());
    }

    private void toInitByStrongZMovement() {
        //Start -> Initial or Peak -> Initial
        Assert.assertFalse("FSM should not go to end state when shaking in X/Y axis",
                fsm.stateTransition(
                        EVENT_STRONG_PEAK_Z
                ));
        Assert.assertEquals("Machine should now be in INIT state",
                FSMTap.TapState.INIT, fsm.getCurrentTapState());
    }

    private void toInitByTimeout() throws InterruptedException {
        //timeout can only occur on PEAK state
        Assert.assertEquals("FSM has to be in PEAK state for timeout",
                FSMTap.TapState.PEAK, fsm.getCurrentTapState());
        //timeout should occur after 15ms, additional 5ms account for threading delays
        Thread.sleep(tapTimeout + 5);
        Assert.assertEquals("Machine should now be in INIT state",
                FSMTap.TapState.INIT, fsm.getCurrentTapState());
    }

    @After
    public void tearDownFSM() {
        this.fsm = null;
    }
}
