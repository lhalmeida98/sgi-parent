package ec.sgi.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"ec.sgi.backend", "ec.sri.einvoice"})
@EntityScan(basePackages = {"ec.sgi.backend.infrastructure.persistence.entity", "ec.sri.einvoice.infrastructure.persistence.entity"})
@EnableJpaRepositories(basePackages = {"ec.sgi.backend.infrastructure.persistence.repository", "ec.sri.einvoice.infrastructure.persistence.repository"})
@EnableScheduling
public class SgiBackendApplication {
  public static void main(String[] args) {
    SpringApplication.run(SgiBackendApplication.class, args);
  }
}
