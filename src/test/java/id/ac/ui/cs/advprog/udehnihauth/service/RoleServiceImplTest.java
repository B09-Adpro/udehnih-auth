package id.ac.ui.cs.advprog.udehnihauth.service;

import id.ac.ui.cs.advprog.udehnihauth.model.Role;
import id.ac.ui.cs.advprog.udehnihauth.model.RoleType;
import id.ac.ui.cs.advprog.udehnihauth.model.User;
import id.ac.ui.cs.advprog.udehnihauth.repository.RoleRepository;
import id.ac.ui.cs.advprog.udehnihauth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private RoleServiceImpl roleService;

    private User user;
    private User staff;
    private Role studentRole;
    private Role tutorRole;
    private Role staffRole;

    @BeforeEach
    void setUp() {
        studentRole = new Role();
        studentRole.setId(1L);
        studentRole.setName(RoleType.STUDENT);
        studentRole.setUsers(new HashSet<>());

        tutorRole = new Role();
        tutorRole.setId(2L);
        tutorRole.setName(RoleType.TUTOR);
        tutorRole.setUsers(new HashSet<>());

        staffRole = new Role();
        staffRole.setId(3L);
        staffRole.setName(RoleType.STAFF);
        staffRole.setUsers(new HashSet<>());

        user = new User();
        user.setId(1L);
        user.setEmail("user@example.com");
        user.setName("Test User");
        user.setPassword("password");
        user.setRegistrationDate(LocalDateTime.now());
        user.setRoles(new HashSet<>());
        user.getRoles().add(studentRole);

        staff = new User();
        staff.setId(2L);
        staff.setEmail("staff@example.com");
        staff.setName("Staff User");
        staff.setPassword("password");
        staff.setRegistrationDate(LocalDateTime.now());
        staff.setRoles(new HashSet<>());
        staff.getRoles().add(staffRole);

        studentRole.getUsers().add(user);
        staffRole.getUsers().add(staff);
    }

    @Test
    void addRoleToUser_WithValidStaffAndNewRole_ShouldReturnTrue() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(staff));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roleRepository.findByName(RoleType.TUTOR)).thenReturn(Optional.of(tutorRole));
        when(userRepository.save(any(User.class))).thenReturn(user);

        boolean result = roleService.addRoleToUser(1L, RoleType.TUTOR, 2L);

        assertTrue(result);
        verify(userRepository).save(user);
        verify(userRepository).findById(1L);
        verify(userRepository).findById(2L);
        verify(roleRepository).findByName(RoleType.TUTOR);
    }

    @Test
    void addRoleToUser_WithNonStaffUser_ShouldThrowSecurityException() {
        User nonStaff = new User();
        nonStaff.setId(3L);
        nonStaff.setRoles(new HashSet<>());
        nonStaff.getRoles().add(studentRole);

        when(userRepository.findById(3L)).thenReturn(Optional.of(nonStaff));

        SecurityException exception = assertThrows(SecurityException.class, () -> {
            roleService.addRoleToUser(1L, RoleType.TUTOR, 3L);
        });

        assertEquals("Only staff can add roles to users", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void addRoleToUser_WithExistingRole_ShouldReturnFalse() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(staff));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roleRepository.findByName(RoleType.STUDENT)).thenReturn(Optional.of(studentRole));

        boolean result = roleService.addRoleToUser(1L, RoleType.STUDENT, 2L);

        assertFalse(result);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void addRoleToUser_WithNonExistentUser_ShouldReturnFalse() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(staff));
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        boolean result = roleService.addRoleToUser(999L, RoleType.TUTOR, 2L);

        assertFalse(result);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void userHasRole_WithExistingRole_ShouldReturnTrue() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        boolean result = roleService.userHasRole(1L, RoleType.STUDENT);

        assertTrue(result);
        verify(userRepository).findById(1L);
    }

    @Test
    void userHasRole_WithNonExistingRole_ShouldReturnFalse() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        boolean result = roleService.userHasRole(1L, RoleType.TUTOR);

        assertFalse(result);
        verify(userRepository).findById(1L);
    }

    @Test
    void userHasRole_WithNonExistentUser_ShouldReturnFalse() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        boolean result = roleService.userHasRole(999L, RoleType.STUDENT);

        assertFalse(result);
        verify(userRepository).findById(999L);
    }

    @Test
    void getUserRoles_WithValidUser_ShouldReturnRoles() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        Set<RoleType> result = roleService.getUserRoles(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.contains(RoleType.STUDENT));
        verify(userRepository).findById(1L);
    }

    @Test
    void getUserRoles_WithNonExistentUser_ShouldReturnEmptySet() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        Set<RoleType> result = roleService.getUserRoles(999L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userRepository).findById(999L);
    }

    @Test
    void removeRoleFromUser_WithExistingRole_ShouldReturnTrue() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roleRepository.findByName(RoleType.STUDENT)).thenReturn(Optional.of(studentRole));
        when(userRepository.save(any(User.class))).thenReturn(user);

        boolean result = roleService.removeRoleFromUser(1L, RoleType.STUDENT);

        assertTrue(result);
        verify(userRepository).save(user);
        verify(userRepository).findById(1L);
        verify(roleRepository).findByName(RoleType.STUDENT);
    }

    @Test
    void removeRoleFromUser_WithNonExistingRole_ShouldReturnFalse() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roleRepository.findByName(RoleType.TUTOR)).thenReturn(Optional.of(tutorRole));

        boolean result = roleService.removeRoleFromUser(1L, RoleType.TUTOR);

        assertFalse(result);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void removeRoleFromUser_WithNonExistentUser_ShouldReturnFalse() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        boolean result = roleService.removeRoleFromUser(999L, RoleType.STUDENT);

        assertFalse(result);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void removeRoleFromUser_WithNonExistentRole_ShouldReturnFalse() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roleRepository.findByName(RoleType.TUTOR)).thenReturn(Optional.empty());

        boolean result = roleService.removeRoleFromUser(1L, RoleType.TUTOR);

        assertFalse(result);
        verify(userRepository, never()).save(any(User.class));
    }
}