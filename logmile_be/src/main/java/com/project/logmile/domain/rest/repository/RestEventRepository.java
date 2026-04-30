package com.project.logmile.domain.rest.repository;

import com.project.logmile.domain.rest.entity.RestEvent;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RestEventRepository extends JpaRepository<RestEvent, Long> {

	List<RestEvent> findByDriveLogIdOrderByRestStartedAtAsc(Long driveLogId);
}
