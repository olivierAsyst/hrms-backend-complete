package com.company.hrms.entity;

import com.company.hrms.enums.PermissionType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "permissions", indexes = {
    @Index(name = "idx_permission_name", columnList = "name")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permission extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "name", nullable = false, unique = true, length = 50)
    private PermissionType name;

    @Column(name = "description", length = 500)
    private String description;
}
