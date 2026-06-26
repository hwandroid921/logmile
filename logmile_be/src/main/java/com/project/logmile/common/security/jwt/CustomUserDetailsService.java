package com.project.logmile.common.security.jwt;

import com.project.logmile.common.enums.AdminStatus;
import com.project.logmile.common.exception.BusinessException;
import com.project.logmile.common.exception.ErrorCode;
import com.project.logmile.domain.admin.entity.Admin;
import com.project.logmile.domain.admin.repository.AdminRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

	private final AdminRepository adminRepository;

	// 로그인 시 email 기반 조회 (Spring Security 기본 흐름)
	@Override
	@Transactional(readOnly = true)
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		Admin admin = adminRepository.findByEmail(email)
			.orElseThrow(() -> new UsernameNotFoundException("이메일을 찾을 수 없습니다: " + email));
		validateStatus(admin);
		return buildUserDetails(admin);
	}

	// JWT 필터에서 adminId 기반 조회
	@Transactional(readOnly = true)
	public UserDetails loadUserById(Long adminId) {
		Admin admin = adminRepository.findById(adminId)
			.orElseThrow(() -> new BusinessException(ErrorCode.ADMIN_NOT_FOUND));
		validateStatus(admin);
		return buildUserDetails(admin);
	}

	private void validateStatus(Admin admin) {
		switch (admin.getStatus()) {
			case PENDING   -> throw new BusinessException(ErrorCode.ACCOUNT_PENDING);
			case REJECTED  -> throw new BusinessException(ErrorCode.ACCOUNT_REJECTED);
			case SUSPENDED -> throw new BusinessException(ErrorCode.ACCOUNT_SUSPENDED);
			case INACTIVE  -> throw new BusinessException(ErrorCode.ACCOUNT_INACTIVE);
			default        -> { /* ACTIVE - 정상 통과 */ }
		}
	}

	private UserDetails buildUserDetails(Admin admin) {
		return new User(
			String.valueOf(admin.getId()),
			admin.getPassword(),
			List.of(new SimpleGrantedAuthority(admin.getRole().name()))
		);
	}
}
