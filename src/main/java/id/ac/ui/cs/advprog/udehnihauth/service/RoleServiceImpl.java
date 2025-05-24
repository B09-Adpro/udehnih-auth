package id.ac.ui.cs.advprog.udehnihauth.service;

import id.ac.ui.cs.advprog.udehnihauth.exception.UserNotFoundException;
import id.ac.ui.cs.advprog.udehnihauth.model.Role;
import id.ac.ui.cs.advprog.udehnihauth.model.RoleType;
import id.ac.ui.cs.advprog.udehnihauth.model.User;
import id.ac.ui.cs.advprog.udehnihauth.repository.RoleRepository;
import id.ac.ui.cs.advprog.udehnihauth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public boolean addRoleToUser(Long userId, RoleType roleType, Long staffId) {
        User staff = getUserById(staffId);
        if (!staff.hasRole(RoleType.STAFF)) {
            log.warn("User {} attempted to add role but is not staff", staffId);
            throw new SecurityException("Only staff can add roles to users");
        }

        try {
            User user = getUserById(userId);
            Role role = getRoleByType(roleType);

            if (user.hasRole(roleType)) {
                log.info("User {} already has role {}", userId, roleType);
                return false;
            }

            user.addRole(role);
            userRepository.save(user);
            log.info("Role {} added to user {} by staff {}", roleType, userId, staffId);
            return true;
        } catch (Exception e) {
            log.error("Error adding role {} to user {}: {}", roleType, userId, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean userHasRole(Long userId, RoleType roleType) {
        try {
            User user = getUserById(userId);
            return user.hasRole(roleType);
        } catch (Exception e) {
            log.error("Error checking role {} for user {}: {}", roleType, userId, e.getMessage());
            return false;
        }
    }

    @Override
    public Set<RoleType> getUserRoles(Long userId) {
        try {
            User user = getUserById(userId);
            return user.getRoles().stream()
                    .map(Role::getName)
                    .collect(Collectors.toSet());
        } catch (Exception e) {
            log.error("Error getting roles for user {}: {}", userId, e.getMessage());
            return Set.of();
        }
    }

    @Override
    @Transactional
    public boolean removeRoleFromUser(Long userId, RoleType roleType) {
        try {
            User user = getUserById(userId);
            Role role = getRoleByType(roleType);

            if (!user.hasRole(roleType)) {
                log.info("User {} doesn't have role {}", userId, roleType);
                return false;
            }

            user.removeRole(role);
            userRepository.save(user);
            log.info("Role {} removed from user {}", roleType, userId);
            return true;
        } catch (Exception e) {
            log.error("Error removing role {} from user {}: {}", roleType, userId, e.getMessage());
            return false;
        }
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
    }

    private Role getRoleByType(RoleType roleType) {
        return roleRepository.findByName(roleType)
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleType));
    }
}