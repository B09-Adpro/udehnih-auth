package id.ac.ui.cs.advprog.udehnihauth.config;

import id.ac.ui.cs.advprog.udehnihauth.service.JwtService;
import id.ac.ui.cs.advprog.udehnihauth.service.TokenBlacklistService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.OngoingStubbing;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private final String validToken = "valid.jwt.token";
    private final String blacklistedToken = "blacklisted.jwt.token";
    private final String userEmail = "test@example.com";

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_WithNoAuthHeader_ShouldContinueFilterChain() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtService, never()).extractUsername(anyString());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_WithNonBearerToken_ShouldContinueFilterChain() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Basic dXNlcjpwYXNzd29yZA==");

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtService, never()).extractUsername(anyString());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_WithBlacklistedToken_ShouldNotAuthenticate() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer " + blacklistedToken);
        when(tokenBlacklistService.isBlacklisted(blacklistedToken)).thenReturn(true);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtService, never()).extractUsername(anyString());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_WithValidToken_ShouldAuthenticate() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(tokenBlacklistService.isBlacklisted(validToken)).thenReturn(false);
        when(jwtService.extractUsername(validToken)).thenReturn(userEmail);
        when(userDetailsService.loadUserByUsername(userEmail)).thenReturn(userDetails);
        when(jwtService.isTokenValid(validToken, userDetails)).thenReturn(true);
        when(userDetails.getAuthorities()).thenReturn(Collections.emptyList());

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtService).extractUsername(validToken);
        verify(userDetailsService).loadUserByUsername(userEmail);
        verify(jwtService).isTokenValid(validToken, userDetails);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertTrue(SecurityContextHolder.getContext().getAuthentication() instanceof UsernamePasswordAuthenticationToken);
        assertEquals(userDetails, SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    }

    @Test
    void doFilterInternal_WithInvalidToken_ShouldNotAuthenticate() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(tokenBlacklistService.isBlacklisted(validToken)).thenReturn(false);
        when(jwtService.extractUsername(validToken)).thenReturn(userEmail);
        when(userDetailsService.loadUserByUsername(userEmail)).thenReturn(userDetails);
        when(jwtService.isTokenValid(validToken, userDetails)).thenReturn(false);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtService).extractUsername(validToken);
        verify(userDetailsService).loadUserByUsername(userEmail);
        verify(jwtService).isTokenValid(validToken, userDetails);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_WhenExceptionOccurs_ShouldContinueFilterChain() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(tokenBlacklistService.isBlacklisted(validToken)).thenReturn(false);
        when(jwtService.extractUsername(validToken)).thenThrow(new RuntimeException("Test exception"));

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtService).extractUsername(validToken);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_WithNullUsername_ShouldNotAuthenticate() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(tokenBlacklistService.isBlacklisted(validToken)).thenReturn(false);
        OngoingStubbing<String> stringOngoingStubbing = when(jwtService.extractUsername(validToken)).thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtService).extractUsername(validToken);
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_WithExistingAuthentication_ShouldNotModifyAuthentication() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("existing", null, Collections.emptyList()));

        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(tokenBlacklistService.isBlacklisted(validToken)).thenReturn(false);
        when(jwtService.extractUsername(validToken)).thenReturn(userEmail);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtService).extractUsername(validToken);
        verify(userDetailsService, never()).loadUserByUsername(anyString());

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("existing", SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    }
}