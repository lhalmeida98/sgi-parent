package ec.sri.einvoice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SriElectronicInvoicingCoreApplication {
  public static void main(String[] args) {
    SpringApplication.run(SriElectronicInvoicingCoreApplication.class, args);
  }
}
