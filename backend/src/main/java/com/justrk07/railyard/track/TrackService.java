package com.justrk07.railyard.track;

import com.justrk07.railyard.common.error.DuplicateResourceException;
import com.justrk07.railyard.common.error.ResourceNotFoundException;
import com.justrk07.railyard.track.dto.TrackRequest;
import com.justrk07.railyard.yard.YardRepository;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TrackService {

    private final TrackRepository tracks;
    private final YardRepository yards;

    public TrackService(TrackRepository tracks, YardRepository yards) {
        this.tracks = tracks;
        this.yards = yards;
    }

    @Transactional(readOnly = true)
    public Page<Track> list(UUID yardId, Pageable pageable) {
        return yardId == null
                ? tracks.findAllByOrderByYardIdAscCodeAsc(pageable)
                : tracks.findAllByYardIdOrderByCodeAsc(yardId, pageable);
    }

    @Transactional(readOnly = true)
    public Track get(UUID id) {
        return tracks.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Track " + id + " not found"));
    }

    @Transactional
    public Track create(UUID yardId, TrackRequest request) {
        if (!yards.existsById(yardId)) {
            throw new ResourceNotFoundException("Yard " + yardId + " not found");
        }
        tracks.findByYardIdAndCode(yardId, request.code()).ifPresent(existing -> {
            throw new DuplicateResourceException(
                    "Track code " + request.code() + " already exists in yard " + yardId);
        });
        return tracks.save(Track.create(
                yardId,
                request.code(),
                request.usableLengthMeters(),
                request.purpose(),
                request.status(),
                request.setupBufferMinutes(),
                request.clearanceBufferMinutes(),
                request.capabilities() == null ? Set.of() : request.capabilities()));
    }

    @Transactional
    public Track update(UUID id, TrackRequest request) {
        Track existing = get(id);
        if (!existing.getCode().equals(request.code())) {
            tracks.findByYardIdAndCode(existing.getYardId(), request.code()).ifPresent(other -> {
                if (!other.getId().equals(id)) {
                    throw new DuplicateResourceException(
                            "Track code " + request.code() + " already exists in yard " + existing.getYardId());
                }
            });
        }
        existing.update(
                request.code(),
                request.usableLengthMeters(),
                request.purpose(),
                request.status() == null ? existing.getStatus() : request.status(),
                request.setupBufferMinutes(),
                request.clearanceBufferMinutes(),
                request.capabilities());
        return existing;
    }

    @Transactional
    public Track changeStatus(UUID id, TrackStatus status) {
        Track existing = get(id);
        existing.changeStatus(status);
        return existing;
    }
}
