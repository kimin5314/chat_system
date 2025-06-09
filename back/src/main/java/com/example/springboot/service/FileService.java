package com.example.springboot.service;

import com.example.springboot.Enum.FileType;
import com.example.springboot.Enum.PermissionType;
import com.example.springboot.entity.User;
import com.example.springboot.entity.filePermission;
import com.example.springboot.entity.fileStorage;
import com.example.springboot.repository.FilePermissionRepository;
import com.example.springboot.repository.FileStorageRepository;
import com.example.springboot.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

@Service
public class FileService {
    private final FileStorageRepository fileStorageRepository;
    private final FilePermissionRepository filePermissionRepository;
    private final UserRepository userRepository;


    @Value("${file.upload-dir}")
    private String uploadDir;  // 配置上传文件的存储路径

    public FileService(FileStorageRepository fileStorageRepository, FilePermissionRepository filePermissionRepository, UserRepository userRepository) {
        this.fileStorageRepository = fileStorageRepository;
        this.filePermissionRepository = filePermissionRepository;
        this.userRepository = userRepository;
    }

    // 获取当前用户上传的文件和共享文件
    public List<fileStorage> getUserFiles(Integer userId) {
        // 获取用户有权限的文件
        List<filePermission> Permissions = filePermissionRepository.findByUserId(userId);
        List<fileStorage> files = Permissions.stream()
                .map(filePermission::getFile)
                .toList();
        return files;
    }

    // 处理文件上传后写入数据库
    public fileStorage uploadFile(Integer userId, MultipartFile file, String fileName, FileType fileType, long fileSize) throws IOException {
        // 如果没有传入文件名，使用原始文件名
        if (fileName == null || fileName.isEmpty()) {
            fileName = file.getOriginalFilename();
        }

        // 构建存储文件的路径
        Path path = Paths.get(uploadDir, fileName);
        // 创建文件并存储
        File destFile = path.toFile();
        file.transferTo(destFile);

        // 保存文件的元数据到数据库
        fileStorage stored = new fileStorage();
        stored.setUserId(userId);
        stored.setFileName(fileName);
        stored.setFileType(fileType);
        stored.setFileSize(fileSize);
        stored.setFileUrl(path.toString());
        stored = fileStorageRepository.save(stored);

        // 读
        filePermission permission_read = new filePermission();
        permission_read.setUserId(userId);                // 上传者的 userId
        permission_read.setFile(stored);                  // 关联到刚才保存的 fileStorage
        permission_read.setPermission(PermissionType.READ); // 或者 WRITE/DELETE/SHARE，看你业务需求
        filePermissionRepository.save(permission_read);
        // 删除
        filePermission permission_delete = new filePermission();
        permission_delete.setUserId(userId);
        permission_delete.setFile(stored);
        permission_delete.setPermission(PermissionType.DELETE);
        filePermissionRepository.save(permission_delete);
        // 共享
        filePermission permission_share = new filePermission();
        permission_share.setUserId(userId);
        permission_share.setFile(stored);
        permission_share.setPermission(PermissionType.SHARE);
        filePermissionRepository.save(permission_share);
        return stored;
    }

    // 根据文件 ID 获取文件并返回响应实体
    public ResponseEntity<InputStreamResource> downloadFile(Long fileId, Integer userId) throws IOException {
        // ------------------- 0. 从数据库查询文件元数据（只查一次） -------------------
        fileStorage fileMeta = fileStorageRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found")); // 找不到文件记录则抛异常

        boolean hasReadPermission = filePermissionRepository.existsByFileIdAndUserIdAndPermissionIn(
                fileId,
                userId,
                List.of(PermissionType.READ)
        );
        if (!hasReadPermission) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }
        // 2. 本地磁盘路径
        Path path = Paths.get(uploadDir).resolve(fileMeta.getFileName());
        File fileToDownload = path.toFile();
        if (!fileToDownload.exists()) {
            throw new RuntimeException("File not found on server");
        }

        // 3. 构造资源流
        InputStreamResource resource = new InputStreamResource(new FileInputStream(fileToDownload));

        // 4. 推断 MIME 类型
        String mimeType = Files.probeContentType(path);
        if (mimeType == null) {
            // fallback
            mimeType = "application/octet-stream";
        }        // 5. 设置下载时的文件名，处理中文或特殊字符
        String encodedFileName = UriUtils.encode(fileMeta.getFileName(), StandardCharsets.UTF_8);
        String contentDisposition = "attachment; filename=\"" + encodedFileName + "\"; filename*=utf-8''" + encodedFileName;
        // 6. 构造响应
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .contentType(MediaType.parseMediaType(mimeType))
                .contentLength(fileToDownload.length())
                .body(resource);
    }


    // 根据文件 ID 删除文件
    @Transactional
    public String deleteFile(Long fileId, Integer userId) {
        boolean hasReadOrDeletePermission = filePermissionRepository.existsByFileIdAndUserIdAndPermissionIn(
                fileId,
                userId,
                List.of(PermissionType.DELETE)
        );
        // 1.3 如果两者都不满足，则直接返回 403 Forbidden（无权下载）
        if (!hasReadOrDeletePermission) {
            return "no permission to delete this file";
        }

        // 获取文件存储信息
        Optional<fileStorage> fileOptional = fileStorageRepository.findById(fileId);

        if (fileOptional.isEmpty()) {
            return "File not found";
        }

        fileStorage file = fileOptional.get();        // 构建文件路径
        String filePath = uploadDir + File.separator + file.getFileName();
        File fileToDelete = new File(filePath);

        // 删除文件
        if (fileToDelete.exists()) {
            boolean isDeleted = fileToDelete.delete();
            if (!isDeleted) {
                return "Failed to delete file from server";
            }
        } else {
            filePermissionRepository.deleteByFileId(fileId);
            fileStorageRepository.delete(file);
            return "File not found on server";
        }
        // 删除数据库中的记录
        filePermissionRepository.deleteByFileId(fileId);
        fileStorageRepository.delete(file);        return "File deleted successfully";
    }

    // 文件共享
    public String shareFile(Integer userId, String targetUsername, Long fileId, PermissionType permission) {
        // 查找文件
        fileStorage file = fileStorageRepository.findById(fileId).orElse(null);
        if (file == null) {
            return "File not found";
        }

        // 查找目标用户 - 使用用户名而不是用户ID
        User targetUser = userRepository.findByUsername(targetUsername);
        if (targetUser == null) {
            return "Target user not found";
        }

        // 1.2 如果不是上传者，再判断是否在 file_permission 表中具有 READ 或 SHARE 权限
        boolean hasReadOrSharePermission = filePermissionRepository.existsByFileIdAndUserIdAndPermissionIn(
                fileId,
                userId,
                List.of(PermissionType.SHARE, PermissionType.READ)
        );        // 1.3 如果两者都不满足，则直接返回 403 Forbidden（无权下载）
        if (!hasReadOrSharePermission) {
            return "no permission to share this file";
        }
        // 创建文件权限
        filePermission filePermission = new filePermission();
        filePermission.setUserId(targetUser.getId()); // 使用目标用户的ID
        filePermission.setFile(file);
        filePermission.setPermission(permission);

        // 保存权限
        filePermissionRepository.save(filePermission);

        return "File shared successfully";
    }

    public ResponseEntity<byte[]> demoFile(Long fileId, Integer userId) {
        // 1) 查询数据库记录
        fileStorage fileMeta = fileStorageRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found")); // 找不到文件记录则抛异常

        // 2) 权限校验
        boolean hasReadPermission = filePermissionRepository.existsByFileIdAndUserIdAndPermissionIn(
                fileId,
                userId,
                List.of(PermissionType.READ)
        );
        if (!hasReadPermission) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }

        // 3) 构造路径并读取文件字节
        Path path = Paths.get(uploadDir).resolve(fileMeta.getFileName());
        File fileToDownload = path.toFile();
        if (!fileToDownload.exists()) {
            throw new RuntimeException("File not found on server");
        }

        byte[] data;
        try {
            data = Files.readAllBytes(path);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        // 4) 判断 MIME 类型
        String filename = fileStorageRepository.findFilenameById(fileId);
        String ext = getFileExtension(filename);
        MediaType mediaType = getMediaType(ext);

        // 5) 设置响应头，改为 inline
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(mediaType);
        headers.setContentDisposition(
                ContentDisposition.inline().filename(filename).build()
        );

        return new ResponseEntity<>(data, headers, HttpStatus.OK);
    }

    private String getFileExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        return (lastDot == -1) ? "" : filename.substring(lastDot + 1).toLowerCase();
    }

    private MediaType getMediaType(String ext) {
        switch (ext) {
            case "png":  return MediaType.IMAGE_PNG;
            case "jpg":
            case "jpeg": return MediaType.IMAGE_JPEG;
            case "gif":  return MediaType.IMAGE_GIF;
            case "txt":
            case "md":   return MediaType.TEXT_PLAIN;
            case "json": return MediaType.APPLICATION_JSON;
            case "pdf":  return MediaType.APPLICATION_PDF;
            default:     return MediaType.APPLICATION_OCTET_STREAM;
        }
    }

    // 撤销文件的共享权限
    public String revokeFilePermission(Long fileId, Integer targetUserId) {
        // 查找文件
        if (!fileStorageRepository.existsById(fileId)) {
            return "File not found";
        }

        // 查找目标用户
        if (!userRepository.existsById(targetUserId)) {
            return "Target user not found";
        }

        // 查找权限记录
        Optional<filePermission> filePermissionOptional = filePermissionRepository.findByFileIdAndUserId(fileId, targetUserId);

        // 如果权限记录存在，则删除它
        if (filePermissionOptional.isPresent()) {
            filePermissionRepository.deleteByFileIdAndUserId(fileId, targetUserId);
            return "File permission revoked successfully";
        } else {
            return "No permissions found for this user on the file";
        }
    }
}
