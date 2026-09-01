package org.example.itemcounting.repository;

import org.example.itemcounting.entity.DeliveryTimeCoefficient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalTime;
import java.util.Optional;

public interface DeliveryTimeCoefficientRepository extends JpaRepository<DeliveryTimeCoefficient, Long> {

    @Query("SELECT dtc FROM DeliveryTimeCoefficient dtc " +
            "WHERE :time BETWEEN dtc.startTime AND dtc.endTime")
    Optional<DeliveryTimeCoefficient> findByTime(@Param("time") LocalTime time);
}
