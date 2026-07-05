package com.cargosphere.auth.repository;

import com.cargosphere.auth.entity.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class RoleRepositoryTest {

    @Autowired
    private RoleRepository roleRepository;

    @Test
    void shouldFindDefaultAdminRoleAfterFlywayMigration() {
        Optional<Role> role = roleRepository.findByName("ROLE_ADMIN");

        assertThat(role).isPresent();
        assertThat(role.get().getName()).isEqualTo("ROLE_ADMIN");
    }
}