package com.project.logmile.domain.company.service;

import com.project.logmile.domain.company.dto.CompanyRegisterRequest;
import com.project.logmile.domain.company.dto.CompanyResponse;
import com.project.logmile.domain.company.entity.Company;
import com.project.logmile.domain.company.repository.CompanyRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CompanyService {

	private final CompanyRepository companyRepository;

	@Transactional
	public CompanyResponse register(CompanyRegisterRequest request) {
		if (companyRepository.existsByName(request.name())) {
			throw new IllegalArgumentException("이미 등록된 업체명입니다: " + request.name());
		}
		Company company = Company.create(request.name(), request.address(), request.phone());
		return CompanyResponse.from(companyRepository.save(company));
	}

	@Transactional(readOnly = true)
	public CompanyResponse findById(Long id) {
		Company company = companyRepository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("업체를 찾을 수 없습니다: " + id));
		return CompanyResponse.from(company);
	}

	@Transactional(readOnly = true)
	public List<CompanyResponse> findAll() {
		return companyRepository.findAll()
			.stream()
			.map(CompanyResponse::from)
			.toList();
	}

	// 회원가입 흐름에서 Company 엔티티 직접 조회가 필요한 경우 사용
	@Transactional(readOnly = true)
	public Company getEntityById(Long id) {
		return companyRepository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("업체를 찾을 수 없습니다: " + id));
	}
}
