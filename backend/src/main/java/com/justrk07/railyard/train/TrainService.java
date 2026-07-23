package com.justrk07.railyard.train;

import com.justrk07.railyard.common.error.DuplicateResourceException;
import com.justrk07.railyard.common.error.ResourceNotFoundException;
import com.justrk07.railyard.train.dto.TrainRequest;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TrainService {

    private final TrainRepository trains;

    public TrainService(TrainRepository trains) {
        this.trains = trains;
    }

    @Transactional(readOnly = true)
    public Page<Train> list(String query, Pageable pageable) {
        if (query == null || query.isBlank()) {
            return trains.findAllByOrderByTrainNumberAsc(pageable);
        }
        return trains.searchByNumber(query.trim(), pageable);
    }

    @Transactional(readOnly = true)
    public Train get(UUID id) {
        return trains.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Train " + id + " not found"));
    }

    @Transactional
    public Train create(TrainRequest request) {
        trains.findByTrainNumber(request.trainNumber()).ifPresent(existing -> {
            throw new DuplicateResourceException(
                    "Train number " + existing.getTrainNumber() + " already exists");
        });
        return trains.save(Train.create(
                request.trainNumber(),
                request.lengthMeters(),
                request.serviceType(),
                request.priority(),
                request.origin(),
                request.destination(),
                request.requiredCapabilities()));
    }

    @Transactional
    public Train update(UUID id, TrainRequest request) {
        Train existing = get(id);
        if (!existing.getTrainNumber().equals(request.trainNumber())) {
            trains.findByTrainNumber(request.trainNumber()).ifPresent(other -> {
                if (!other.getId().equals(id)) {
                    throw new DuplicateResourceException(
                            "Train number " + request.trainNumber() + " already exists");
                }
            });
        }
        existing.update(
                request.trainNumber(),
                request.lengthMeters(),
                request.serviceType(),
                request.priority(),
                request.origin(),
                request.destination(),
                request.active() == null ? existing.isActive() : request.active(),
                request.requiredCapabilities());
        return existing;
    }
}
