package id.ac.ui.cs.advprog.udehnihauth.service;

import id.ac.ui.cs.advprog.udehnihauth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(UserDetailsServiceImpl.class);
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            Long userId = Long.parseLong(username);
            logger.info("Loading user by ID: {}", userId);

            return userRepository.findById(userId)
                    .map(user -> {
                        logger.info("User found: {}", user.getEmail());
                        return User.withUsername(user.getEmail())
                                .password(user.getPassword())
                                .authorities(user.getRoles().stream()
                                        .map(role -> "ROLE_" + role.getName().name())
                                        .toArray(String[]::new))
                                .build();
                    })
                    .orElseThrow(() -> {
                        logger.warn("User not found with id: {}", username);
                        return new UsernameNotFoundException("User not found with id: " + username);
                    });
        } catch (NumberFormatException e) {
            logger.info("Loading user by email: {}", username);
            return userRepository.findByEmail(username)
                    .map(user -> User.withUsername(user.getEmail())
                            .password(user.getPassword())
                            .authorities(user.getRoles().stream()
                                    .map(role -> "ROLE_" + role.getName().name())
                                    .toArray(String[]::new))
                            .build())
                    .orElseThrow(() -> {
                        logger.warn("User not found with email: {}", username);
                        return new UsernameNotFoundException("User not found with email: " + username);
                    });
        }
    }
}
