package de.volzo.tapper.GestureDetector.DTW;

/**
 * Created by tassilokarge on 07.12.16.
 */

public abstract class StreamPassthrough<T,U> extends StreamEmitter<T> implements StreamReceiver<U> {
    StreamPassthrough(StreamReceiver<T> receiver) {
        super(receiver);
    }
}
