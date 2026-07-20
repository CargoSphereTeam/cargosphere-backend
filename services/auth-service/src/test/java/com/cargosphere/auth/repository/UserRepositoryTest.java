package com.cargosphere.auth.repository;

import com.cargosphere.auth.entity.Role;
import com.cargosphere.auth.entity.User;
import com.cargosphere.auth.entity.UserStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Test
    void shouldSaveAndFindUserByEmail() {
        Role customerRole = roleRepository.findByName("ROLE_CLIENT")
                .orElseThrow();

        String uniqueEmail = "repo-test-" + UUID.randomUUID() + "@example.com";

        User user = new User();
        user.setFullName("Repository Test User");
        user.setEmail(uniqueEmail);
        user.setPasswordHash("hashed-password");
        user.setPhoneNumber("9876543210");
        user.setRole(customerRole);
        user.setStatus(UserStatus.ACTIVE);

        userRepository.save(user);

        Optional<User> foundUser = userRepository.findByEmail(uniqueEmail);

        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo(uniqueEmail);
        assertThat(foundUser.get().getRole().getName()).isEqualTo("ROLE_CLIENT");
    }

    @Test
    void existsByEmailShouldReturnTrueWhenEmailExists() {
        Role customerRole = roleRepository.findByName("ROLE_CLIENT")
                .orElseThrow();

        String uniqueEmail = "exists-test-" + UUID.randomUUID() + "@example.com";

        User user = new User();
        user.setFullName("Exists Test User");
        user.setEmail(uniqueEmail);
        user.setPasswordHash("hashed-password");
        user.setRole(customerRole);
        user.setStatus(UserStatus.ACTIVE);

        userRepository.save(user);

        boolean exists = userRepository.existsByEmail(uniqueEmail);

        assertThat(exists).isTrue();
    }
}
