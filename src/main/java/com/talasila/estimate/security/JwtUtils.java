package com.talasila.estimate.security;

import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

import com.talasila.estimate.service.UserDetailsImpl;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

@Component
public class JwtUtils {
  private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);
  private static final String ACCESS_TOKEN_TYPE = "access";
  private static final String REFRESH_TOKEN_TYPE = "refresh";
  private static final String ESTIMATE_SHARE_TOKEN_TYPE = "estimate_share";

  @Value("${app.jwt.secret}")
  private String jwtSecret;

  @Value("${app.jwt.expirationMs}")
  private long jwtExpirationMs;

  @Value("${app.jwt.cookieName}")
  private String jwtCookie;

  @Value("${app.jwt.jwtRefreshExpirationMs}")
  private long jwtRefreshExpirationMs;

  @Value("${app.jwt.jwtRefreshCookieName}")
  private String jwtRefreshCookie;

  @Value("${app.jwt.pdfShareExpirationMs}")
  private long pdfShareExpirationMs;

  public String getJwtFromCookies(HttpServletRequest request) {
    return getCookieValue(request, jwtCookie);
  }

  public String getJwtRefreshFromCookies(HttpServletRequest request) {
    return getCookieValue(request, jwtRefreshCookie);
  }

  public ResponseCookie generateJwtCookie(UserDetailsImpl userPrincipal) {
    String jwt = generateToken(userPrincipal, ACCESS_TOKEN_TYPE, jwtExpirationMs);
    return buildCookie(jwtCookie, jwt, jwtExpirationMs / 1000L);
  }

  public ResponseCookie generateRefreshJwtCookie(UserDetailsImpl userPrincipal) {
    String refreshToken = generateToken(userPrincipal, REFRESH_TOKEN_TYPE, jwtRefreshExpirationMs);
    return buildCookie(jwtRefreshCookie, refreshToken, jwtRefreshExpirationMs / 1000L);
  }

  public ResponseCookie getCleanJwtCookie() {
    return buildCookie(jwtCookie, "", 0);
  }

  public ResponseCookie getCleanJwtRefreshCookie() {
    return buildCookie(jwtRefreshCookie, "", 0);
  }

  public String getUserNameFromJwtToken(String token) {
    return Jwts.parserBuilder().setSigningKey(key()).build()
        .parseClaimsJws(token).getBody().getSubject();
  }

  public boolean validateJwtToken(String authToken) {
    return validateToken(authToken, ACCESS_TOKEN_TYPE);
  }

  public boolean validateJwtRefreshToken(String authToken) {
    return validateToken(authToken, REFRESH_TOKEN_TYPE);
  }

  public String generateEstimateShareToken(Long estimateId) {
    Date now = new Date();

    return Jwts.builder()
        .claim("estimateId", estimateId)
        .claim("tokenType", ESTIMATE_SHARE_TOKEN_TYPE)
        .setIssuedAt(now)
        .setExpiration(new Date(now.getTime() + pdfShareExpirationMs))
        .signWith(key(), SignatureAlgorithm.HS256)
        .compact();
  }

  public boolean validateEstimateShareToken(String token, Long estimateId) {
    try {
      Long tokenEstimateId = Jwts.parserBuilder()
          .setSigningKey(key())
          .build()
          .parseClaimsJws(token)
          .getBody()
          .get("estimateId", Long.class);

      String tokenType = Jwts.parserBuilder()
          .setSigningKey(key())
          .build()
          .parseClaimsJws(token)
          .getBody()
          .get("tokenType", String.class);

      return ESTIMATE_SHARE_TOKEN_TYPE.equals(tokenType) && estimateId.equals(tokenEstimateId);
    } catch (MalformedJwtException e) {
      logger.error("Invalid estimate share token: {}", e.getMessage());
    } catch (ExpiredJwtException e) {
      logger.error("Estimate share token is expired: {}", e.getMessage());
    } catch (UnsupportedJwtException e) {
      logger.error("Estimate share token is unsupported: {}", e.getMessage());
    } catch (IllegalArgumentException e) {
      logger.error("Estimate share token claims are empty: {}", e.getMessage());
    }

    return false;
  }

  private String getCookieValue(HttpServletRequest request, String cookieName) {
    Cookie cookie = WebUtils.getCookie(request, cookieName);
    return cookie != null ? cookie.getValue() : null;
  }

  private ResponseCookie buildCookie(String cookieName, String value, long maxAgeSeconds) {
    return ResponseCookie.from(cookieName, value)
        .path("/api")
        .maxAge(maxAgeSeconds)
        .httpOnly(true)
        .sameSite("None")
        .secure(true)
        .build();
  }

  private Key key() {
    return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
  }

  private boolean validateToken(String authToken, String expectedTokenType) {
    try {
      String tokenType = Jwts.parserBuilder()
          .setSigningKey(key())
          .build()
          .parseClaimsJws(authToken)
          .getBody()
          .get("tokenType", String.class);

      if (ACCESS_TOKEN_TYPE.equals(expectedTokenType)) {
        return tokenType == null || ACCESS_TOKEN_TYPE.equals(tokenType);
      }

      return expectedTokenType.equals(tokenType);
    } catch (MalformedJwtException e) {
      logger.error("Invalid JWT token: {}", e.getMessage());
    } catch (ExpiredJwtException e) {
      logger.error("JWT token is expired: {}", e.getMessage());
    } catch (UnsupportedJwtException e) {
      logger.error("JWT token is unsupported: {}", e.getMessage());
    } catch (IllegalArgumentException e) {
      logger.error("JWT claims string is empty: {}", e.getMessage());
    }

    return false;
  }

  private String generateToken(UserDetailsImpl userPrincipal, String tokenType, long expirationMs) {
    List<String> roles = userPrincipal.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .collect(Collectors.toList());

    Date now = new Date();

    return Jwts.builder()
        .setSubject(userPrincipal.getUsername())
        .claim("roles", roles)
        .claim("tokenType", tokenType)
        .setIssuedAt(now)
        .setExpiration(new Date(now.getTime() + expirationMs))
        .signWith(key(), SignatureAlgorithm.HS256)
        .compact();
  }
}
