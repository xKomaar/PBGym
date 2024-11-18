package pl.pbgym.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.pbgym.util.encryption.EncryptionUtil;

@Configuration
public class CreditCardEncryptionConfig {

    @Value("${env.ENCRYPTION_SECRET_KEY}")
    private String base64SecretKey;

    @Bean(name = "creditCardEncryptionUtil")
    public EncryptionUtil creditCardEncryptionUtil() {
        return new EncryptionUtil(base64SecretKey);
    }
}
