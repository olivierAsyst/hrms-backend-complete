package com.company.hrms.controller;

import com.company.hrms.dto.common.ApiResponse;
import com.company.hrms.dto.common.PagedResponse;
import com.company.hrms.dto.employee.EmployeeDTO;
import com.company.hrms.enums.EmployeeStatus;
import com.company.hrms.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Employees", description = "Employee management APIs")
@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class EmployeeController {

    private final EmployeeService employeeService;

    @Operation(summary = "Get all employees", description = "Get a paginated list of all employees")
    @GetMapping
    @PreAuthorize("hasAnyAuthority('EMPLOYEE_READ', 'ROLE_ADMIN', 'ROLE_HR_MANAGER')")
    public ResponseEntity<ApiResponse<PagedResponse<EmployeeDTO>>> getAllEmployees(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort by field") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "ASC") String direction) {
        
        Sort.Direction sortDirection = direction.equalsIgnoreCase("DESC") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        
        PagedResponse<EmployeeDTO> employees = employeeService.getAllEmployees(pageable);
        return ResponseEntity.ok(ApiResponse.success(employees));
    }

    @Operation(summary = "Get employee by ID", description = "Get a single employee by their ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('EMPLOYEE_READ', 'ROLE_ADMIN', 'ROLE_HR_MANAGER')")
    public ResponseEntity<ApiResponse<EmployeeDTO>> getEmployeeById(
            @Parameter(description = "Employee ID") @PathVariable Long id) {
        EmployeeDTO employee = employeeService.getEmployeeById(id);
        return ResponseEntity.ok(ApiResponse.success(employee));
    }

    @Operation(summary = "Get employee by number", description = "Get a single employee by their employee number")
    @GetMapping("/number/{employeeNumber}")
    @PreAuthorize("hasAnyAuthority('EMPLOYEE_READ', 'ROLE_ADMIN', 'ROLE_HR_MANAGER')")
    public ResponseEntity<ApiResponse<EmployeeDTO>> getEmployeeByNumber(
            @Parameter(description = "Employee number") @PathVariable String employeeNumber) {
        EmployeeDTO employee = employeeService.getEmployeeByNumber(employeeNumber);
        return ResponseEntity.ok(ApiResponse.success(employee));
    }

    @Operation(summary = "Search employees", description = "Advanced search with multiple filters")
    @GetMapping("/search")
    @PreAuthorize("hasAnyAuthority('EMPLOYEE_READ', 'ROLE_ADMIN', 'ROLE_HR_MANAGER')")
    public ResponseEntity<ApiResponse<PagedResponse<EmployeeDTO>>> searchEmployees(
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) EmployeeStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        PagedResponse<EmployeeDTO> employees = employeeService.searchEmployees(
                firstName, lastName, email, departmentId, status, pageable);
        return ResponseEntity.ok(ApiResponse.success(employees));
    }

    @Operation(summary = "Full-text search", description = "Search employees using PostgreSQL full-text search")
    @GetMapping("/fulltext-search")
    @PreAuthorize("hasAnyAuthority('EMPLOYEE_READ', 'ROLE_ADMIN', 'ROLE_HR_MANAGER')")
    public ResponseEntity<ApiResponse<PagedResponse<EmployeeDTO>>> fullTextSearch(
            @Parameter(description = "Search term") @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        PagedResponse<EmployeeDTO> employees = employeeService.fullTextSearch(q, pageable);
        return ResponseEntity.ok(ApiResponse.success(employees));
    }

    @Operation(summary = "Get employees by department", description = "Get all employees in a specific department")
    @GetMapping("/department/{departmentId}")
    @PreAuthorize("hasAnyAuthority('EMPLOYEE_READ', 'ROLE_ADMIN', 'ROLE_HR_MANAGER', 'ROLE_MANAGER')")
    public ResponseEntity<ApiResponse<PagedResponse<EmployeeDTO>>> getEmployeesByDepartment(
            @Parameter(description = "Department ID") @PathVariable Long departmentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        PagedResponse<EmployeeDTO> employees = employeeService.getEmployeesByDepartment(departmentId, pageable);
        return ResponseEntity.ok(ApiResponse.success(employees));
    }

    @Operation(summary = "Get employees by manager", description = "Get all employees reporting to a specific manager")
    @GetMapping("/manager/{managerId}")
    @PreAuthorize("hasAnyAuthority('EMPLOYEE_READ', 'ROLE_ADMIN', 'ROLE_HR_MANAGER', 'ROLE_MANAGER')")
    public ResponseEntity<ApiResponse<List<EmployeeDTO>>> getEmployeesByManager(
            @Parameter(description = "Manager ID") @PathVariable Long managerId) {
        List<EmployeeDTO> employees = employeeService.getEmployeesByManager(managerId);
        return ResponseEntity.ok(ApiResponse.success(employees));
    }

    @Operation(summary = "Create employee", description = "Create a new employee")
    @PostMapping
    @PreAuthorize("hasAnyAuthority('EMPLOYEE_CREATE', 'ROLE_ADMIN', 'ROLE_HR_MANAGER')")
    public ResponseEntity<ApiResponse<EmployeeDTO>> createEmployee(
            @Valid @RequestBody EmployeeDTO employeeDTO) {
        EmployeeDTO createdEmployee = employeeService.createEmployee(employeeDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Employee created successfully", createdEmployee));
    }

    @Operation(summary = "Update employee", description = "Update an existing employee")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('EMPLOYEE_UPDATE', 'ROLE_ADMIN', 'ROLE_HR_MANAGER')")
    public ResponseEntity<ApiResponse<EmployeeDTO>> updateEmployee(
            @Parameter(description = "Employee ID") @PathVariable Long id,
            @Valid @RequestBody EmployeeDTO employeeDTO) {
        EmployeeDTO updatedEmployee = employeeService.updateEmployee(id, employeeDTO);
        return ResponseEntity.ok(ApiResponse.success("Employee updated successfully", updatedEmployee));
    }

    @Operation(summary = "Update employee status", description = "Update only the status of an employee")
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyAuthority('EMPLOYEE_UPDATE', 'ROLE_ADMIN', 'ROLE_HR_MANAGER')")
    public ResponseEntity<ApiResponse<EmployeeDTO>> updateEmployeeStatus(
            @Parameter(description = "Employee ID") @PathVariable Long id,
            @Parameter(description = "New status") @RequestParam EmployeeStatus status) {
        EmployeeDTO updatedEmployee = employeeService.updateEmployeeStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Employee status updated successfully", updatedEmployee));
    }

    @Operation(summary = "Delete employee", description = "Soft delete an employee")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('EMPLOYEE_DELETE', 'ROLE_ADMIN', 'ROLE_HR_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> deleteEmployee(
            @Parameter(description = "Employee ID") @PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.ok(ApiResponse.success("Employee deleted successfully", null));
    }
}
