package com.company.hrms.service;

import com.company.hrms.dto.common.PagedResponse;
import com.company.hrms.dto.employee.EmployeeDTO;
import com.company.hrms.entity.Department;
import com.company.hrms.entity.Employee;
import com.company.hrms.enums.EmployeeStatus;
import com.company.hrms.exception.BadRequestException;
import com.company.hrms.exception.DuplicateResourceException;
import com.company.hrms.exception.ResourceNotFoundException;
import com.company.hrms.repository.DepartmentRepository;
import com.company.hrms.repository.EmployeeRepository;
import com.company.hrms.service.audit.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    @Cacheable(value = "employees", key = "#id")
    public EmployeeDTO getEmployeeById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .filter(e -> !e.getDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));
        
        return convertToDTO(employee);
    }

    @Transactional(readOnly = true)
    public EmployeeDTO getEmployeeByNumber(String employeeNumber) {
        Employee employee = employeeRepository.findByEmployeeNumberAndDeletedFalse(employeeNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "employeeNumber", employeeNumber));
        
        return convertToDTO(employee);
    }

    @Transactional(readOnly = true)
    public PagedResponse<EmployeeDTO> getAllEmployees(Pageable pageable) {
        Page<Employee> employeePage = employeeRepository.findAll(pageable);
        List<EmployeeDTO> employees = employeePage.getContent().stream()
                .filter(e -> !e.getDeleted())
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        return PagedResponse.of(
                employees,
                employeePage.getNumber(),
                employeePage.getSize(),
                employeePage.getTotalElements(),
                employeePage.getTotalPages()
        );
    }

    @Transactional(readOnly = true)
    public PagedResponse<EmployeeDTO> searchEmployees(String firstName, String lastName, 
                                                      String email, Long departmentId, 
                                                      EmployeeStatus status, Pageable pageable) {
        Page<Employee> employeePage = employeeRepository.advancedSearch(
                firstName, lastName, email, departmentId, status, pageable);
        
        List<EmployeeDTO> employees = employeePage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        return PagedResponse.of(
                employees,
                employeePage.getNumber(),
                employeePage.getSize(),
                employeePage.getTotalElements(),
                employeePage.getTotalPages()
        );
    }

    @Transactional(readOnly = true)
    public PagedResponse<EmployeeDTO> getEmployeesByDepartment(Long departmentId, Pageable pageable) {
        if (!departmentRepository.existsById(departmentId)) {
            throw new ResourceNotFoundException("Department", "id", departmentId);
        }
        
        Page<Employee> employeePage = employeeRepository.findByDepartmentId(departmentId, pageable);
        List<EmployeeDTO> employees = employeePage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        return PagedResponse.of(
                employees,
                employeePage.getNumber(),
                employeePage.getSize(),
                employeePage.getTotalElements(),
                employeePage.getTotalPages()
        );
    }

    @Transactional
    @CacheEvict(value = "employees", allEntries = true)
    public EmployeeDTO createEmployee(EmployeeDTO employeeDTO) {
        // Validate unique constraints
        if (employeeRepository.existsByEmployeeNumber(employeeDTO.getEmployeeNumber())) {
            throw new DuplicateResourceException("Employee", "employeeNumber", employeeDTO.getEmployeeNumber());
        }
        
        if (employeeRepository.existsByEmail(employeeDTO.getEmail())) {
            throw new DuplicateResourceException("Employee", "email", employeeDTO.getEmail());
        }

        Employee employee = convertToEntity(employeeDTO);
        Employee savedEmployee = employeeRepository.save(employee);
        
        // Log audit
        auditService.logAction("Employee", savedEmployee.getId(), "CREATE", null, savedEmployee);
        
        log.info("Employee created successfully: {}", savedEmployee.getEmployeeNumber());
        return convertToDTO(savedEmployee);
    }

    @Transactional
    @CacheEvict(value = "employees", key = "#id")
    public EmployeeDTO updateEmployee(Long id, EmployeeDTO employeeDTO) {
        Employee existingEmployee = employeeRepository.findById(id)
                .filter(e -> !e.getDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));

        // Store old values for audit
        Employee oldEmployee = copyEmployee(existingEmployee);

        // Validate unique constraints
        if (employeeRepository.existsByEmployeeNumberAndIdNot(employeeDTO.getEmployeeNumber(), id)) {
            throw new DuplicateResourceException("Employee", "employeeNumber", employeeDTO.getEmployeeNumber());
        }
        
        if (employeeRepository.existsByEmailAndIdNot(employeeDTO.getEmail(), id)) {
            throw new DuplicateResourceException("Employee", "email", employeeDTO.getEmail());
        }

        // Update fields
        updateEmployeeFields(existingEmployee, employeeDTO);
        Employee updatedEmployee = employeeRepository.save(existingEmployee);
        
        // Log audit
        auditService.logAction("Employee", updatedEmployee.getId(), "UPDATE", oldEmployee, updatedEmployee);
        
        log.info("Employee updated successfully: {}", updatedEmployee.getEmployeeNumber());
        return convertToDTO(updatedEmployee);
    }

    @Transactional
    @CacheEvict(value = "employees", key = "#id")
    public void deleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .filter(e -> !e.getDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));

        // Soft delete
        LocalDateTime now = LocalDateTime.now();
        String currentUser = auditService.getCurrentUsername();
        employeeRepository.softDelete(id, now, currentUser);
        
        // Log audit
        auditService.logAction("Employee", id, "DELETE", employee, null);
        
        log.info("Employee deleted successfully: {}", employee.getEmployeeNumber());
    }

    @Transactional
    @CacheEvict(value = "employees", key = "#employeeId")
    public EmployeeDTO updateEmployeeStatus(Long employeeId, EmployeeStatus newStatus) {
        Employee employee = employeeRepository.findById(employeeId)
                .filter(e -> !e.getDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", employeeId));

        EmployeeStatus oldStatus = employee.getStatus();
        employee.setStatus(newStatus);
        
        if (newStatus == EmployeeStatus.TERMINATED && employee.getTerminationDate() == null) {
            employee.setTerminationDate(java.time.LocalDate.now());
        }
        
        Employee updatedEmployee = employeeRepository.save(employee);
        
        // Log audit
        auditService.logAction("Employee", employeeId, "STATUS_CHANGE", 
                "Status changed from " + oldStatus + " to " + newStatus, updatedEmployee);
        
        log.info("Employee status updated: {} - {} to {}", 
                employee.getEmployeeNumber(), oldStatus, newStatus);
        
        return convertToDTO(updatedEmployee);
    }

    @Transactional(readOnly = true)
    public List<EmployeeDTO> getEmployeesByManager(Long managerId) {
        return employeeRepository.findByManagerId(managerId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PagedResponse<EmployeeDTO> fullTextSearch(String searchTerm, Pageable pageable) {
        Page<Employee> employeePage = employeeRepository.fullTextSearch(searchTerm, pageable);
        List<EmployeeDTO> employees = employeePage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        return PagedResponse.of(
                employees,
                employeePage.getNumber(),
                employeePage.getSize(),
                employeePage.getTotalElements(),
                employeePage.getTotalPages()
        );
    }

    // Helper methods
    private EmployeeDTO convertToDTO(Employee employee) {
        return EmployeeDTO.builder()
                .id(employee.getId())
                .employeeNumber(employee.getEmployeeNumber())
                .firstName(employee.getFirstName())
                .middleName(employee.getMiddleName())
                .lastName(employee.getLastName())
                .email(employee.getEmail())
                .phoneNumber(employee.getPhoneNumber())
                .dateOfBirth(employee.getDateOfBirth())
                .gender(employee.getGender())
                .nationality(employee.getNationality())
                .nationalId(employee.getNationalId())
                .passportNumber(employee.getPassportNumber())
                .address(employee.getAddress())
                .departmentId(employee.getDepartment() != null ? employee.getDepartment().getId() : null)
                .departmentName(employee.getDepartment() != null ? employee.getDepartment().getName() : null)
                .jobTitle(employee.getJobTitle())
                .jobLevel(employee.getJobLevel())
                .managerId(employee.getManager() != null ? employee.getManager().getId() : null)
                .managerName(employee.getManager() != null ? employee.getManager().getFullName() : null)
                .status(employee.getStatus())
                .hireDate(employee.getHireDate())
                .terminationDate(employee.getTerminationDate())
                .contractType(employee.getContractType())
                .employmentType(employee.getEmploymentType())
                .salary(employee.getSalary())
                .currency(employee.getCurrency())
                .emergencyContact(employee.getEmergencyContact())
                .skills(employee.getSkills())
                .education(employee.getEducation())
                .workHistory(employee.getWorkHistory())
                .documents(employee.getDocuments())
                .customFields(employee.getCustomFields())
                .userId(employee.getUser() != null ? employee.getUser().getId() : null)
                .username(employee.getUser() != null ? employee.getUser().getUsername() : null)
                .profilePictureUrl(employee.getProfilePictureUrl())
                .notes(employee.getNotes())
                .probationEndDate(employee.getProbationEndDate())
                .workLocation(employee.getWorkLocation())
                .workSchedule(employee.getWorkSchedule())
                .overtimeEligible(employee.getOvertimeEligible())
                .remoteWorkEligible(employee.getRemoteWorkEligible())
                .createdAt(employee.getCreatedAt())
                .updatedAt(employee.getUpdatedAt())
                .createdBy(employee.getCreatedBy())
                .updatedBy(employee.getUpdatedBy())
                .fullName(employee.getFullName())
                .yearsOfService(employee.getYearsOfService())
                .build();
    }

    private Employee convertToEntity(EmployeeDTO dto) {
        Employee employee = Employee.builder()
                .employeeNumber(dto.getEmployeeNumber())
                .firstName(dto.getFirstName())
                .middleName(dto.getMiddleName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .phoneNumber(dto.getPhoneNumber())
                .dateOfBirth(dto.getDateOfBirth())
                .gender(dto.getGender())
                .nationality(dto.getNationality())
                .nationalId(dto.getNationalId())
                .passportNumber(dto.getPassportNumber())
                .address(dto.getAddress())
                .jobTitle(dto.getJobTitle())
                .jobLevel(dto.getJobLevel())
                .status(dto.getStatus())
                .hireDate(dto.getHireDate())
                .terminationDate(dto.getTerminationDate())
                .contractType(dto.getContractType())
                .employmentType(dto.getEmploymentType())
                .salary(dto.getSalary())
                .currency(dto.getCurrency())
                .emergencyContact(dto.getEmergencyContact())
                .skills(dto.getSkills())
                .education(dto.getEducation())
                .workHistory(dto.getWorkHistory())
                .documents(dto.getDocuments())
                .customFields(dto.getCustomFields())
                .profilePictureUrl(dto.getProfilePictureUrl())
                .notes(dto.getNotes())
                .probationEndDate(dto.getProbationEndDate())
                .workLocation(dto.getWorkLocation())
                .workSchedule(dto.getWorkSchedule())
                .overtimeEligible(dto.getOvertimeEligible())
                .remoteWorkEligible(dto.getRemoteWorkEligible())
                .build();

        // Set department if provided
        if (dto.getDepartmentId() != null) {
            Department department = departmentRepository.findById(dto.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department", "id", dto.getDepartmentId()));
            employee.setDepartment(department);
        }

        // Set manager if provided
        if (dto.getManagerId() != null) {
            Employee manager = employeeRepository.findById(dto.getManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", dto.getManagerId()));
            employee.setManager(manager);
        }

        return employee;
    }

    private void updateEmployeeFields(Employee employee, EmployeeDTO dto) {
        employee.setEmployeeNumber(dto.getEmployeeNumber());
        employee.setFirstName(dto.getFirstName());
        employee.setMiddleName(dto.getMiddleName());
        employee.setLastName(dto.getLastName());
        employee.setEmail(dto.getEmail());
        employee.setPhoneNumber(dto.getPhoneNumber());
        employee.setDateOfBirth(dto.getDateOfBirth());
        employee.setGender(dto.getGender());
        employee.setNationality(dto.getNationality());
        employee.setNationalId(dto.getNationalId());
        employee.setPassportNumber(dto.getPassportNumber());
        employee.setAddress(dto.getAddress());
        employee.setJobTitle(dto.getJobTitle());
        employee.setJobLevel(dto.getJobLevel());
        employee.setStatus(dto.getStatus());
        employee.setHireDate(dto.getHireDate());
        employee.setTerminationDate(dto.getTerminationDate());
        employee.setContractType(dto.getContractType());
        employee.setEmploymentType(dto.getEmploymentType());
        employee.setSalary(dto.getSalary());
        employee.setCurrency(dto.getCurrency());
        employee.setEmergencyContact(dto.getEmergencyContact());
        employee.setSkills(dto.getSkills());
        employee.setEducation(dto.getEducation());
        employee.setWorkHistory(dto.getWorkHistory());
        employee.setDocuments(dto.getDocuments());
        employee.setCustomFields(dto.getCustomFields());
        employee.setProfilePictureUrl(dto.getProfilePictureUrl());
        employee.setNotes(dto.getNotes());
        employee.setProbationEndDate(dto.getProbationEndDate());
        employee.setWorkLocation(dto.getWorkLocation());
        employee.setWorkSchedule(dto.getWorkSchedule());
        employee.setOvertimeEligible(dto.getOvertimeEligible());
        employee.setRemoteWorkEligible(dto.getRemoteWorkEligible());

        // Update department if changed
        if (dto.getDepartmentId() != null) {
            if (employee.getDepartment() == null || !employee.getDepartment().getId().equals(dto.getDepartmentId())) {
                Department department = departmentRepository.findById(dto.getDepartmentId())
                        .orElseThrow(() -> new ResourceNotFoundException("Department", "id", dto.getDepartmentId()));
                employee.setDepartment(department);
            }
        } else {
            employee.setDepartment(null);
        }

        // Update manager if changed
        if (dto.getManagerId() != null) {
            if (employee.getManager() == null || !employee.getManager().getId().equals(dto.getManagerId())) {
                Employee manager = employeeRepository.findById(dto.getManagerId())
                        .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", dto.getManagerId()));
                employee.setManager(manager);
            }
        } else {
            employee.setManager(null);
        }
    }

    private Employee copyEmployee(Employee employee) {
        // Simple shallow copy for audit purposes
        return Employee.builder()
                .id(employee.getId())
                .employeeNumber(employee.getEmployeeNumber())
                .firstName(employee.getFirstName())
                .lastName(employee.getLastName())
                .email(employee.getEmail())
                .status(employee.getStatus())
                .build();
    }
}
