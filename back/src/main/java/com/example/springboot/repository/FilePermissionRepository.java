package com.example.springboot.repository;

import com.example.springboot.Enum.PermissionType;
import com.example.springboot.entity.filePermission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface FilePermissionRepository extends JpaRepository<filePermission, Long> {
    List<filePermission> findByUserId(Integer userId);

    void deleteByFileId(Long fileId);

    Optional<filePermission> findByFileIdAndUserId(Long fileId, Integer targetUserId);

    void deleteByFileIdAndUserId(Long fileId, Integer targetUserId);
    boolean existsByFileIdAndUserIdAndPermissionIn(
            Long file_id, Integer userId, Collection<PermissionType> permission
    );

}
