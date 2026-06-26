package com.project.logmile.domain.driver.service;

import com.project.logmile.common.exception.BusinessException;
import com.project.logmile.common.exception.ErrorCode;
import com.project.logmile.common.security.TenantAccessService;
import com.project.logmile.domain.company.entity.Company;
import com.project.logmile.domain.driver.dto.DriverRequest;
import com.project.logmile.domain.driver.dto.DriverResponse;
import com.project.logmile.domain.driver.entity.Driver;
import com.project.logmile.domain.driver.repository.DriverRepository;
import com.project.logmile.domain.vehicle.entity.Vehicle;
import com.project.logmile.domain.vehicle.repository.VehicleRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DriverService {

	private final DriverRepository driverRepository;
	private final VehicleRepository vehicleRepository;
	private final TenantAccessService tenantAccessService;

	@Transactional
	public DriverResponse register(DriverRequest request) {
		Company company = tenantAccessService.getCurrentAdmin().getCompany();
		Driver driver = Driver.create(company, request.name(), request.phone(), request.licenseType());
		return DriverResponse.from(driverRepository.save(driver));
	}

	@Transactional(readOnly = true)
	public List<DriverResponse> findAll() {
		Long companyId = tenantAccessService.getCurrentCompanyId();
		return driverRepository.findByCompanyId(companyId)
			.stream()
			.map(DriverResponse::from)
			.toList();
	}

	@Transactional(readOnly = true)
	public DriverResponse findById(Long id) {
		Driver driver = getDriver(id);
		tenantAccessService.validate(driver.getCompany().getId());
		return DriverResponse.from(driver);
	}

	@Transactional
	public DriverResponse update(Long id, DriverRequest request) {
		Driver driver = getDriver(id);
		tenantAccessService.validate(driver.getCompany().getId());
		driver.update(request.name(), request.phone(), request.licenseType());
		return DriverResponse.from(driver);
	}

	@Transactional
	public void delete(Long id) {
		Driver driver = getDriver(id);
		tenantAccessService.validate(driver.getCompany().getId());
		// 차량 배정 해제 후 삭제
		if (driver.getVehicle() != null) {
			driver.getVehicle().unassignDriver();
			driver.unassignVehicle();
		}
		driverRepository.delete(driver);
	}

	@Transactional
	public DriverResponse assignVehicle(Long driverId, Long vehicleId) {
		Driver driver   = getDriver(driverId);
		Vehicle vehicle = vehicleRepository.findById(vehicleId)
			.orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT));

		tenantAccessService.validate(driver.getCompany().getId());
		tenantAccessService.validate(vehicle.getCompany().getId());

		// 차량에 이미 다른 운전자가 배정된 경우
		if (vehicle.getDriver() != null && !vehicle.getDriver().getId().equals(driverId)) {
			throw new BusinessException(ErrorCode.INVALID_INPUT);
		}
		driver.assignVehicle(vehicle);
		vehicle.assignDriver(driver);
		return DriverResponse.from(driver);
	}

	@Transactional
	public DriverResponse unassignVehicle(Long driverId) {
		Driver driver = getDriver(driverId);
		tenantAccessService.validate(driver.getCompany().getId());
		if (driver.getVehicle() != null) {
			driver.getVehicle().unassignDriver();
			driver.unassignVehicle();
		}
		return DriverResponse.from(driver);
	}

	public Driver getEntityById(Long id) { return getDriver(id); }

	private Driver getDriver(Long id) {
		return driverRepository.findById(id)
			.orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT));
	}
}
