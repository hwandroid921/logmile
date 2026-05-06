package com.project.logmile.domain.company.service;

import com.project.logmile.common.exception.BusinessException;
import com.project.logmile.common.exception.ErrorCode;
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
			throw new BusinessException(ErrorCode.DUPLICATE_COMPANY_NAME);
		}
		Company company = Company.create(request.name(), request.address(), request.phone());
		return CompanyResponse.from(companyRepository.save(company));
	}

	@Transactional(readOnly = true)
	public CompanyResponse findById(Long id) {
		Company company = companyRepository.findById(id)
			.orElseThrow(() -> new BusinessException(ErrorCode.COMPANY_NOT_FOUND));
		return CompanyResponse.from(company);
	}

	@Transactional(readOnly = true)
	public List<CompanyResponse> findAll() {
		return companyRepository.findAll()
			.stream()
			.map(CompanyResponse::from)
			.toList();
	}

	@Transactional(readOnly = true)
	public Company getEntityById(Long id) {
		return companyRepository.findById(id)
			.orElseThrow(() -> new BusinessException(ErrorCode.COMPANY_NOT_FOUND));
	}
}
