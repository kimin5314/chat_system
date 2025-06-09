package com.example.springboot.entity;

import jakarta.persistence.*;
import lombok.Setter;
import lombok.Getter;
import com.example.springboot.Enum.PermissionType;

@Entity
@Getter
@Setter
@Table(name = "file_permission")
public class filePermission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "file_id", nullable = false, foreignKey = @ForeignKey(name = "fk_file_permission_file"))
    private fileStorage file;

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
    private User user;    @Enumerated(EnumType.STRING)
    @Column(name = "permission", length = 20, nullable = false)
    private PermissionType permission;

    // Getters and Setters for fields that Lombok isn't generating
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public fileStorage getFile() { return file; }
    public void setFile(fileStorage file) { this.file = file; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public PermissionType getPermission() { return permission; }
    public void setPermission(PermissionType permission) { this.permission = permission; }
}
