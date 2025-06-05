package com.example.springboot.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import com.example.springboot.Enum.FileType;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "file_storage")
public class fileStorage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @ManyToOne
    @JoinColumn(
            name = "user_id",
            referencedColumnName = "id",
            insertable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "fk_file_storage_user")
    )
    private User user;


    @Column(name = "file_name", columnDefinition = "TEXT")
    private String fileName;

    @Column(name = "file_url", nullable = false, unique = true, columnDefinition = "TEXT")
    private String fileUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "file_type", length = 20, nullable = false)
    private FileType fileType;

    @Column(name = "file_size")
    private long fileSize;

    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private LocalDateTime uploadedAt = LocalDateTime.now();

}

