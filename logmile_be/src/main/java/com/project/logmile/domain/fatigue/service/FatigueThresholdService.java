package com.project.logmile.domain.fatigue.service;

import com.project.logmile.common.exception.BusinessException;
import com.project.logmile.common.exception.ErrorCode;
import com.project.logmile.domain.fatigue.dto.ThresholdResponse;
import com.project.logmile.domain.fatigue.dto.ThresholdUpdateRequest;
import com.project.logmile.domain.fatigue.entity.FatigueThreshold;
import com.project.logmile.domain.fatigue.repository.FatigueThresholdRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FatigueThresholdService {

	private final FatigueThresholdRepository fatigueThresholdRepository;

	/**
	 * 전체 임계값 목록 조회
	 */
	@Transactional(readOnly = true)
	public List<ThresholdResponse> findAll() {
		return fatigueThresholdRepository.findAll()
			.stream()
			.map(ThresholdResponse::from)
			.toList();
	}

	/**
	 * 특정 임계값 조회 (key 기준)
	 */
	@Transactional(readOnly = true)
	public ThresholdResponse findByKey(String key) {
		FatigueThreshold threshold = fatigueThresholdRepository.findByThresholdKey(key)
			.orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT));
		return ThresholdResponse.from(threshold);
	}

	/**
	 * 임계값 수정
	 */
	@Transactional
	public ThresholdResponse update(Long id, ThresholdUpdateRequest request) {
		FatigueThreshold threshold = fatigueThresholdRepository.findById(id)
			.orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT));
		threshold.updateValue(request.thresholdValue(), request.description());
		return ThresholdResponse.from(threshold);
	}

	/**
	 * 임계값 수정 (key 기준)
	 */
	@Transactional
	public ThresholdResponse updateByKey(String key, ThresholdUpdateRequest request) {
		FatigueThreshold threshold = fatigueThresholdRepository.findByThresholdKey(key)
			.orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT));
		threshold.updateValue(request.thresholdValue(), request.description());
		return ThresholdResponse.from(threshold);
	}
}
