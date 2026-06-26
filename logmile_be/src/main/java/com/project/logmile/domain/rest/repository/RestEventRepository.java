package com.project.logmile.domain.rest.repository;

import com.project.logmile.domain.rest.entity.RestEvent;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RestEventRepository extends JpaRepository<RestEvent, Long> {

	List<RestEvent> findByDriveLogIdOrderByRestStartedAtAsc(Long driveLogId);

	// 현재 진행 중(PENDING) 휴식 이벤트 조회
	java.util.Optional<RestEvent> findTopByDriveLogIdAndRestTypeOrderByRestStartedAtDesc(
		Long driveLogId, com.project.logmile.common.enums.RestType restType);

	// 가장 최근 종료된 유효/충분 휴식 조회 (연속운행 계산용)
	java.util.Optional<RestEvent> findTopByDriveLogIdAndRestTypeInOrderByRestEndedAtDesc(
		Long driveLogId, java.util.List<com.project.logmile.common.enums.RestType> types);

	// 유효 휴식 횟수 (종료 기준)
	long countByDriveLogIdAndRestTypeIn(Long driveLogId,
		java.util.List<com.project.logmile.common.enums.RestType> types);
}
