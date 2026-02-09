package ec.sgi.backend.application.port;

import java.util.List;

public interface EmailService {
  void enviarEmail(String to, String subject, String htmlBody, List<EmailAttachment> attachments);
}
