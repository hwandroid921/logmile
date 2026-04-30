package com.project.logmile.domain.vehicle.repository;

import com.project.logmile.domain.vehicle.entity.Vehicle;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

	Optional<Vehicle> findByPlateNo(String plateNo);

	List<Vehicle> findByCompanyId(Long companyId);

	boolean existsByPlateNo(String plateNo);
}
