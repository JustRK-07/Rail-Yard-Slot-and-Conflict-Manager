package com.justrk07.railyard.yard;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface YardRepository extends JpaRepository<Yard, UUID> {

    Optional<Yard> findByCode(String code);

    Page<Yard> findAllByOrderByCodeAsc(Pageable pageable);
}
