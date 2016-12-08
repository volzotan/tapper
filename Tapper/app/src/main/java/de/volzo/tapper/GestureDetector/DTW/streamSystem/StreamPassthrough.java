package de.volzo.tapper.GestureDetector.DTW.streamSystem;

/**
 * Created by tassilokarge on 07.12.16.
 */

public abstract class StreamPassthrough<T,U> extends StreamEmitter<T> implements StreamReceiver<U> {
    public StreamPassthrough(StreamReceiver<T> receiver) {
        super(receiver);
    }
}
