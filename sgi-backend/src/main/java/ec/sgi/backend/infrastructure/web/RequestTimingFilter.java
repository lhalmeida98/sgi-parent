package ec.sgi.backend.infrastructure.web;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestTimingFilter extends OncePerRequestFilter {
  private static final Logger log = LoggerFactory.getLogger(RequestTimingFilter.class);

  @Value("${app.logging.request-timing.enabled:true}")
  private boolean enabled;

  @Value("${app.logging.request-timing.min-ms:0}")
  private long minMs;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain
  ) throws ServletException, IOException {
    if (!enabled) {
      filterChain.doFilter(request, response);
      return;
    }

    long startNs = System.nanoTime();
    try {
      filterChain.doFilter(request, response);
    } finally {
      long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);
      if (durationMs >= minMs) {
        String uri = request.getRequestURI();
        String query = request.getQueryString();
        if (query != null && !query.isBlank()) {
          uri = uri + "?" + query;
        }
        log.info("request {} {} -> {} ({} ms)", request.getMethod(), uri, response.getStatus(), durationMs);
      }
    }
  }
}
