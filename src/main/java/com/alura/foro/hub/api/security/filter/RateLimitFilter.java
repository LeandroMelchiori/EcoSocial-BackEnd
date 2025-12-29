package com.alura.foro.hub.api.security.filter;

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

        Window(long start, int count) {
            this.windowStartEpochSec = start;
            this.count = count;
        }
    }

    // key = ip + ":" + bucketName
    private final Map<String, Window> counters = new ConcurrentHashMap<>();

    // Ajustá límites según endpoint
    private static final int LOGIN_MAX_PER_MIN = 100;         // /auth/login
    private static final int WRITE_MAX_PER_MIN = 300;         // POST/PUT/PATCH/DELETE
    private static final int READ_MAX_PER_MIN  = 1200;        // GET
    private static final long WINDOW_SECONDS = 600;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        String method = request.getMethod();

        // No limites swagger/api-docs (si lo usás)
        if (path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs")) {
            filterChain.doFilter(request, response);
            return;
        }

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
        long currentWindowStart = now - (now % WINDOW_SECONDS);

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
        // Si en algún momento estás detrás de reverse proxy, esto ayuda
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private enum Bucket {
        LOGIN("login", LOGIN_MAX_PER_MIN),
        WRITE("write", WRITE_MAX_PER_MIN),
        READ("read", READ_MAX_PER_MIN);

        final String name;
        final int maxRequestsPerWindow;
        Bucket(String name, int max) { this.name = name; this.maxRequestsPerWindow = max; }
    }

    private Bucket classify(String path, String method) {
        if (path.equals("/auth/login")) return Bucket.LOGIN;

        return switch (method) {
            case "POST", "PUT", "PATCH", "DELETE" -> Bucket.WRITE;
            default -> Bucket.READ;
        };
    }
}
