package com.example.springboot.controller;

import com.example.springboot.service.ChunkUploadService;
import com.example.springboot.service.UserService;
import com.example.springboot.service.FileService;
import com.example.springboot.entity.User;
import com.example.springboot.entity.fileStorage;
import com.example.springboot.Enum.FileType;
import com.example.springboot.utils.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.List;

@RestController
@RequestMapping("/upload1")
public class ChunkUploadController {
    private final UserService userService;
    private final FileService fileService;
    private final ChunkUploadService chunkService;

    public ChunkUploadController(UserService userService,
                                 FileService fileService,
                                 ChunkUploadService chunkService) {
        this.userService = userService;
        this.fileService = fileService;
        this.chunkService = chunkService;
    }

    private Integer extractUserIdFromToken(String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String username = JwtUtil.getUsernameFromToken(token);
        User user = userService.getUserInformation(username);
        return user.getId();
    }

    private boolean isValidToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return false;
        return JwtUtil.validateToken(authHeader.substring(7));
    }

    @GetMapping("/status")
    public ResponseEntity<?> status(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam String fileId) throws IOException {
        if (!isValidToken(authHeader))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        extractUserIdFromToken(authHeader);

        List<Integer> uploaded = chunkService.listUploadedChunks(fileId);
        return ResponseEntity.ok(uploaded);
    }

    @PostMapping("/chunk")
    public ResponseEntity<?> uploadChunk(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam String fileId,
            @RequestParam int chunkIndex,
            @RequestParam MultipartFile chunk) throws IOException {
        if (!isValidToken(authHeader))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");        extractUserIdFromToken(authHeader);

        chunkService.saveChunk(fileId, chunkIndex, chunk.getInputStream());
        return ResponseEntity.ok("Chunk saved");
    }

    @PostMapping("/merge")
    public ResponseEntity<?> merge(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam String fileId,
            @RequestParam String filename) throws IOException {
        if (!isValidToken(authHeader))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        extractUserIdFromToken(authHeader);

        try {
            chunkService.mergeChunks(fileId, filename);
            return ResponseEntity.ok("Merge OK");
        } catch (NoSuchFileException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Chunks not found");
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam MultipartFile file,
            @RequestParam(required = false) String file_name,
            @RequestParam FileType file_type,
            @RequestParam long file_size) {
        if (!isValidToken(authHeader))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        Integer userId = extractUserIdFromToken(authHeader);
        try {
            fileStorage stored = fileService.uploadFile(userId, file, file_name, file_type, file_size);
            return ResponseEntity.ok(stored);        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Upload failed");
        }
    }
}
//import com.example.springboot.utils.JwtUtil;
//import com.example.springboot.service.FileService;
//import com.example.springboot.entity.fileStorage;
//import com.example.springboot.Enum.FileType;
//import jakarta.annotation.PostConstruct;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.util.FileSystemUtils;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.OutputStream;
//import java.io.UncheckedIOException;
//import java.nio.file.*;
//import java.util.Collections;
//import java.util.Comparator;
//import java.util.List;
//import java.util.stream.Collectors;
//
//@RestController
//@RequestMapping("/upload1")
//public class ChunkUploadController {
//    @Value("${file.upload-dir}")
//    private String uploadDir;
//    private Path tempRoot;
//    private final UserService userService;
//    private final FileService fileService;
//
//    public ChunkUploadController(UserService userService, FileService fileService) {
//        this.userService = userService;
//        this.fileService = fileService;
//    }
//
//    @PostConstruct
//    public void init() {
//        tempRoot = Paths.get(uploadDir);
//        try {
//            Files.createDirectories(tempRoot);
//        } catch (IOException e) {
//            throw new IllegalStateException("无法创建临时目录：" + tempRoot, e);
//        }
//    }
//
//    /**
//     * 从 Authorization 头中解析并获取用户 ID
//     */
//    private Integer extractUserIdFromToken(String tokenHeader) {
//        String token = tokenHeader.replace("Bearer ", "");
//        String username = JwtUtil.getUsernameFromToken(token);
//        User user = userService.getUserInformation(username);
//        System.out.println(user.getId());
//        return user.getId(); // 假设 token 解析后 userId 为 101
//    }
//
//    /**
//     * 简单的 token 校验方法，如果无效返回 false
//     */
//    private boolean isValidToken(String authorization) {
//        return true;
//    }
//
//    // 1. 查询已上传分片状态
//    @GetMapping("/status")
//    public ResponseEntity<?> status(
//            @RequestHeader(name = "Authorization") String authorization,
//            @RequestParam String fileId) throws IOException {
//        if (!isValidToken(authorization)) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or missing token");
//        }
//        Integer userId = extractUserIdFromToken(authorization);
//
//        Path dir = tempRoot.resolve(fileId);
//        if (!Files.exists(dir)) {
//            return ResponseEntity.ok(Collections.emptyList());
//        }
//        List<Integer> uploaded = Files.list(dir)
//                .filter(p -> p.getFileName().toString().endsWith(".part"))
//                .map(p -> Integer.parseInt(
//                        p.getFileName().toString().replace(".part", "")
//                ))
//                .sorted()
//                .collect(Collectors.toList());
//        return ResponseEntity.ok(uploaded);
//    }
//
//    // 2. 接收单个分片
//    @PostMapping("/chunk")
//    public ResponseEntity<?> uploadChunk(
//            @RequestHeader(name = "Authorization") String authorization,
//            @RequestParam("fileId") String fileId,
//            @RequestParam("chunkIndex") int chunkIndex,
//            @RequestParam("chunk") MultipartFile chunk
//    ) throws IOException {
//        System.out.println("Received chunk: " + chunkIndex + " for fileId: " + fileId);
//        if (!isValidToken(authorization)) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or missing token");
//        }
//        Integer userId = extractUserIdFromToken(authorization);
//
//        Path dir = tempRoot.resolve(fileId);
//        if (!Files.exists(dir)) {
//            Files.createDirectories(dir);
//        }
//        Path partPath = dir.resolve(chunkIndex + ".part");
//        try (InputStream in = chunk.getInputStream()) {
//            Files.copy(in, partPath, StandardCopyOption.REPLACE_EXISTING);
//        }
//        return ResponseEntity.ok("OK");
//    }
//
//    // 3. 合并所有分片
//    @PostMapping("/merge")
//    public ResponseEntity<?> merge(
//            @RequestHeader(name = "Authorization") String authorization,
//            @RequestParam String fileId,
//            @RequestParam String filename
//    ) throws IOException {
//        if (!isValidToken(authorization)) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or missing token");
//        }
//        Integer userId = extractUserIdFromToken(authorization);
//
//        Path dir = tempRoot.resolve(fileId);
//        if (!Files.exists(dir)) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("分片不存在");
//        }
//
//        Path target = Paths.get(uploadDir).resolve(filename);
//        try (OutputStream out = Files.newOutputStream(target,
//                StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
//            Files.list(dir)
//                    .filter(p -> p.getFileName().toString().endsWith(".part"))
//                    .sorted(Comparator.comparingInt(
//                            p -> Integer.parseInt(
//                                    p.getFileName().toString().replace(".part", "")
//                            )
//                    ))
//                    .forEach(part -> {
//                        try (InputStream in = Files.newInputStream(part)) {
//                            byte[] buf = new byte[8 * 1024];
//                            int len;
//                            while ((len = in.read(buf)) != -1) {
//                                out.write(buf, 0, len);
//                            }
//                        } catch (IOException e) {
//                            throw new UncheckedIOException(e);
//                        }
//                    });
//        }
//        FileSystemUtils.deleteRecursively(dir);
//        return ResponseEntity.ok("Merge OK");
//    }
//
//    // 4. 单文件上传接口，带 Authorization 校验
//    @PostMapping("/upload")
//    public ResponseEntity<?> uploadFile(
//            @RequestHeader(name = "Authorization") String authorization,
//            @RequestParam("file") MultipartFile file,
//            @RequestParam(value = "file_name", required = false) String fileName,
//            @RequestParam("file_type") FileType fileType,
//            @RequestParam("file_size") long fileSize
//    ) {
//        if (!isValidToken(authorization)) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
//        }
//        Integer userId = extractUserIdFromToken(authorization);
//        try {
//            fileStorage storedFile = fileService.uploadFile(userId, file, fileName, fileType, fileSize);
//            return ResponseEntity.ok(storedFile);
//        } catch (IOException e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body("File upload failed: " + e.getMessage());
//        }
//    }
//}
