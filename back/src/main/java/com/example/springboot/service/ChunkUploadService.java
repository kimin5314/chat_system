// Service: ChunkUploadService.java
package com.example.springboot.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;

import java.io.*;
import java.nio.file.*;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChunkUploadService {
    private Path tempRoot;
    public ChunkUploadService(@Value("${file.upload-dir}") String uploadDir) throws IOException {
        this.tempRoot = Paths.get(uploadDir);
        Files.createDirectories(this.tempRoot);
    }
    /**
     * 获取已上传分片列表
     */
    public List<Integer> listUploadedChunks(String fileId) throws IOException {
        Path dir = tempRoot.resolve(fileId);
        if (!Files.exists(dir)) {
            return List.of();
        }
        return Files.list(dir)
                .filter(p -> p.getFileName().toString().endsWith(".part"))
                .map(p -> Integer.parseInt(
                        p.getFileName().toString().replace(".part", "")))
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * 保存单个分片
     */
    public void saveChunk(String fileId, int chunkIndex, InputStream in) throws IOException {
        Path dir = tempRoot.resolve(fileId);
        Files.createDirectories(dir);
        Path partPath = dir.resolve(chunkIndex + ".part");
        Files.copy(in, partPath, StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * 合并所有分片并清理临时目录
     */
    public Path mergeChunks(String fileId, String filename) throws IOException {
        Path dir = tempRoot.resolve(fileId);
        if (!Files.exists(dir)) {
            throw new NoSuchFileException("Chunk directory does not exist: " + dir.toString());
        }
        Path target = tempRoot.getParent().resolve("uploads").resolve(filename);
        Files.createDirectories(target.getParent());

        try (OutputStream out = Files.newOutputStream(target,
                StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
            Files.list(dir)
                    .filter(p -> p.getFileName().toString().endsWith(".part"))
                    .sorted(Comparator.comparingInt(
                            p -> Integer.parseInt(
                                    p.getFileName().toString().replace(".part", ""))))
                    .forEach(part -> {
                        try (InputStream inStream = Files.newInputStream(part)) {
                            byte[] buf = new byte[8 * 1024];
                            int len;
                            while ((len = inStream.read(buf)) != -1) {
                                out.write(buf, 0, len);
                            }
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    });
        }
        FileSystemUtils.deleteRecursively(dir);
        return target;
    }

}