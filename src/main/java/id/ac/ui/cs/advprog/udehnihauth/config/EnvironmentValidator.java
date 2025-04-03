package id.ac.ui.cs.advprog.udehnihauth.config;

import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Profile("!test")
public class EnvironmentValidator implements ApplicationListener<ApplicationStartedEvent> {

    private final Environment environment;

    private final List<String> requiredVariables = Arrays.asList(
            "DB_HOST",
            "DB_PORT",
            "DB_NAME",
            "DB_USERNAME",
            "DB_PASSWORD",
            "JWT_SECRET_KEY"
    );

    public EnvironmentValidator(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        List<String> missingVariables = requiredVariables.stream()
                .filter(variable -> environment.getProperty(variable) == null)
                .collect(Collectors.toList());

        if (!missingVariables.isEmpty()) {
            throw new IllegalStateException(
                    "Application cannot start due to missing required environment variables: " +
                            String.join(", ", missingVariables) +
                            ". Please set these variables in your .env file or system environment."
            );
        }
    }
}