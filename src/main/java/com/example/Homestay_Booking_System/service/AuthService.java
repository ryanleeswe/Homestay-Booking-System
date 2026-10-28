package com.example.Homestay_Booking_System.service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.Homestay_Booking_System.domain.RefreshToken;
import com.example.Homestay_Booking_System.domain.User;
import com.example.Homestay_Booking_System.repository.RefreshTokenRepository;
import com.example.Homestay_Booking_System.repository.UserRepository;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtEncoder jwtEncoder;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final long accessTokenTtlSeconds;
    private final long refreshTokenTtlSeconds;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthService(AuthenticationManager authenticationManager, JwtEncoder jwtEncoder,
            UserRepository userRepository, RefreshTokenRepository refreshTokenRepository,
            @Value("${security.jwt.access-token-ttl-seconds}") long accessTokenTtlSeconds,
            @Value("${security.jwt.refresh-token-ttl-seconds}") long refreshTokenTtlSeconds) {
        this.authenticationManager = authenticationManager;
        this.jwtEncoder = jwtEncoder;
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.accessTokenTtlSeconds = accessTokenTtlSeconds;
        this.refreshTokenTtlSeconds = refreshTokenTtlSeconds;
    }

    @Transactional
    public TokenPair login(String email, String password) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
        User user = findUser(email);
        return createTokenPair(user);
    }

    @Transactional
    public TokenPair refresh(String rawRefreshToken) {
        RefreshToken storedToken = refreshTokenRepository.findByTokenHashForUpdate(hashToken(rawRefreshToken))
                .filter(token -> !token.isRevoked() && token.getExpiresAt().isAfter(Instant.now()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token"));

        User user = findUser(storedToken.getUserEmail());
        storedToken.setRevoked(true);
        return createTokenPair(user);
    }

    @Transactional
    public void logout(String userEmail) {
        refreshTokenRepository.revokeAllByUserEmail(userEmail);
    }

    private TokenPair createTokenPair(User user) {
        Instant now = Instant.now();
        Instant accessExpiry = now.plus(accessTokenTtlSeconds, ChronoUnit.SECONDS);
        String accessToken = jwtEncoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(),
                JwtClaimsSet.builder()
                        .issuer("homestay-booking-system")
                        .issuedAt(now)
                        .expiresAt(accessExpiry)
                        .subject(user.getEmail())
                        .claim("roles", List.of("ROLE_" + user.getRole().name()))
                        .claim("token_type", "access")
                        .build())).getTokenValue();

        byte[] tokenBytes = new byte[64];
        secureRandom.nextBytes(tokenBytes);
        String rawRefreshToken = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setTokenHash(hashToken(rawRefreshToken));
        refreshToken.setUserEmail(user.getEmail());
        refreshToken.setExpiresAt(now.plus(refreshTokenTtlSeconds, ChronoUnit.SECONDS));
        refreshToken.setRevoked(false);
        refreshTokenRepository.save(refreshToken);

        return new TokenPair(accessToken, rawRefreshToken, accessTokenTtlSeconds,
                refreshTokenTtlSeconds);
    }

    private User findUser(String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Account no longer exists");
        }
        return user;
    }

    private String hashToken(String rawToken) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(rawToken.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    public record TokenPair(String tokenType, String accessToken, long accessTokenExpiresIn,
            String refreshToken, long refreshTokenExpiresIn) {
        public TokenPair(String accessToken, String refreshToken, long accessTokenExpiresIn,
                long refreshTokenExpiresIn) {
            this("Bearer", accessToken, accessTokenExpiresIn, refreshToken, refreshTokenExpiresIn);
        }
    }
}
