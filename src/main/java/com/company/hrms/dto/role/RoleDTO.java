package com.company.hrms.dto.role;

import com.company.hrms.entity.Permission;
import com.company.hrms.enums.RoleType;
import jakarta.persistence.Column;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleDTO {
    private Long id;
    private RoleType name;
    private String description;
    private Set<Permission> permissions;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
    private Long version; // Optimistic locking
    private Boolean deleted = false;
    private LocalDateTime deletedAt;
    private String deletedBy;
}
