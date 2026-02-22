package com.company.hrms;

import com.company.hrms.dto.auth.RegisterRequest;
import com.company.hrms.entity.Permission;
import com.company.hrms.entity.Role;
import com.company.hrms.enums.PermissionType;
import com.company.hrms.enums.RoleType;
import com.company.hrms.repository.PermissionRepository;
import com.company.hrms.repository.RoleRepository;
import com.company.hrms.service.AuthService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.*;

import static com.company.hrms.enums.PermissionType.*;

@SpringBootApplication
@EnableJpaAuditing
@EnableCaching
public class HrmsBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(HrmsBackendApplication.class, args);
    }

//    @Bean
    CommandLineRunner start(
            PermissionRepository permissionRepository,
            RoleRepository roleRepository,
            AuthService authService
            ) {
        return args -> {

            if (roleRepository.findAll().isEmpty()) {
                List<PermissionType> permissionTypes = Arrays.asList(
                        EMPLOYEE_READ,
                        EMPLOYEE_CREATE,
                        EMPLOYEE_UPDATE,
                        EMPLOYEE_DELETE,
                        DEPARTMENT_READ,
                        DEPARTMENT_CREATE,
                        DEPARTMENT_UPDATE,
                        DEPARTMENT_DELETE,
                        USER_READ,
                        USER_CREATE,
                        USER_UPDATE,
                        USER_DELETE,
                        ROLE_READ,
                        ROLE_CREATE,
                        ROLE_UPDATE,
                        ROLE_DELETE,
                        AUDIT_READ,
                        SYSTEM_SETTINGS
                );
                List<Permission> permissions = new ArrayList<>();
                for (PermissionType permissionType : permissionTypes) {
                    Permission permission = new Permission();
                    String desc = "";
                    if (permissionType.toString().contains("READ")){
                        desc = "Lire";
                    }else if (permissionType.toString().contains("CREATE")){
                        desc = "Créer";
                    }else if (permissionType.toString().contains("UPDATE")){
                        desc = "Modifier";
                    }else if (permissionType.toString().contains("DELETE")){
                        desc = "Supprimer";
                    } else {
                        desc = permissionType.name();
                    }
                    permission.setName(permissionType);
                    permission.setDescription("Permission de "+desc);
                    permissions.add(permission);
                }
                permissionRepository.saveAll(permissions);
                List<Permission> all = permissionRepository.findAll();
                Role role = new Role();
                role.setName(RoleType.ROLE_SUPER_ADMIN);
                role.setDescription("Role super admin");
                role.setPermissions(new HashSet<>(all));
                roleRepository.save(role);
                RegisterRequest user = new RegisterRequest(
                        "Bonix",
                        "bonheur@ymail.com",
                        "Test@123",
                        "Bonheur",
                        "Bagule",
                        "0999644524",
                        Set.of("ROLE_SUPER_ADMIN")
                );
                authService.register(user);
            }

        };
    }
}
