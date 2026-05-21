package com.project.logmile.domain.dashboard.repository;

import com.project.logmile.common.enums.DashboardActionType;
import com.project.logmile.domain.dashboard.entity.DashboardAction;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DashboardActionRepository extends JpaRepository<DashboardAction, Long> {

	@Query("SELECT a FROM DashboardAction a WHERE a.driveLog.id IN :driveLogIds")
	List<DashboardAction> findByDriveLogIds(@Param("driveLogIds") Collection<Long> driveLogIds);

	Optional<DashboardAction> findTopByDriveLogIdAndActionTypeOrderByCreatedAtDesc(
		Long driveLogId, DashboardActionType actionType);

	@Query("SELECT a.driveLog.id, COUNT(a) FROM DashboardAction a "
		+ "WHERE a.driveLog.id IN :driveLogIds "
		+ "AND a.actionType = com.project.logmile.common.enums.DashboardActionType.REST_GUIDE "
		+ "GROUP BY a.driveLog.id")
	List<Object[]> countRestGuideByDriveLogIds(@Param("driveLogIds") Collection<Long> driveLogIds);
}
