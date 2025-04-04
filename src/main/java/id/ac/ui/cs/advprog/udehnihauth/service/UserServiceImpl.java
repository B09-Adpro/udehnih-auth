package id.ac.ui.cs.advprog.udehnihauth.service;

import id.ac.ui.cs.advprog.udehnihauth.dto.response.ServiceResponse;
import id.ac.ui.cs.advprog.udehnihauth.dto.response.UserResponse;
import id.ac.ui.cs.advprog.udehnihauth.model.Role;
import id.ac.ui.cs.advprog.udehnihauth.model.User;
import id.ac.ui.cs.advprog.udehnihauth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public User saveUser(User user) {
        return userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findById(UUID id) {
        return userRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findUsersByRole(Role role) {
        return userRepository.findByRolesContaining(role);
    }

    @Override
    @Transactional
    public ServiceResponse<User> addRoleToUser(UUID userId, Role role) {
        try {
            if (userId == null) {
                return ServiceResponse.error("User ID is required");
            }

            if (role == null) {
                return ServiceResponse.error("Role is required");
            }

            Optional<User> userOptional = userRepository.findById(userId);
            if (userOptional.isEmpty()) {
                return ServiceResponse.error("User not found with ID: " + userId);
            }

            User user = userOptional.get();

            if (user.hasRole(role)) {
                return ServiceResponse.error("User already has role: " + role);
            }

            user.addRole(role);
            User savedUser = userRepository.save(user);

            return ServiceResponse.success(savedUser, "Role " + role + " added to user successfully");
        } catch (Exception e) {
            return ServiceResponse.error("Failed to add role to user: " + e.getMessage());
        }
    }

    @Override
    public UserResponse convertToDto(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())
                .dateOfBirth(user.getDateOfBirth())
                .registrationDate(user.getRegistrationDate())
                .gender(user.getGender())
                .roles(user.getRoles())
                .build();
    }

    @Override
    @Transactional
    public ServiceResponse<Void> deleteUser(UUID id) {
        try {
            if (id == null) {
                return ServiceResponse.error("User ID is required");
            }

            if (!userRepository.existsById(id)) {
                return ServiceResponse.error("User not found with ID: " + id);
            }

            userRepository.deleteById(id);
            return ServiceResponse.success(null, "User deleted successfully");
        } catch (Exception e) {
            return ServiceResponse.error("Failed to delete user: " + e.getMessage());
        }
    }
}