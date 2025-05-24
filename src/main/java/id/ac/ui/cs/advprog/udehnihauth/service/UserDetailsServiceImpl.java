package id.ac.ui.cs.advprog.udehnihauth.service;

import id.ac.ui.cs.advprog.udehnihauth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            Long userId = Long.parseLong(username);
            log.debug("Loading user by ID: {}", userId);

            return userRepository.findByIdWithRoles(userId)
                    .map(this::createUserDetails)
                    .orElseThrow(() -> {
                        log.warn("User not found with id: {}", userId);
                        return new UsernameNotFoundException("User not found with id: " + userId);
                    });
        } catch (NumberFormatException e) {
            log.debug("Loading user by email: {}", username);

            return userRepository.findByEmailForAuthentication(username)
                    .map(this::createUserDetails)
                    .orElseThrow(() -> {
                        log.warn("User not found with email: {}", username);
                        return new UsernameNotFoundException("User not found with email: " + username);
                    });
        }
    }

    private UserDetails createUserDetails(id.ac.ui.cs.advprog.udehnihauth.model.User user) {
        return User.withUsername(user.getEmail())
                .password(user.getPassword())
                .authorities(user.getRoles().stream()
                        .map(role -> "ROLE_" + role.getName().name())
                        .toArray(String[]::new))
                .build();
    }
}