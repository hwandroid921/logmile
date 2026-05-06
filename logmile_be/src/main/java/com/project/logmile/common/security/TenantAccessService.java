package com.project.logmile.common.security;

import com.project.logmile.common.enums.AdminRole;
import com.project.logmile.common.exception.BusinessException;
import com.project.logmile.common.exception.ErrorCode;
import com.project.logmile.domain.admin.entity.Admin;
import com.project.logmile.domain.admin.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TenantAccessService {

	private final AdminRepository adminRepository;

	/**
	 * 현재 로그인 관리자의 소속 업체 ID를 반환한다.
	 * SUPER_ADMIN은 company_id 가 null 이므로 사용 시 주의.
	 */
	public Long getCurrentCompanyId() {
		Admin admin = getCurrentAdmin();
		if (admin.getCompany() == null) {
			throw new BusinessException(ErrorCode.COMPANY_NOT_FOUND);
		}
		return admin.getCompany().getId();
	}

	/**
	 * 리소스의 companyId와 현재 관리자의 companyId를 비교한다.
	 * SUPER_ADMIN은 모든 업체에 접근 가능하므로 검증을 생략한다.
	 */
	public void validate(Long resourceCompanyId) {
		Admin admin = getCurrentAdmin();
		if (admin.getRole() == AdminRole.ROLE_SUPER_ADMIN) {
			return;
		}
		if (admin.getCompany() == null
			|| !admin.getCompany().getId().equals(resourceCompanyId)) {
			throw new BusinessException(ErrorCode.ACCESS_DENIED);
		}
	}

	public Admin getCurrentAdmin() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		Long adminId = Long.parseLong(auth.getName());
		return adminRepository.findById(adminId)
			.orElseThrow(() -> new BusinessException(ErrorCode.ADMIN_NOT_FOUND));
	}
}
