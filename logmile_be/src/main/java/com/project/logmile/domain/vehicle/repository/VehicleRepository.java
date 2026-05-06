package com.project.logmile.domain.vehicle.repository;

import com.project.logmile.domain.vehicle.entity.Vehicle;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

	Optional<Vehicle> findByPlateNo(String plateNo);

	@Query("select v from Vehicle v where replace(v.plateNo, ' ', '') = :plateNo")
	Optional<Vehicle> findByNormalizedPlateNo(@Param("plateNo") String plateNo);

	List<Vehicle> findByCompanyId(Long companyId);

	boolean existsByPlateNo(String plateNo);
}
