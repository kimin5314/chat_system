package com.example.springboot.repository;
import com.example.springboot.entity.fileStorage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface FileStorageRepository extends JpaRepository<fileStorage, Long> {

    // 原有方法：通过用户ID查找该用户上传的所有文件
    List<fileStorage> findByUserId(Long userId);

    // ✅ 新增方法：通过文件ID查询文件名（只查文件名字段，提高效率）
//    String findOriginalFilenameById(Long fileId);
    @Query("SELECT f.fileName FROM fileStorage f WHERE f.id = :id")
    String findFilenameById(@Param("id") Long id);

}