package com.company.hrms.enums;

public enum PermissionType {
    // Employee permissions
    EMPLOYEE_READ,
    EMPLOYEE_CREATE,
    EMPLOYEE_UPDATE,
    EMPLOYEE_DELETE,
    
    // Department permissions
    DEPARTMENT_READ,
    DEPARTMENT_CREATE,
    DEPARTMENT_UPDATE,
    DEPARTMENT_DELETE,
    
    // User management permissions
    USER_READ,
    USER_CREATE,
    USER_UPDATE,
    USER_DELETE,
    
    // Role management permissions
    ROLE_READ,
    ROLE_CREATE,
    ROLE_UPDATE,
    ROLE_DELETE,
    
    // Audit permissions
    AUDIT_READ,
    
    // System permissions
    SYSTEM_SETTINGS
}
