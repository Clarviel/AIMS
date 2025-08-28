package worker.aims.config;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.io.Encoders;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import worker.aims.util.JWT;

import javax.crypto.SecretKey;
import javax.sql.DataSource;
import java.time.Duration;

@Configuration
public class JwtConfig {

    @Bean
    public JWT jwt(DataSource dataSource) {
        // 生成 256-bit 的密钥
        SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        String base64Secret = Encoders.BASE64.encode(key.getEncoded());
        return new JWT(
                base64Secret,
                Duration.ofHours(24),
                Duration.ofDays(7),
                dataSource
        );
    }
}
