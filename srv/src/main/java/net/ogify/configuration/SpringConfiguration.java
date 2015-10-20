package net.ogify.configuration;

import net.ogify.engine.secure.AuthController;
import net.ogify.engine.secure.BetaAuthController;
import net.ogify.engine.secure.LocalAuthController;
import net.ogify.engine.secure.ProductionAuthController;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

/**
 * Spring Configuration file
 */
@Configuration
@ComponentScan(basePackages = {"net.ogify.engine", "net.ogify.rest.resources", "net.ogify.database"})
@PropertySource("classpath:ogify.properties")
public class SpringConfiguration {
    @Value("${auth.type:withKey}")
    String authType;

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyConfigInDev() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean
    public AuthController authController() {
        switch (authType) {
            case "production":
                return new ProductionAuthController();
            case "local":
                return new LocalAuthController();
            default:
                return new BetaAuthController();
        }
    }
}
