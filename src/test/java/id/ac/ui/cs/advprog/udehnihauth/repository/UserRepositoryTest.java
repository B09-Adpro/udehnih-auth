package id.ac.ui.cs.advprog.udehnihauth.repository;

import id.ac.ui.cs.advprog.udehnihauth.model.Gender;
import id.ac.ui.cs.advprog.udehnihauth.model.Role;
import id.ac.ui.cs.advprog.udehnihauth.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Test
    void whenSaveUser_thenUserIsPersisted() {
        User user = User.builder()
                .email("test@example.com")
                .fullName("Test User")
                .password("password123")
                .registrationDate(LocalDateTime.now())
                .gender(Gender.PREFER_NOT_TO_SAY)
                .build();
        user.addRole(Role.STUDENT);

        User savedUser = userRepository.save(user);

        assertThat(savedUser.getId()).isNotNull();
    }

    @Test
    void whenFindByEmail_thenReturnUser() {
        User user = User.builder()
                .email("find@example.com")
                .fullName("Find User")
                .password("password123")
                .registrationDate(LocalDateTime.now())
                .build();
        user.addRole(Role.STUDENT);

        entityManager.persist(user);
        entityManager.flush();

        Optional<User> found = userRepository.findByEmail("find@example.com");

        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo(user.getEmail());
        assertThat(found.get().getFullName()).isEqualTo(user.getFullName());
    }

    @Test
    void whenExistsByEmail_thenReturnTrue() {
        User user = User.builder()
                .email("exists@example.com")
                .fullName("Exists User")
                .password("password123")
                .registrationDate(LocalDateTime.now())
                .build();

        entityManager.persist(user);
        entityManager.flush();

        boolean exists = userRepository.existsByEmail("exists@example.com");

        assertThat(exists).isTrue();
    }

    @Test
    void whenNonExistentEmail_thenReturnFalse() {
        boolean exists = userRepository.existsByEmail("nonexistent@example.com");

        assertThat(exists).isFalse();
    }

    @Test
    void whenFindByRole_thenReturnMatchingUsers() {
        User studentUser = User.builder()
                .email("student@example.com")
                .fullName("Student User")
                .password("password123")
                .registrationDate(LocalDateTime.now())
                .build();
        studentUser.addRole(Role.STUDENT);

        User tutorUser = User.builder()
                .email("tutor@example.com")
                .fullName("Tutor User")
                .password("password123")
                .registrationDate(LocalDateTime.now())
                .build();
        tutorUser.addRole(Role.TUTOR);

        User adminUser = User.builder()
                .email("admin@example.com")
                .fullName("Admin User")
                .password("password123")
                .registrationDate(LocalDateTime.now())
                .build();
        adminUser.addRole(Role.ADMIN);

        entityManager.persist(studentUser);
        entityManager.persist(tutorUser);
        entityManager.persist(adminUser);
        entityManager.flush();

        List<User> tutors = userRepository.findByRolesContaining(Role.TUTOR);

        assertThat(tutors).hasSize(1);
        assertThat(tutors).extracting(User::getEmail)
                .containsExactly("tutor@example.com");
    }

    @Test
    void whenDeleteUser_thenUserIsRemoved() {
        User user = User.builder()
                .email("delete@example.com")
                .fullName("Delete User")
                .password("password123")
                .registrationDate(LocalDateTime.now())
                .build();

        User savedUser = entityManager.persist(user);
        entityManager.flush();

        userRepository.deleteById(savedUser.getId());

        Optional<User> deletedUser = userRepository.findById(savedUser.getId());
        assertThat(deletedUser).isEmpty();
    }

    @Test
    void whenUpdateUser_thenChangesArePersisted() {
        User user = User.builder()
                .email("original@example.com")
                .fullName("Original Name")
                .password("password123")
                .registrationDate(LocalDateTime.now())
                .build();

        User savedUser = entityManager.persist(user);
        entityManager.flush();

        savedUser.setEmail("updated@example.com");
        savedUser.setFullName("Updated Name");
        userRepository.save(savedUser);

        User updatedUser = entityManager.find(User.class, savedUser.getId());

        assertThat(updatedUser.getEmail()).isEqualTo("updated@example.com");
        assertThat(updatedUser.getFullName()).isEqualTo("Updated Name");
    }
}