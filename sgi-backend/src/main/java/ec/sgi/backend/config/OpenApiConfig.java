package ec.sgi.backend.config;

import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT"
)
public class OpenApiConfig {
  @Bean
  public OpenAPI sgiOpenApi() {
    return new OpenAPI()
        .info(new Info()
            .title("SGI Backend API")
            .version("v1")
            .description("Documentacion de endpoints SGI"));
  }
}
