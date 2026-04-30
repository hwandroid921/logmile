package com.project.logmile.domain.company.repository;

import com.project.logmile.domain.company.entity.Company;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyRepository extends JpaRepository<Company, Long> {

	Optional<Company> findByName(String name);

	boolean existsByName(String name);
}
