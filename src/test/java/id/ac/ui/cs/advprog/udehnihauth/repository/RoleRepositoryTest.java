package id.ac.ui.cs.advprog.udehnihauth.repository;

import id.ac.ui.cs.advprog.udehnihauth.model.Role;
import id.ac.ui.cs.advprog.udehnihauth.model.RoleType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class RoleRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private RoleRepository roleRepository;

    @Test
    void testFindByName_whenRoleExists_shouldReturnRole() {
        Role role = new Role(RoleType.STUDENT);
        entityManager.persist(role);
        entityManager.flush();

        Optional<Role> found = roleRepository.findByName(RoleType.STUDENT);

        assertTrue(found.isPresent());
        assertEquals(RoleType.STUDENT, found.get().getName());
    }

    @Test
    void testFindByName_whenRoleDoesNotExist_shouldReturnEmpty() {
        Optional<Role> found = roleRepository.findByName(RoleType.STAFF);
        assertFalse(found.isPresent());
    }
}
