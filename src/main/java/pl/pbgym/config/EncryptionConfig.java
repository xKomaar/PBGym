package pl.pbgym.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.pbgym.util.encryption.EncryptionUtil;

@Configuration
public class EncryptionConfig {

    @Value("${env.ENCRYPTION_SECRET_KEY}")
    private String secretKey;

    @Bean
    public EncryptionUtil encryptionUtil() {
        return new EncryptionUtil(secretKey);
    }
}
