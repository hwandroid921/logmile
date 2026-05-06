package com.project.logmile.domain.plateevent.repository;

import com.project.logmile.domain.plateevent.entity.PlateEvent;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlateEventRepository extends JpaRepository<PlateEvent, Long> {

	List<PlateEvent> findByPlateNoOrderByObservedAtDesc(String plateNo);
}
