package com.project.logmile.domain.company.controller;

import com.project.logmile.common.exception.BusinessException;
import com.project.logmile.common.exception.ErrorCode;
import com.project.logmile.domain.admin.entity.Admin;
import com.project.logmile.domain.admin.repository.AdminRepository;
import com.project.logmile.domain.company.dto.CompanyResponse;
import com.project.logmile.domain.company.service.CompanyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Company", description = "업체 API")
@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
public class CompanyController {

	private final CompanyService companyService;
	private final AdminRepository adminRepository;

	@Operation(summary = "전체 업체 목록 조회 (SUPER_ADMIN 전용)", description = "등록된 모든 업체 목록을 조회합니다.")
	@GetMapping
	@PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
	public ResponseEntity<List<CompanyResponse>> findAll() {
		return ResponseEntity.ok(companyService.findAll());
	}

	@Operation(summary = "내 소속 업체 조회", description = "로그인한 관리자의 소속 업체 정보를 조회합니다.")
	@GetMapping("/me")
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	public ResponseEntity<CompanyResponse> findMyCompany(Authentication authentication) {
		Long adminId = Long.parseLong(authentication.getName());
		Admin admin = adminRepository.findById(adminId)
			.orElseThrow(() -> new BusinessException(ErrorCode.ADMIN_NOT_FOUND));
		if (admin.getCompany() == null) {
			throw new BusinessException(ErrorCode.COMPANY_NOT_FOUND);
		}
		return ResponseEntity.ok(CompanyResponse.from(admin.getCompany()));
	}
}
