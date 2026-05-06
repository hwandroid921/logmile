package com.project.logmile.domain.auth.service;

import com.project.logmile.common.exception.BusinessException;
import com.project.logmile.common.exception.ErrorCode;
import com.project.logmile.common.security.jwt.JwtTokenProvider;
import com.project.logmile.domain.admin.entity.Admin;
import com.project.logmile.domain.admin.repository.AdminRepository;
import com.project.logmile.domain.auth.dto.LoginRequest;
import com.project.logmile.domain.auth.dto.LoginResponse;
import com.project.logmile.domain.auth.dto.SignupRequest;
import com.project.logmile.domain.auth.dto.SignupResponse;
import com.project.logmile.domain.company.entity.Company;
import com.project.logmile.domain.company.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final AdminRepository adminRepository;
	private final CompanyRepository companyRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtTokenProvider jwtTokenProvider;

	@Transactional
	public SignupResponse signup(SignupRequest request) {
		if (companyRepository.existsByName(request.companyName())) {
			throw new BusinessException(ErrorCode.DUPLICATE_COMPANY_NAME);
		}
		if (adminRepository.existsByEmail(request.email())) {
			throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
		}

		Company company = Company.create(request.companyName(), request.address(), request.phone());
		companyRepository.save(company);

		Admin admin = Admin.create(
			request.email(),
			passwordEncoder.encode(request.password()),
			request.name(),
			request.adminPhone(),
			company
		);
		adminRepository.save(admin);

		return SignupResponse.from(admin);
	}

	@Transactional(readOnly = true)
	public LoginResponse login(LoginRequest request) {
		Admin admin = adminRepository.findByEmail(request.email())
			.orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));

		if (!passwordEncoder.matches(request.password(), admin.getPassword())) {
			throw new BusinessException(ErrorCode.UNAUTHORIZED);
		}

		switch (admin.getStatus()) {
			case PENDING   -> throw new BusinessException(ErrorCode.ACCOUNT_PENDING);
			case REJECTED  -> throw new BusinessException(ErrorCode.ACCOUNT_REJECTED);
			case SUSPENDED -> throw new BusinessException(ErrorCode.ACCOUNT_SUSPENDED);
			case INACTIVE  -> throw new BusinessException(ErrorCode.ACCOUNT_INACTIVE);
			default -> { }
		}

		String token = jwtTokenProvider.createToken(admin.getId(), admin.getEmail(), admin.getRole());

		Long companyId = admin.getCompany() != null ? admin.getCompany().getId() : null;
		return LoginResponse.of(token, admin.getId(), admin.getName(),
			admin.getRole(), admin.getStatus(), companyId);
	}
}
