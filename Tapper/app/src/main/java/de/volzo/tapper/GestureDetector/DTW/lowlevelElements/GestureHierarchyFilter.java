package de.volzo.tapper.GestureDetector.DTW.lowlevelElements;

import java.util.Arrays;
import java.util.List;

import de.volzo.tapper.GestureDetector.DTW.streamSystem.StreamPassthrough;
import de.volzo.tapper.GestureDetector.DTW.streamSystem.StreamReceiver;
import de.volzo.tapper.GestureDetector.GestureType;

import static de.volzo.tapper.GestureDetector.GestureType.DOUBLETAP;
import static de.volzo.tapper.GestureDetector.GestureType.NOTHING;
import static de.volzo.tapper.GestureDetector.GestureType.PICKUPDROP;
import static de.volzo.tapper.GestureDetector.GestureType.SHAKE;
import static de.volzo.tapper.GestureDetector.GestureType.SIDETAP;
import static de.volzo.tapper.GestureDetector.GestureType.TAP;

/**
 * Created by tassilokarge on 08.12.16.
 */

public class GestureHierarchyFilter extends StreamPassthrough<GestureType, GestureType> {

    private final List<GestureType> hierarchy = Arrays.asList(
            NOTHING,
            SIDETAP,
            TAP,
            SHAKE,
            DOUBLETAP,
            PICKUPDROP
    );

    private GestureType previousGestureType;
    private boolean emitted = false;

    public GestureHierarchyFilter(StreamReceiver<GestureType> receiver) {
        super(receiver);
    }

    @Override
    public void process(GestureType input) {
        GestureType filteredType = filterWithHierarchy(input);
        //System.out.println(
        //        "Type: " + input.name()
        //        + ", filtered: " + (filteredType == null ? "null" : filteredType.name()) + ", emitted: " + emitted);
        if (filteredType != null) {
            emitElement(filteredType);
        }
    }

    private GestureType filterWithHierarchy(GestureType type) {

        if (rank(type) == rank(hierarchy.get(hierarchy.size() - 1))) {
            //immediately emit highest ranked element
            previousGestureType = type;
            emitted = true;
            return type;
        }
        if (rank(type) > rank(previousGestureType)) {
            //replace highest ranked gesture
            previousGestureType = type;
            emitted = false;
            return null;
        } else if (rank(type) == rank(previousGestureType)) {
            if (rank(type) == 0) {
                //emit nothing
                //previous gesture was also null
                //emitted must have been false
                return null;
            } else {
                //emit gesture
                //previous gesture is equal
                emitted = true;
                return type;
            }
        } else {
            if (!emitted) {
                //emit gesture
                //previous gesture remains high
                emitted = true;
                return previousGestureType;
            } else if (rank(type) == 0) {
                //emit nothing, reset
                previousGestureType = type;
                emitted = false;
                return null;
            } else {
                //rank has not dropped to nothing yet
                //previous gesture remains high
                //emitted stays true
                return null;
            }
        }
    }

    private int rank(GestureType type) {
        return hierarchy.indexOf(type);
    }
}
