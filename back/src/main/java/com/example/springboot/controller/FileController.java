package com.example.springboot.controller;

import com.example.springboot.Enum.PermissionType;
import com.example.springboot.entity.User;
import com.example.springboot.entity.fileStorage;
import com.example.springboot.service.FileService;
import com.example.springboot.service.UserService;
import com.example.springboot.utils.JwtUtil;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

//@CrossOrigin(origins = "http://localhost:5173")
@CrossOrigin(
        origins = "http://localhost:5173",       // 允许的前端域名
        exposedHeaders = HttpHeaders.CONTENT_DISPOSITION  // 明确暴露 CONTENT_DISPOSITION
)
@RestController
@RequestMapping("/file")
public class FileController {
    private final FileService fileService;
    private final UserService userService;

    public FileController(FileService fileService, UserService userService) {
        this.fileService = fileService;
        this.userService = userService;
    }

    private Integer extractUserIdFromToken(String tokenHeader) {
        String token = tokenHeader.replace("Bearer ", "");
        String username = JwtUtil.getUsernameFromToken(token);
        User user = userService.getUserInformation(username);
        return user.getId();
    }

    // 简单的 token 校验方法（根据实际情况来实现）
//    private boolean isValidToken(String authorization) {
//
//
//        return true;
//    }

    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> listUserFiles(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            Integer userId = extractUserIdFromToken(authorizationHeader);

            // 查询用户的文件
            List<fileStorage> files = fileService.getUserFiles(userId);
            List<fileStorage> distinctFiles = files.stream()
                    .collect(Collectors.collectingAndThen(
                            Collectors.toMap(file -> file.getId(), file -> file, (f1, f2) -> f1),
                            map -> new ArrayList<>(map.values())
                    ));
            return ResponseEntity.ok(Map.of(
                    "code", 200,
                    "message", "Success",
                    "files", distinctFiles
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of(
                    "code", 400,
                    "message", "Failed to fetch files"
            ));
        }
    }

    // 下载文件 API
    @GetMapping("/download")
    public ResponseEntity<?> downloadFile(@RequestParam Long fileId,
                                          @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        try {
            // 在此可以添加文件权限验证，检查 token 或权限
            Integer userId = extractUserIdFromToken(authorizationHeader);
            // 调用 FileService 处理文件下载
            return fileService.downloadFile(fileId, userId);
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Failed to download file: " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body("File not found: " + e.getMessage());
        }
    }

    // 删除文件 API
    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteFile(@RequestParam Long fileId,
                                        @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        try {
            // 在此可以添加文件权限验证，检查 token 或权限
            Integer userId = extractUserIdFromToken(authorizationHeader);
            // 调用 FileService 删除文件
            String message = fileService.deleteFile(fileId, userId);

            if (message.equals("File deleted successfully")) {
                return ResponseEntity.ok().body("File deleted successfully");
            } else {
                return ResponseEntity.status(404).body(message);  // 返回 404 状态码表示文件未找到
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to delete file: " + e.getMessage());
        }
    }

    // 文件共享 API
    @PostMapping("/share")
    public ResponseEntity<?> shareFile(@RequestParam Integer targetUserId,
                                       @RequestParam Long fileId,
                                       @RequestParam PermissionType permission,
                                       @RequestHeader(value = "Authorization") String authorizationHeader) {
        try {

            Integer userId = extractUserIdFromToken(authorizationHeader);
            // 调用 FileService 共享文件
            String message = fileService.shareFile(userId, targetUserId, fileId, permission);

            if (message.equals("File shared successfully")) {
                return ResponseEntity.ok().body(message);
            } else {
                return ResponseEntity.badRequest().body(message);
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to share file: " + e.getMessage());
        }
    }
    @GetMapping("/demo")
    public ResponseEntity<?> demoFile(@RequestParam Long fileId,
                                          @RequestHeader(value = "Authorization") String authorizationHeader) {
        try {
            Integer userId = extractUserIdFromToken(authorizationHeader);
            // 调用 Service 层处理文件下载
            return fileService.demoFile(fileId, userId);

        }catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("code", "400", "msg", e.getMessage()));

        }
    }

    // 撤销文件权限 API
    @PostMapping("/revoke")
    public ResponseEntity<?> revokeFilePermission(@RequestParam Long fileId,
                                                  @RequestParam Integer targetUserId,
                                                  @RequestHeader(value = "Authorization") String authorizationHeader) {
        try {
            // 进行权限验证，检查用户是否有权限撤销
            // 可通过解析 token 进行验证，或通过其他方式确保权限（在这里简化了处理）

            // 调用 FileService 撤销权限
            String message = fileService.revokeFilePermission(fileId, targetUserId);

            if (message.equals("File permission revoked successfully")) {
                return ResponseEntity.ok().body(message);
            } else {
                return ResponseEntity.badRequest().body(message);
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to revoke file permission: " + e.getMessage());
        }
    }
}

