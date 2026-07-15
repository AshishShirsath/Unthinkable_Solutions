package com.ashish.rentflatmatefinder.config;

import com.ashish.rentflatmatefinder.entity.Role;
import com.ashish.rentflatmatefinder.entity.RoleName;
import com.ashish.rentflatmatefinder.entity.User;
import com.ashish.rentflatmatefinder.repository.RoleRepository;
import com.ashish.rentflatmatefinder.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RoleInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email:admin@rentflatmatefinder.com}")
    private String adminEmail;

    @Value("${app.admin.password:Admin123!}")
    private String adminPassword;

    @Value("${app.admin.first-name:Admin}")
    private String adminFirstName;

    @Value("${app.admin.last-name:User}")
    private String adminLastName;

    @Override
    public void run(String... args) {
        Role adminRole = createRole(RoleName.ADMIN);
        createRole(RoleName.OWNER);
        createRole(RoleName.TENANT);

        if (!userRepository.existsByEmail(adminEmail)) {
            User adminUser = new User();
            adminUser.setFirstName(adminFirstName);
            adminUser.setLastName(adminLastName);
            adminUser.setEmail(adminEmail);
            adminUser.setPassword(passwordEncoder.encode(adminPassword));
            adminUser.setPhoneNumber("0000000000");
            adminUser.setEnabled(true);
            adminUser.setRole(adminRole);
            userRepository.save(adminUser);
        }
    }

    private Role createRole(RoleName roleName) {
        return roleRepository.findByName(roleName).orElseGet(() -> {
            Role role = new Role();
            role.setName(roleName);
            return roleRepository.save(role);
        });
    }
}
