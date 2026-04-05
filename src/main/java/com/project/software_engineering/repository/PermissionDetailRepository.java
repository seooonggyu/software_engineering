package com.project.software_engineering.repository;

import com.project.software_engineering.domain.PermissionDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PermissionDetailRepository extends JpaRepository<PermissionDetail, Long> {
    PermissionDetail findByPermissionIdAndTargetAndFunc(Long permissionId, String target, Integer func);
}
