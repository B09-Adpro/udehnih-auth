package id.ac.ui.cs.advprog.udehnihauth.service;

import id.ac.ui.cs.advprog.udehnihauth.model.Role;
import id.ac.ui.cs.advprog.udehnihauth.model.RoleType;
import id.ac.ui.cs.advprog.udehnihauth.model.User;
import id.ac.ui.cs.advprog.udehnihauth.repository.RoleRepository;
import id.ac.ui.cs.advprog.udehnihauth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@Import(RoleServiceImpl.class)
class RoleServiceTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private RoleService roleService;

    private User user;
    private User staff;
    private Role studentRole;
    private Role tutorRole;
    private Role staffRole;

    @BeforeEach
    void setUp() {
        studentRole = new Role();
        studentRole.setName(RoleType.STUDENT);
        studentRole.setUsers(new HashSet<>());
        entityManager.persist(studentRole);

        tutorRole = new Role();
        tutorRole.setName(RoleType.TUTOR);
        tutorRole.setUsers(new HashSet<>());
        entityManager.persist(tutorRole);

        staffRole = new Role();
        staffRole.setName(RoleType.STAFF);
        staffRole.setUsers(new HashSet<>());
        entityManager.persist(staffRole);

        user = new User();
        user.setEmail("user@example.com");
        user.setName("Test User");
        user.setPassword("password");
        user.setRegistrationDate(LocalDateTime.now());
        user.setRoles(new HashSet<>());
        user.getRoles().add(studentRole);
        entityManager.persist(user);

        staff = new User();
        staff.setEmail("staff@example.com");
        staff.setName("Staff User");
        staff.setPassword("password");
        staff.setRegistrationDate(LocalDateTime.now());
        staff.setRoles(new HashSet<>());
        staff.getRoles().add(staffRole);
        entityManager.persist(staff);

        studentRole.getUsers().add(user);
        staffRole.getUsers().add(staff);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void addRoleToUser_WithStaffAndValidRole_ShouldReturnTrue() {
        boolean result = roleService.addRoleToUser(user.getId(), RoleType.TUTOR, staff.getId());

        assertTrue(result);

        User updatedUser = userRepository.findById(user.getId()).orElse(null);
        assertNotNull(updatedUser);
        assertTrue(updatedUser.getRoles().stream()
                .anyMatch(role -> role.getName() == RoleType.TUTOR));
    }

    @Test
    void addRoleToUser_WithNonStaffUser_ShouldThrowSecurityException() {
        User regularUser = new User();
        regularUser.setEmail("regular@example.com");
        regularUser.setName("Regular User");
        regularUser.setPassword("password");
        regularUser.setRegistrationDate(LocalDateTime.now());
        regularUser.setRoles(new HashSet<>());
        regularUser.getRoles().add(studentRole);
        entityManager.persist(regularUser);
        entityManager.flush();

        SecurityException exception = assertThrows(SecurityException.class, () -> {
            roleService.addRoleToUser(user.getId(), RoleType.TUTOR, regularUser.getId());
        });

        assertEquals("Only staff can add roles to users", exception.getMessage());
    }

    @Test
    void addRoleToUser_WithExistingRole_ShouldReturnFalse() {
        boolean result = roleService.addRoleToUser(user.getId(), RoleType.STUDENT, staff.getId());

        assertFalse(result);
    }

    @Test
    void addRoleToUser_WithNonExistentUser_ShouldReturnFalse() {
        boolean result = roleService.addRoleToUser(999L, RoleType.TUTOR, staff.getId());

        assertFalse(result);
    }

    @Test
    void userHasRole_WithExistingRole_ShouldReturnTrue() {
        boolean result = roleService.userHasRole(user.getId(), RoleType.STUDENT);

        assertTrue(result);
    }

    @Test
    void userHasRole_WithNonExistingRole_ShouldReturnFalse() {
        boolean result = roleService.userHasRole(user.getId(), RoleType.TUTOR);

        assertFalse(result);
    }

    @Test
    void userHasRole_WithNonExistentUser_ShouldReturnFalse() {
        boolean result = roleService.userHasRole(999L, RoleType.STUDENT);

        assertFalse(result);
    }

    @Test
    void getUserRoles_WithValidUser_ShouldReturnCorrectRoles() {
        Set<RoleType> roles = roleService.getUserRoles(user.getId());

        assertNotNull(roles);
        assertEquals(1, roles.size());
        assertTrue(roles.contains(RoleType.STUDENT));
    }

    @Test
    void getUserRoles_WithStaffUser_ShouldReturnStaffRole() {
        Set<RoleType> roles = roleService.getUserRoles(staff.getId());

        assertNotNull(roles);
        assertEquals(1, roles.size());
        assertTrue(roles.contains(RoleType.STAFF));
    }

    @Test
    void getUserRoles_WithNonExistentUser_ShouldReturnEmptySet() {
        Set<RoleType> roles = roleService.getUserRoles(999L);

        assertNotNull(roles);
        assertTrue(roles.isEmpty());
    }

    @Test
    void removeRoleFromUser_WithExistingRole_ShouldReturnTrue() {
        roleService.addRoleToUser(user.getId(), RoleType.TUTOR, staff.getId());

        boolean result = roleService.removeRoleFromUser(user.getId(), RoleType.TUTOR);

        assertTrue(result);

        User updatedUser = userRepository.findById(user.getId()).orElse(null);
        assertNotNull(updatedUser);
        assertFalse(updatedUser.getRoles().stream()
                .anyMatch(role -> role.getName() == RoleType.TUTOR));
    }

    @Test
    void removeRoleFromUser_WithNonExistingRole_ShouldReturnFalse() {
        boolean result = roleService.removeRoleFromUser(user.getId(), RoleType.TUTOR);

        assertFalse(result);
    }

    @Test
    void removeRoleFromUser_WithNonExistentUser_ShouldReturnFalse() {
        boolean result = roleService.removeRoleFromUser(999L, RoleType.STUDENT);

        assertFalse(result);
    }

    @Test
    void integrationTest_AddMultipleRoles_ShouldWork() {
        boolean addTutorResult = roleService.addRoleToUser(user.getId(), RoleType.TUTOR, staff.getId());
        assertTrue(addTutorResult);

        boolean addStaffResult = roleService.addRoleToUser(user.getId(), RoleType.STAFF, staff.getId());
        assertTrue(addStaffResult);

        Set<RoleType> roles = roleService.getUserRoles(user.getId());
        assertEquals(3, roles.size());
        assertTrue(roles.contains(RoleType.STUDENT));
        assertTrue(roles.contains(RoleType.TUTOR));
        assertTrue(roles.contains(RoleType.STAFF));

        assertTrue(roleService.userHasRole(user.getId(), RoleType.STUDENT));
        assertTrue(roleService.userHasRole(user.getId(), RoleType.TUTOR));
        assertTrue(roleService.userHasRole(user.getId(), RoleType.STAFF));
    }

    @Test
    void integrationTest_AddAndRemoveRoles_ShouldWork() {
        assertEquals(1, roleService.getUserRoles(user.getId()).size());

        assertTrue(roleService.addRoleToUser(user.getId(), RoleType.TUTOR, staff.getId()));
        assertEquals(2, roleService.getUserRoles(user.getId()).size());

        assertTrue(roleService.removeRoleFromUser(user.getId(), RoleType.STUDENT));
        assertEquals(1, roleService.getUserRoles(user.getId()).size());
        assertTrue(roleService.userHasRole(user.getId(), RoleType.TUTOR));
        assertFalse(roleService.userHasRole(user.getId(), RoleType.STUDENT));

        assertTrue(roleService.removeRoleFromUser(user.getId(), RoleType.TUTOR));
        assertTrue(roleService.getUserRoles(user.getId()).isEmpty());
    }

    @Test
    void integrationTest_StaffCanManageOwnRoles_ShouldWork() {
        boolean result = roleService.addRoleToUser(staff.getId(), RoleType.TUTOR, staff.getId());

        assertTrue(result);

        Set<RoleType> staffRoles = roleService.getUserRoles(staff.getId());
        assertEquals(2, staffRoles.size());
        assertTrue(staffRoles.contains(RoleType.STAFF));
        assertTrue(staffRoles.contains(RoleType.TUTOR));
    }

    @Test
    void integrationTest_MultipleStaffMembers_ShouldWork() {
        User staff2 = new User();
        staff2.setEmail("staff2@example.com");
        staff2.setName("Staff User 2");
        staff2.setPassword("password");
        staff2.setRegistrationDate(LocalDateTime.now());
        staff2.setRoles(new HashSet<>());
        staff2.getRoles().add(staffRole);
        entityManager.persist(staff2);
        entityManager.flush();

        assertTrue(roleService.addRoleToUser(user.getId(), RoleType.TUTOR, staff.getId()));
        assertTrue(roleService.addRoleToUser(user.getId(), RoleType.STAFF, staff2.getId()));

        Set<RoleType> userRoles = roleService.getUserRoles(user.getId());
        assertEquals(3, userRoles.size());
    }

    @Test
    void performanceTest_MultipleOperations_ShouldBeEfficient() {
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < 10; i++) {
            roleService.addRoleToUser(user.getId(), RoleType.TUTOR, staff.getId());
            roleService.removeRoleFromUser(user.getId(), RoleType.TUTOR);
            roleService.getUserRoles(user.getId());
            roleService.userHasRole(user.getId(), RoleType.STUDENT);
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        assertTrue(duration < 5000, "Operations took too long: " + duration + "ms");
    }
}