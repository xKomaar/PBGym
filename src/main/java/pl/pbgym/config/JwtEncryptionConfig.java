package pl.pbgym.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.pbgym.util.encryption.EncryptionUtil;

@Configuration
public class JwtEncryptionConfig {
    @Value("${env.JWT_ENCRYPTION_KEY}")
    private String base64SecretKey;

    @Bean(name = "jwtEncryptionUtil")
    public EncryptionUtil jwtEncryptionUtil() {
        return new EncryptionUtil(base64SecretKey);
    }
}
