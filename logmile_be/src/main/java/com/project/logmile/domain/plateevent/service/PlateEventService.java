package com.project.logmile.domain.plateevent.service;

import com.project.logmile.domain.plateevent.dto.PlateEventCreateRequest;
import com.project.logmile.domain.plateevent.dto.PlateEventResponse;
import com.project.logmile.domain.plateevent.entity.PlateEvent;
import com.project.logmile.domain.plateevent.repository.PlateEventRepository;
import com.project.logmile.domain.vehicle.entity.Vehicle;
import com.project.logmile.domain.vehicle.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlateEventService {

	private final PlateEventRepository plateEventRepository;
	private final VehicleRepository vehicleRepository;

	@Transactional
	public PlateEventResponse create(PlateEventCreateRequest request) {
		String plateNo = request.plateNo().trim();
		Vehicle vehicle = findVehicle(plateNo);
		PlateEvent plateEvent = PlateEvent.create(
			vehicle,
			plateNo,
			request.eventType(),
			request.locationType(),
			request.sourceType(),
			request.observedAt(),
			request.latitude(),
			request.longitude(),
			request.confidence(),
			request.detectionConfidence(),
			request.manualRequired(),
			request.imagePath(),
			request.memo()
		);

		return PlateEventResponse.from(plateEventRepository.save(plateEvent));
	}

	private Vehicle findVehicle(String plateNo) {
		return vehicleRepository.findByPlateNo(plateNo)
			.or(() -> vehicleRepository.findByNormalizedPlateNo(plateNo.replaceAll("\\s+", "")))
			.orElse(null);
	}
}
