package de.volzo.tapper.GestureDetector;

import java.util.HashMap;

/**
 * Created by volzotan on 11.11.16.
 */
public enum GestureType {
    NOTHING, //only used internally
    DOUBLETAP,
    SIDETAP,
    TAP, //only used internally
    PICKUPDROP,
    SHAKE;

    static final HashMap<GestureType, String> displayNames = new HashMap<GestureType, String>() {{
        put(DOUBLETAP, "Double Tap");
        put(SIDETAP, "Side Tap");
        put(PICKUPDROP, "Pick up, Drop");
        put(SHAKE, "Shake");
    }};

    static final HashMap<GestureType, String> descriptions = new HashMap<GestureType, String>() {{
        put(DOUBLETAP, "Knocking with the knuckles twice");
        put(SIDETAP, "A quick slap on the side");
        put(PICKUPDROP, "Picking up and dropping, like you want to get rid of the ringing of an old analogue phone");
        put(SHAKE, "Shake for three or four times, like waking someone up");
    }};

    public GestureType[] getAllPublicGestureTypes() {
        return new GestureType[]{DOUBLETAP, SIDETAP, PICKUPDROP, SHAKE};
    }

    public String getDescription(GestureType type) {
        return descriptions.get(type);
    }

    public String getDisplayName(GestureType type) {
        return descriptions.get(type);
    }
}
