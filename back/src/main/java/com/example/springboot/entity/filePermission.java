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
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "permission", length = 20, nullable = false)
    private PermissionType permission;

}
