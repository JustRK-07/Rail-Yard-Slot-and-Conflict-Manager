package com.justrk07.railyard.train;

import com.justrk07.railyard.track.TrackCapability;

/**
 * A capability referenced by a train. The enum values come from
 * {@link TrackCapability}; this wrapper exists so the train module can
 * depend on the enumeration without introducing a cycle through the track
 * module.
 */
public enum TrackCapabilityRef {
    DIESEL,
    ELECTRIFIED,
    HEAVY_FREIGHT,
    HAZARDOUS_MATERIAL,
    LOADING,
    PASSENGER;

    public static TrackCapabilityRef from(TrackCapability capability) {
        return TrackCapabilityRef.valueOf(capability.name());
    }

    public TrackCapability toTrackCapability() {
        return TrackCapability.valueOf(name());
    }
}
