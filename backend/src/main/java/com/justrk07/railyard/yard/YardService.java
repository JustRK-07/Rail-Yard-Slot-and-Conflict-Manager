package com.justrk07.railyard.yard;

import com.justrk07.railyard.common.error.DuplicateResourceException;
import com.justrk07.railyard.common.error.ResourceNotFoundException;
import com.justrk07.railyard.yard.dto.YardRequest;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class YardService {

    private final YardRepository yards;

    public YardService(YardRepository yards) {
        this.yards = yards;
    }

    @Transactional(readOnly = true)
    public Page<Yard> list(Pageable pageable) {
        return yards.findAllByOrderByCodeAsc(pageable);
    }

    @Transactional(readOnly = true)
    public Yard get(UUID id) {
        return yards.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Yard " + id + " not found"));
    }

    @Transactional
    public Yard create(YardRequest request) {
        yards.findByCode(request.code()).ifPresent(existing -> {
            throw new DuplicateResourceException("Yard code " + existing.getCode() + " already exists");
        });
        return yards.save(Yard.create(
                request.code(),
                request.name(),
                request.location(),
                request.timeZone()));
    }

    @Transactional
    public Yard update(UUID id, YardRequest request) {
        Yard existing = get(id);
        existing.update(
                request.name(),
                request.location(),
                request.timeZone(),
                request.active() == null ? existing.isActive() : request.active());
        return existing;
    }
}
