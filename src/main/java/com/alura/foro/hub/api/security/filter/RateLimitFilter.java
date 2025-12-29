package com.alura.foro.hub.api.security.filter;

import com.alura.foro.hub.api.security.filter.RateLimitProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RateLimitFilter extends OncePerRequestFilter {

    private static final class Window {
        long windowStartEpochSec;
        int count;
        Window(long start, int count) { this.windowStartEpochSec = start; this.count = count; }
    }

    private final Map<String, Window> counters = new ConcurrentHashMap<>();
    private final RateLimitProperties props;

    public RateLimitFilter(RateLimitProperties props) {
        this.props = props;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (!props.enabled()) return true;

        String path = request.getRequestURI();

        // no limitar swagger/api-docs
        if (path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs")) return true;

        // opcional: no limitar actuator (si lo usás)
        if (path.startsWith("/actuator")) return true;

        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        String method = request.getMethod();

        String ip = clientIp(request);
        Bucket bucket = classify(path, method);

        if (!allow(ip, bucket.name, bucket.maxRequestsPerWindow)) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write("""
                {"error":"Too Many Requests","message":"Demasiadas solicitudes. Intenta nuevamente en unos segundos."}
            """);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean allow(String ip, String bucketName, int limit) {
        long now = Instant.now().getEpochSecond();
        long windowSeconds = props.windowSeconds();
        long currentWindowStart = now - (now % windowSeconds);

        String key = ip + ":" + bucketName;

        Window w = counters.compute(key, (k, existing) -> {
            if (existing == null || existing.windowStartEpochSec != currentWindowStart) {
                return new Window(currentWindowStart, 1);
            }
            existing.count++;
            return existing;
        });

        return w.count <= limit;
    }

    private String clientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) return xff.split(",")[0].trim();
        return request.getRemoteAddr();
    }

    private enum Bucket {
        LOGIN("login"),
        WRITE("write"),
        READ("read");

        final String name;
        int maxRequestsPerWindow;
        Bucket(String name) { this.name = name; }
    }

    private Bucket classify(String path, String method) {
        Bucket b;
        if (path.equals("/auth/login")) b = Bucket.LOGIN;
        else if (method.equals("POST") || method.equals("PUT") || method.equals("PATCH") || method.equals("DELETE")) b = Bucket.WRITE;
        else b = Bucket.READ;

        b.maxRequestsPerWindow = switch (b) {
            case LOGIN -> props.loginMax();
            case WRITE -> props.writeMax();
            case READ  -> props.readMax();
        };

        return b;
    }
}
