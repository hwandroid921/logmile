package com.project.logmile.domain.admin.service;

import com.project.logmile.common.enums.AdminStatus;
import com.project.logmile.common.exception.BusinessException;
import com.project.logmile.common.exception.ErrorCode;
import com.project.logmile.domain.admin.dto.AdminSummaryResponse;
import com.project.logmile.domain.admin.entity.Admin;
import com.project.logmile.domain.admin.repository.AdminRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminApprovalService {

	private final AdminRepository adminRepository;

	@Transactional(readOnly = true)
	public List<AdminSummaryResponse> getPendingAdmins() {
		return adminRepository.findByStatus(AdminStatus.PENDING)
			.stream()
			.map(AdminSummaryResponse::from)
			.toList();
	}

	@Transactional
	public void approve(Long adminId) {
		Admin admin = findAdmin(adminId);
		if (admin.getStatus() != AdminStatus.PENDING) {
			throw new BusinessException(ErrorCode.INVALID_INPUT);
		}
		admin.approve();
	}

	@Transactional
	public void reject(Long adminId) {
		Admin admin = findAdmin(adminId);
		admin.reject();
	}

	@Transactional
	public void suspend(Long adminId) {
		Admin admin = findAdmin(adminId);
		if (admin.isSuperAdmin()) {
			throw new BusinessException(ErrorCode.ACCESS_DENIED);
		}
		admin.suspend();
	}

	@Transactional
	public void unsuspend(Long adminId) {
		Admin admin = findAdmin(adminId);
		admin.unsuspend();
	}

	private Admin findAdmin(Long adminId) {
		return adminRepository.findById(adminId)
			.orElseThrow(() -> new BusinessException(ErrorCode.ADMIN_NOT_FOUND));
	}
}
