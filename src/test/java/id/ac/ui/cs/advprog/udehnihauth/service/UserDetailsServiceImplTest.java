package id.ac.ui.cs.advprog.udehnihauth.service;

import id.ac.ui.cs.advprog.udehnihauth.model.Role;
import id.ac.ui.cs.advprog.udehnihauth.model.RoleType;
import id.ac.ui.cs.advprog.udehnihauth.model.User;
import id.ac.ui.cs.advprog.udehnihauth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    private User user;
    private Role studentRole;
    private Role tutorRole;

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

        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setName("Test User");
        user.setPassword("encoded_password");
        user.setRegistrationDate(LocalDateTime.now());
        user.setRoles(new HashSet<>());
        user.getRoles().add(studentRole);
        user.getRoles().add(tutorRole);

        studentRole.getUsers().add(user);
        tutorRole.getUsers().add(user);
    }

    @Test
    void loadUserByUsername_WithExistingUser_ShouldReturnUserDetails() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        UserDetails userDetails = userDetailsService.loadUserByUsername("test@example.com");

        assertNotNull(userDetails);
        assertEquals("test@example.com", userDetails.getUsername());
        assertEquals("encoded_password", userDetails.getPassword());

        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        assertEquals(2, authorities.size());

        Set<String> authorityNames = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        assertTrue(authorityNames.contains("ROLE_STUDENT"));
        assertTrue(authorityNames.contains("ROLE_TUTOR"));

        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void loadUserByUsername_WithNonExistentUser_ShouldThrowException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        Exception exception = assertThrows(UsernameNotFoundException.class, () -> {
            userDetailsService.loadUserByUsername("nonexistent@example.com");
        });

        assertEquals("User not found with email: nonexistent@example.com", exception.getMessage());
        verify(userRepository).findByEmail("nonexistent@example.com");
    }

    @Test
    void loadUserByUsername_WithNoRoles_ShouldReturnUserWithoutAuthorities() {
        User userWithNoRoles = new User();
        userWithNoRoles.setId(2L);
        userWithNoRoles.setEmail("noroles@example.com");
        userWithNoRoles.setName("No Roles User");
        userWithNoRoles.setPassword("encoded_password");
        userWithNoRoles.setRegistrationDate(LocalDateTime.now());
        userWithNoRoles.setRoles(new HashSet<>());

        when(userRepository.findByEmail("noroles@example.com")).thenReturn(Optional.of(userWithNoRoles));

        UserDetails userDetails = userDetailsService.loadUserByUsername("noroles@example.com");

        assertNotNull(userDetails);
        assertEquals("noroles@example.com", userDetails.getUsername());
        assertEquals("encoded_password", userDetails.getPassword());

        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        assertTrue(authorities.isEmpty());

        verify(userRepository).findByEmail("noroles@example.com");
    }

    @Test
    void loadUserByUsername_WithSpecificRole_ShouldHaveCorrectAuthorities() {
        User staffUser = new User();
        staffUser.setId(3L);
        staffUser.setEmail("staff@example.com");
        staffUser.setName("Staff User");
        staffUser.setPassword("encoded_password");
        staffUser.setRegistrationDate(LocalDateTime.now());
        staffUser.setRoles(new HashSet<>());

        Role staffRole = new Role();
        staffRole.setId(3L);
        staffRole.setName(RoleType.STAFF);
        staffRole.setUsers(new HashSet<>());

        staffUser.getRoles().add(staffRole);
        staffRole.getUsers().add(staffUser);

        when(userRepository.findByEmail("staff@example.com")).thenReturn(Optional.of(staffUser));

        UserDetails userDetails = userDetailsService.loadUserByUsername("staff@example.com");

        assertNotNull(userDetails);

        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        assertEquals(1, authorities.size());

        boolean hasStaffRole = authorities.stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_STAFF"));

        assertTrue(hasStaffRole);

        verify(userRepository).findByEmail("staff@example.com");
    }

    @Test
    void loadUserByUsername_ShouldConvertRolesToAuthorities() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        UserDetails userDetails = userDetailsService.loadUserByUsername("test@example.com");

        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();

        Set<SimpleGrantedAuthority> actualAuthorities = authorities.stream()
                .map(auth -> new SimpleGrantedAuthority(auth.getAuthority()))
                .collect(Collectors.toSet());

        Set<SimpleGrantedAuthority> expectedAuthorities = Set.of(
                new SimpleGrantedAuthority("ROLE_STUDENT"),
                new SimpleGrantedAuthority("ROLE_TUTOR")
        );

        assertEquals(expectedAuthorities, actualAuthorities);
    }
}
