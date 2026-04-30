package com.project.logmile.domain.driver.repository;

import com.project.logmile.domain.driver.entity.Driver;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DriverRepository extends JpaRepository<Driver, Long> {

	List<Driver> findByCompanyId(Long companyId);

	Optional<Driver> findByVehicleId(Long vehicleId);
}
