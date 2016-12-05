# Considerations for Android Tap Detection App

## Gesture vocabulary

- Tap on back
- Doubletap on back
- Sidetap
- Pick up and drop

## Recognition

Pipeline: Accelerometer -> Collector -> Low pass filtering / Averaging -> Envelope Detector -> Quantization -> Detector

### Collection

Collection of 128 samples in circular queue

### Low pass filtering

Smoothing with low-pass filter

### Averaging

Moving average filter with non-linear kernel 
[0.3, 0.3, 0.5, 0.8, 0.8, 0.5]

Removes high frequencies and zero points for envelope detection

### Envelope curve

Detects the envelope of the signal

### Quantization

3 categories:
TODO: limits

- Z
    + nothing: <= a
    + peak: > a, <=b
    + strong peak: > b, <= c
    + very strong peak: > c

- X/Y
    + nothing: <= a
    + peak: > a, <=b
    + strong peak: > b, <= c
    + very strong peak: > c

### Gesture detection

State machine for each gesture

* Tap
    1. nothing
    2. nothing in X/Y axis and peak (<= a ms) in Z axis
    3. nothing
* Doubletap
    1. Tap
    2. nothing (< b ms)
    3. Tap
* Sidetap
    1. nothing
    2. peak or strong peak on X or Y axis or both (< c ms)
    3. nothing
* Pick up and drop
    1. nothing
    2. strong peak on Z axis (> d ms)
    3. very strong peak on Z axis (< e ms)
    4. nothing
