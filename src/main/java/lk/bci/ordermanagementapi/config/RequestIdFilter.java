package lk.bci.ordermanagementapi.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestIdFilter extends OncePerRequestFilter {

    public static final String REQUEST_ID_HEADER = "X-Request-Id";
    public static final String MDC_REQUEST_ID_KEY = "requestId";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String requestId = request.getHeader(REQUEST_ID_HEADER);
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString();
        } else {
            requestId = requestId.trim();
        }

        MDC.put(REQUEST_ID_HEADER, requestId);
        MDC.put(MDC_REQUEST_ID_KEY, requestId);
        response.setHeader(REQUEST_ID_HEADER, requestId);

        long startTime = System.currentTimeMillis();
        String uri = request.getQueryString() != null
                ? request.getRequestURI() + "?" + request.getQueryString()
                : request.getRequestURI();

        log.info("Incoming request: method={}, uri={}, requestId={}", request.getMethod(), uri, requestId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            long durationMs = System.currentTimeMillis() - startTime;
            log.info(
                    "Completed request: method={}, uri={}, status={}, durationMs={}",
                    request.getMethod(),
                    uri,
                    response.getStatus(),
                    durationMs
            );
            MDC.remove(REQUEST_ID_HEADER);
            MDC.remove(MDC_REQUEST_ID_KEY);
        }
    }
}
