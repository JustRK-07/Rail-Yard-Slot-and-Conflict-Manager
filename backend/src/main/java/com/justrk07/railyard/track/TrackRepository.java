package com.justrk07.railyard.track;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrackRepository extends JpaRepository<Track, UUID> {

    Page<Track> findAllByYardIdOrderByCodeAsc(UUID yardId, Pageable pageable);

    Page<Track> findAllByOrderByYardIdAscCodeAsc(Pageable pageable);

    Optional<Track> findByYardIdAndCode(UUID yardId, String code);
}
