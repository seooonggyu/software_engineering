package com.project.software_engineering.repository;

import com.project.software_engineering.domain.PermissionUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PermissionUserRepository extends JpaRepository<PermissionUser, Long> {
    PermissionUser findByPermissionIdAndUserId(Long permissionId, Long userId);
}
