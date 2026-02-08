package com.company.hrms.service;

import com.company.hrms.dto.role.PermissionDTO;
import com.company.hrms.dto.role.RoleDTO;
import com.company.hrms.entity.Permission;
import com.company.hrms.entity.Role;
import com.company.hrms.exception.DuplicateResourceException;
import com.company.hrms.repository.PermissionRepository;
import com.company.hrms.repository.RoleRepository;
import com.company.hrms.service.audit.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final AuditService auditService;

    @CacheEvict(value = "roles", allEntries = true)
    public RoleDTO createRole(RoleDTO roleDTO) {
        // Validate unique constraints
        if (roleRepository.existsByName(roleDTO.getName())) {
            throw new DuplicateResourceException("Role", "roleName", roleDTO.getName());
        }

        Role role = convertToEntity(roleDTO);
        Role savedRole = roleRepository.save(role);

        // Log audit
        auditService.logAction("Role", savedRole.getId(), "CREATE", null, role);

        log.info("Role created successfully: {}", role.getName());
        return convertToDTO(savedRole);
    }

    @CacheEvict(value = "permissions", allEntries = true)
    public List<PermissionDTO> createPermission(List<PermissionDTO> permissionDTOS) {
        // Validate unique constraints
        for (PermissionDTO permissionDTO : permissionDTOS) {
            if (permissionRepository.existsByName(permissionDTO.getName())) {
                throw new DuplicateResourceException("Permission", "nom", permissionDTO.getName());
            }
        }

        List<Permission> savedPermissions = null;
        for (PermissionDTO permissionDTO : permissionDTOS) {
            Permission permission = convertPEntity(permissionDTO);
            Permission savedPermission = permissionRepository.save(permission);
            savedPermissions.add(savedPermission);
            // Log audit
            auditService.logAction("Role", savedPermission.getId(), "CREATE", null, permission);
            log.info("Role created successfully: {}", permission.getName());
        }

        return savedPermissions.stream().map(this::convertPDTO).toList();
    }

    private Role convertToEntity(RoleDTO roleDTO) {
        return Role.builder()
                .name(roleDTO.getName())
                .description(roleDTO.getDescription())
                .permissions(roleDTO.getPermissions())
                .build();
    }

    private RoleDTO convertToDTO(Role role) {
        return RoleDTO.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .createdAt(role.getCreatedAt())
                .updatedAt(role.getUpdatedAt())
                .createdBy(role.getCreatedBy())
                .updatedBy(role.getUpdatedBy())
                .build();
    }

    private Permission convertPEntity(PermissionDTO permissionDTO) {
        return Permission.builder()
                .name(permissionDTO.getName())
                .description(permissionDTO.getDescription())
                .build();
    }

    private PermissionDTO convertPDTO(Permission permission) {
        return PermissionDTO.builder()
                .id(permission.getId())
                .name(permission.getName())
                .description(permission.getDescription())
                .createdAt(permission.getCreatedAt())
                .updatedAt(permission.getUpdatedAt())
                .createdBy(permission.getCreatedBy())
                .updatedBy(permission.getUpdatedBy())
                .build();
    }
}
