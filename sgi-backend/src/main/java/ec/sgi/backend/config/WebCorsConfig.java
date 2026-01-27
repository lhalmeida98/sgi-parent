package ec.sgi.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class WebCorsConfig implements WebMvcConfigurer {
  private final List<String> allowedOrigins;

  public WebCorsConfig(
      @Value("${app.cors.allowed-origins:http://localhost:*,http://127.0.0.1:*,http://10.0.2.2:*}")
      String allowedOrigins
  ) {
    this.allowedOrigins = parseOrigins(allowedOrigins);
  }

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/**")
        .allowedOriginPatterns(allowedOrigins.toArray(new String[0]))
        .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
        .allowedHeaders("*");
  }

  private static List<String> parseOrigins(String origins) {
    return Arrays.stream(origins.split(","))
        .map(String::trim)
        .filter(value -> !value.isEmpty())
        .collect(Collectors.toList());
  }
}
