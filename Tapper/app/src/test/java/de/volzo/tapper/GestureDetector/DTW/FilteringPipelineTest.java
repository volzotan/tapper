package de.volzo.tapper.GestureDetector.DTW;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by tassilokarge on 07.12.16.
 */
public class FilteringPipelineTest {

    @Test
    public void process() throws Exception {

        ArrayList<Integer[]> processed = new ArrayList<>();
        FilteringPipeline fp = new FilteringPipeline(processed::add);

        fp.process(new Double[]{0.0,0.0,0.0});
        fp.process(new Double[]{0.0,0.0,0.0});
        fp.process(new Double[]{0.3,0.3,0.3});
        fp.process(new Double[]{0.5,0.5,0.5});
        fp.process(new Double[]{1.2,1.2,1.2});
        fp.process(new Double[]{3.0,3.0,3.0});
        fp.process(new Double[]{5.0,5.0,5.0});
        fp.process(new Double[]{8.0,8.0,8.0});
        fp.process(new Double[]{5.0,5.0,5.0});
        fp.process(new Double[]{3.0,3.0,3.0});
        fp.process(new Double[]{1.2,1.2,1.2});
        fp.process(new Double[]{0.5,0.5,0.5});
        fp.process(new Double[]{0.3,0.3,0.3});
        fp.process(new Double[]{0.0,0.0,0.0});
        fp.process(new Double[]{0.0,0.0,0.0});

        System.out.println(Arrays.deepToString(processed.toArray()));
    }

}