package com.justrk07.railyard.train;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TrainRepository extends JpaRepository<Train, UUID> {

    Optional<Train> findByTrainNumber(String trainNumber);

    Page<Train> findAllByOrderByTrainNumberAsc(Pageable pageable);

    @Query("""
            SELECT t FROM Train t
            WHERE LOWER(t.trainNumber) LIKE LOWER(CONCAT('%', :query, '%'))
            ORDER BY t.trainNumber ASC
            """)
    Page<Train> searchByNumber(@Param("query") String query, Pageable pageable);
}
