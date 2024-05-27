package ru.javaops.startup.app.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Admin API",
                version = "1.0",
                description = """
                        <b><a href='https://javaops.ru/view/startup'>Startup course</a></b> application<br>
                        <p><b>
                           <a href='/'>Home</a> | <a href='/logout'>Logout</a>
                        </b></p>
                        """,
                contact = @Contact(url = "https://javaops.ru/#contacts", name = "Grigory Kislin", email = "admin@javaops.ru")
        )
)
public class OpenApiConfig {

    @Bean
    public GroupedOpenApi api() {
        return GroupedOpenApi.builder()
                .group("REST API")
                .pathsToMatch(SecurityConfig.API_PATH + "/**")
                .build();
    }
}
