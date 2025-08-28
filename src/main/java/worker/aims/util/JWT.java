package worker.aims.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import worker.aims.entity.PlatformAdmin;
import worker.aims.entity.TempToken;
import worker.aims.entity.UserSession;
import worker.aims.entity.User;
import javax.sql.DataSource;
import java.sql.*;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import java.util.Objects;


public class JWT {
    
    private final SecretKey secretKey;
    private final Duration defaultAccessTtl;
    private final Duration defaultRefreshTtl;

    private final DataSource dataSource;
    private final String SESSION_TABLE = "user_session";
    private final String TEMP_TOKEN_TABLE = "temp_token";

    public JWT(String base64Secret,
               Duration defaultAccessTtl,
               Duration defaultRefreshTtl,
               DataSource dataSource) {
        byte[] keyBytes = Decoders.BASE64.decode(base64Secret); // 传入 Base64 编码后的 256bit 秘钥
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        this.defaultAccessTtl = defaultAccessTtl != null ? defaultAccessTtl : Duration.ofHours(24);
        this.defaultRefreshTtl = defaultRefreshTtl != null ? defaultRefreshTtl : Duration.ofDays(7);
        this.dataSource = dataSource;
    }

    public String generateToken(Map<String, Object> payload, Duration ttl) {
        Instant now = Instant.now();
        Instant exp = now.plus(ttl != null ? ttl : defaultAccessTtl);
        return Jwts.builder()
                .setClaims(payload)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(exp))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims verifyToken(String token) {
        try {
            Jws<Claims> jws = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);
            return jws.getBody();
        } catch (Exception e) {
            throw new RuntimeException("Invalid token", e);
        }
    }

    // 与工厂用户相关的认证令牌
    public Tokens generateAuthTokens(User user) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("userId", user.getUid());
        payload.put("factoryId", user.getFactoryId());
        payload.put("username", user.getUsername());
        payload.put("email", user.getEmail());
        payload.put("role", user.getRole());
        payload.put("department", user.getDepartment());
        payload.put("permissions", user.getPermissions() != null ? user.getPermissions() : new String[]{});
        payload.put("type", "factory_user");
        String token = generateToken(payload, defaultAccessTtl);
        Map<String, Object> refreshPayload = new HashMap<>();
        refreshPayload.put("userId", user.getUid());
        refreshPayload.put("factoryId", user.getFactoryId());
        refreshPayload.put("type", "factory_user");
        String refreshToken = generateToken(refreshPayload, defaultRefreshTtl);
        LocalDateTime expiresAt = LocalDateTime.now().plus(defaultAccessTtl);
        deleteSessionsByUserAndFactory(user.getUid(), user.getFactoryId());
        UserSession session = new UserSession();
        session.setUserId(user.getUid());
        session.setFactoryId(user.getFactoryId());
        session.setToken(token);
        session.setRefreshToken(refreshToken);
        session.setExpiresAt(expiresAt);
        session.setIsRevoked(false);
        createSession(session);
        return new Tokens(token, refreshToken);
    }

    // 平台管理员认证令牌
    public Tokens generatePlatformAuthTokens(PlatformAdmin admin) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("adminId", admin.getPid());
        payload.put("username", admin.getUsername());
        payload.put("email", admin.getEmail());
        payload.put("type", "platform_admin");
        String token = generateToken(payload, defaultAccessTtl);
        Map<String, Object> refreshPayload = new HashMap<>();
        refreshPayload.put("adminId", admin.getPid());
        refreshPayload.put("type", "platform_admin");
        String refreshToken = generateToken(refreshPayload, defaultRefreshTtl);

        return new Tokens(token, refreshToken);
    }

    // 撤销用户所有令牌
    public void revokeUserTokens(Integer userId, String factoryId) {
        revokeSessionsByUserAndFactory(userId, factoryId);
    }

    // 校验会话
    public UserSession validateSession(String token) {
        try {
            UserSession s = findSessionByToken(token);
            if (s == null) return null;
            if (s.getIsRevoked()) return null;
            if (LocalDateTime.now().isAfter(s.getExpiresAt())) return null;
            return s;
        } catch (Exception e) {
            return null;
        }
    }

    // 刷新令牌
    public Tokens refreshAuthToken(String refreshToken) {
        Claims payload;
        try {
            payload = verifyToken(refreshToken);
        } catch (Exception e) {
            throw new RuntimeException("Invalid refresh token");
        }
        String type = String.valueOf(payload.get("type"));
        if (Objects.equals(type, "factory_user")) {
            UserSession userSession = findSessionByRefreshToken(refreshToken);
            if (userSession == null || userSession.getIsRevoked())
                throw new RuntimeException("Invalid refresh token");
            Tokens newTokens = generateAuthTokensFromSession(userSession);
            revokeSessionById(userSession.getId());
            return newTokens;
        } else if (Objects.equals(type, "platform_admin")) {
            // 平台管理员刷新，通常需要据 adminId 重新生成
            Integer adminId = ((Number) payload.get("adminId")).intValue();
            PlatformAdmin admin = new PlatformAdmin();
            admin.setPid(adminId);
            admin.setUsername((String) payload.get("username"));
            admin.setEmail((String) payload.get("email"));
            return generatePlatformAuthTokens(admin);
        }
        throw new RuntimeException("Invalid token type");
    }

    // 生成临时令牌（手机验证等）
    public String generateTempToken(String type,
                                    String factoryId,
                                    String phoneNumber,
                                    Map<String, Object> data,
                                    int expiresInMinutes) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", type);
        claims.put("factoryId", factoryId);
        claims.put("phoneNumber", phoneNumber);
        claims.put("data", data);
        claims.put("timestamp", System.currentTimeMillis());
        String token = generateToken(claims, Duration.ofMinutes(expiresInMinutes));
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(expiresInMinutes);
        TempToken tempToken = new TempToken();
        tempToken.setToken(token);
        tempToken.setType(type);
        tempToken.setFactoryId(factoryId);
        tempToken.setPhoneNumber(phoneNumber);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String json = objectMapper.writeValueAsString(data);
            tempToken.setData(json);
        } catch (Exception e) {
            throw new RuntimeException("JSON parse error", e);
        }
        tempToken.setExpiresAt(expiresAt);
        tempToken.setIsUsed(false);
        createTempTokenRow(tempToken);
        return token;
    }

    // 校验并使用临时令牌
    public TempTokenData verifyAndUseTempToken(String token, String expectedType) {
        try {
            Claims payload = verifyToken(token);
            String actualType = String.valueOf(payload.get("type"));
            if (!Objects.equals(actualType, expectedType)) {
                throw new RuntimeException("Invalid token type");
            }

            TempToken temp = findTempTokenByToken(token);
            if (temp == null || temp.getIsUsed() || LocalDateTime.now().isAfter(temp.getExpiresAt())) {
                throw new RuntimeException("Invalid or expired token");
            }
            markTempTokenUsed(temp.getId());
            ObjectMapper objectMapper = new ObjectMapper();
            String data = temp.getData();
            Map<String, Object> data_map = objectMapper.readValue(data, new TypeReference<Map<String,Object>>(){});
            return new TempTokenData(temp.getFactoryId(), temp.getPhoneNumber(), data_map);
        } catch (Exception e) {
            throw new RuntimeException("Invalid temp token");
        }
    }


    public static class Tokens {
        public final String token;
        public final String refreshToken;
        public Tokens(String token, String refreshToken) {
            this.token = token;
            this.refreshToken = refreshToken;
        }
    }


    public static class TempTokenData {
        public final String factoryId;
        public final String phoneNumber;
        public final Object data;
        public TempTokenData(String factoryId, String phoneNumber, Object data) {
            this.factoryId = factoryId;
            this.phoneNumber = phoneNumber;
            this.data = data;
        }
    }

    private void deleteSessionsByUserAndFactory(long userId, String factoryId) {
        String sql = "DELETE FROM " + SESSION_TABLE + " WHERE user_id = ? AND factory_id = ?";
        try (Connection c = dataSource.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setString(2, factoryId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void createSession(UserSession s) {
        String sql = "INSERT INTO " + SESSION_TABLE + " (user_id, factory_id, token, refresh_token, expires_at, is_revoked) VALUES (?,?,?,?,?,?)";
        try (Connection c = dataSource.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, s.getUserId());
            ps.setString(2, s.getFactoryId());
            ps.setString(3, s.getToken());
            ps.setString(4, s.getRefreshToken());
            ps.setTimestamp(5, Timestamp.valueOf(s.getExpiresAt()));
            ps.setBoolean(6, s.getIsRevoked());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private UserSession findSessionByToken(String token) {
        String sql = "SELECT id, user_id, factory_id, token, refresh_token, expires_at, is_revoked " +
                "FROM `" + SESSION_TABLE + "` WHERE token = ? LIMIT 1";
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, token);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String id = rs.getString("id");
                    Integer userId = (Integer) rs.getObject("user_id"); // 避免 null 变 0
                    String factoryId = rs.getString("factory_id");
                    String refreshToken = rs.getString("refresh_token");
                    Timestamp ts = rs.getTimestamp("expires_at");
                    LocalDateTime expiresAt = (ts != null) ? ts.toLocalDateTime() : null;
                    Boolean isRevoked = (Boolean) rs.getObject("is_revoked"); // 避免 null 变 false
                    UserSession session = new UserSession();
                    session.setId(id);
                    session.setUserId(userId);
                    session.setFactoryId(factoryId);
                    session.setToken(token);
                    session.setRefreshToken(refreshToken);
                    session.setExpiresAt(expiresAt);
                    session.setIsRevoked(isRevoked);
                    return session;
                }
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private UserSession findSessionByRefreshToken(String refreshToken) {
        String sql = "SELECT id, user_id, factory_id, token, refresh_token, expires_at, is_revoked " +
                "FROM " + SESSION_TABLE + " WHERE refresh_token = ? LIMIT 1";
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, refreshToken);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String id = rs.getString("id");
                    Integer userId = (Integer) rs.getObject("user_id"); // 避免 null → 0
                    String factoryId = rs.getString("factory_id");
                    String token = rs.getString("token");
                    Timestamp ts = rs.getTimestamp("expires_at");
                    LocalDateTime expiresAt = (ts != null) ? ts.toLocalDateTime() : null;
                    Boolean isRevoked = (Boolean) rs.getObject("is_revoked"); // 避免 null → false
                    UserSession session = new UserSession();
                    session.setId(id);
                    session.setUserId(userId);
                    session.setFactoryId(factoryId);
                    session.setToken(token);
                    session.setRefreshToken(refreshToken);
                    session.setExpiresAt(expiresAt);
                    session.setIsRevoked(isRevoked);
                    return session;
                }
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void revokeSessionById(String id) {
        String sql = "UPDATE " + SESSION_TABLE + " SET is_revoked = ture WHERE id = ?";
        try (Connection c = dataSource.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void revokeSessionsByUserAndFactory(Integer userId, String factoryId) {
        String sql = "UPDATE " + SESSION_TABLE + " SET is_revoked = true WHERE user_id = ? AND factory_id = ?";
        try (Connection c = dataSource.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, factoryId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void createTempTokenRow(TempToken t) {
        String sql = "INSERT INTO " + TEMP_TOKEN_TABLE + " (token, type, factory_id, phone_number, data, expires_at, is_used) VALUES (?,?,?,?,?, ?, false)";
        try (Connection c = dataSource.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, t.getToken());
            ps.setString(2, t.getType());
            ps.setString(3, t.getFactoryId());
            ps.setString(4, t.getPhoneNumber());
            ps.setString(5, t.getData() != null ? t.getData() : null);
            ps.setTimestamp(6, Timestamp.valueOf(t.getExpiresAt()));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private TempToken findTempTokenByToken(String token) {
        String sql = "SELECT id, token, type, factory_id, phone_number, data, expires_at, is_used " +
                "FROM " + TEMP_TOKEN_TABLE + " WHERE token = ? LIMIT 1";
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, token);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Integer id = (Integer) rs.getObject("id"); // 避免 null → 0
                    String type = rs.getString("type");
                    String factoryId = rs.getString("factory_id");
                    String phoneNumber = rs.getString("phone_number");
                    String data = rs.getString("data");
                    Timestamp ts = rs.getTimestamp("expires_at");
                    LocalDateTime expiresAt = (ts != null) ? ts.toLocalDateTime() : null;
                    Boolean isUsed = (Boolean) rs.getObject("is_used"); // 避免 null → false
                    TempToken tempToken = new TempToken();
                    tempToken.setId(id);
                    tempToken.setToken(token);
                    tempToken.setType(type);
                    tempToken.setFactoryId(factoryId);
                    tempToken.setPhoneNumber(phoneNumber);
                    tempToken.setData(data);
                    tempToken.setExpiresAt(expiresAt);
                    tempToken.setIsUsed(isUsed);
                    return tempToken;
                }
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    private void markTempTokenUsed(Integer id) {
        String sql = "UPDATE " + TEMP_TOKEN_TABLE + " SET is_used = true WHERE id = ?";
        try (Connection c = dataSource.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Tokens generateAuthTokensFromSession(UserSession s) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("userId", s.getUserId());
        payload.put("factoryId", s.getFactoryId());
        payload.put("type", "factory_user");
        String token = generateToken(payload, defaultAccessTtl);
        Map<String, Object> refreshPayload = new HashMap<>();
        refreshPayload.put("userId", s.getUserId());
        refreshPayload.put("factoryId", s.getFactoryId());
        refreshPayload.put("type", "factory_user");
        String refreshToken = generateToken(refreshPayload, defaultRefreshTtl);
        // 替换旧 session
        revokeSessionById(s.getId());
        LocalDateTime expiresAt = LocalDateTime.now().plus(defaultAccessTtl);
        UserSession session = new UserSession();
        session.setUserId(s.getUserId());
        session.setFactoryId(s.getFactoryId());
        session.setToken(token);
        session.setRefreshToken(refreshToken);
        session.setExpiresAt(expiresAt);
        session.setIsRevoked(false);
        createSession(session);
        return new Tokens(token, refreshToken);
    }
}




