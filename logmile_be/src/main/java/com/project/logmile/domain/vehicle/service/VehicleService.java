package com.project.logmile.domain.vehicle.service;

import com.project.logmile.common.exception.BusinessException;
import com.project.logmile.common.exception.ErrorCode;
import com.project.logmile.common.security.TenantAccessService;
import com.project.logmile.domain.company.entity.Company;
import com.project.logmile.domain.vehicle.dto.VehicleRequest;
import com.project.logmile.domain.vehicle.dto.VehicleResponse;
import com.project.logmile.domain.vehicle.entity.Vehicle;
import com.project.logmile.domain.vehicle.repository.VehicleRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VehicleService {

	private final VehicleRepository vehicleRepository;
	private final TenantAccessService tenantAccessService;

	@Transactional
	public VehicleResponse register(VehicleRequest request) {
		if (vehicleRepository.existsByPlateNo(request.plateNo())) {
			throw new BusinessException(ErrorCode.INVALID_INPUT);
		}
		Company company = tenantAccessService.getCurrentAdmin().getCompany();
		Vehicle vehicle = Vehicle.create(company, request.plateNo(), request.type());
		return VehicleResponse.from(vehicleRepository.save(vehicle));
	}

	@Transactional(readOnly = true)
	public List<VehicleResponse> findAll() {
		Long companyId = tenantAccessService.getCurrentCompanyId();
		return vehicleRepository.findByCompanyId(companyId)
			.stream()
			.map(VehicleResponse::from)
			.toList();
	}

	@Transactional(readOnly = true)
	public VehicleResponse findById(Long id) {
		Vehicle vehicle = getVehicle(id);
		tenantAccessService.validate(vehicle.getCompany().getId());
		return VehicleResponse.from(vehicle);
	}

	@Transactional
	public VehicleResponse update(Long id, VehicleRequest request) {
		Vehicle vehicle = getVehicle(id);
		tenantAccessService.validate(vehicle.getCompany().getId());
		vehicle.update(request.plateNo(), request.type());
		return VehicleResponse.from(vehicle);
	}

	@Transactional
	public void deactivate(Long id) {
		Vehicle vehicle = getVehicle(id);
		tenantAccessService.validate(vehicle.getCompany().getId());
		vehicle.deactivate();
	}

	public Vehicle getEntityById(Long id) {
		return getVehicle(id);
	}

	private Vehicle getVehicle(Long id) {
		return vehicleRepository.findById(id)
			.orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT));
	}
}
