package com.project.logmile.domain.admin.repository;

import com.project.logmile.common.enums.AdminStatus;
import com.project.logmile.domain.admin.entity.Admin;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminRepository extends JpaRepository<Admin, Long> {

	Optional<Admin> findByEmail(String email);

	boolean existsByEmail(String email);

	List<Admin> findByStatus(AdminStatus status);

	List<Admin> findByCompanyId(Long companyId);
}
