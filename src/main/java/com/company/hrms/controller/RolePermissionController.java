package com.company.hrms.controller;

import com.company.hrms.dto.common.ApiResponse;
import com.company.hrms.dto.role.PermissionDTO;
import com.company.hrms.dto.role.RoleDTO;
import com.company.hrms.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Affectations", description = "Employee management APIs")
@RestController
@RequestMapping("/v1/affectation")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class RolePermissionController {

    private final RoleService roleService;

    @Operation(summary = "Create role", description = "Create a new role")
    @PostMapping("/role")
    @PreAuthorize("hasAnyAuthority('EMPLOYEE_CREATE', 'ROLE_ADMIN', 'ROLE_HR_MANAGER')")
    public ResponseEntity<ApiResponse<RoleDTO>> createEmployee(
            @Valid @RequestBody RoleDTO roleDTO) {
        RoleDTO createdRole = roleService.createRole(roleDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Employee created successfully", createdRole));
    }

    @Operation(summary = "Create permission", description = "Create a new permission")
    @PostMapping("/permission")
    @PreAuthorize("hasAnyAuthority('EMPLOYEE_CREATE', 'ROLE_ADMIN', 'ROLE_HR_MANAGER')")
    public ResponseEntity<ApiResponse<List<PermissionDTO>>> createEmployee(
            @Valid @RequestBody List<PermissionDTO> permissionDTO) {
        List<PermissionDTO> createPermission = roleService.createPermission(permissionDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Employee created successfully", createPermission));
    }

//    @Operation(summary = "Update employee", description = "Update an existing employee")
//    @PutMapping("/{id}")
//    @PreAuthorize("hasAnyAuthority('EMPLOYEE_UPDATE', 'ROLE_ADMIN', 'ROLE_HR_MANAGER')")
//    public ResponseEntity<ApiResponse<EmployeeDTO>> updateEmployee(
//            @Parameter(description = "Employee ID") @PathVariable Long id,
//            @Valid @RequestBody EmployeeDTO employeeDTO) {
//        EmployeeDTO updatedEmployee = employeeService.updateEmployee(id, employeeDTO);
//        return ResponseEntity.ok(ApiResponse.success("Employee updated successfully", updatedEmployee));
//    }
}
