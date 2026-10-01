package ar.edu.utn.dds.k3003.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Corre antes de cada request y carga en el MDC los datos que van en todas las lineas de log.
 *
 * <p>traceId: identifica toda la cadena de llamadas entre modulos. Si el request trae el header
 * {@link #TRACE_ID_HEADER} lo reusa (lo mando otro modulo); si no, lo genera porque este request
 * es el punto de entrada.
 *
 * <p>requestId: identifica solo este request en este modulo (no se propaga).
 *
 * <p>No se loguea cada request exitoso (GET/POST con 2xx/3xx) para no llenar Better Stack de ruido:
 * solo se loguean las respuestas con error (4xx como WARN, 5xx como ERROR). Los logs de negocio
 * (los que escribe la Fachada) igual salen con el traceId, porque el MDC se carga igual.
 */
@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    public static final String TRACE_ID_HEADER = "X-Trace-Id";

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    private final InstanceInfo instanceInfo;

    public RequestLoggingFilter(InstanceInfo instanceInfo) {
        this.instanceInfo = instanceInfo;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // /actuator: health checks de Render y scraping de Prometheus.
        // /ping: keep-alive del Cron cada 12 min. Loguearlos es puro ruido.
        String uri = request.getRequestURI();
        return uri.startsWith("/actuator") || uri.equals("/ping");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String traceId = request.getHeader(TRACE_ID_HEADER);
        if (traceId == null || traceId.isBlank()) {
            traceId = UUID.randomUUID().toString().substring(0, 8);
        }
        MDC.put("traceId", traceId);
        MDC.put("instanceId", instanceInfo.getInstanceId());
        MDC.put("requestId", UUID.randomUUID().toString().substring(0, 8));
        long start = System.currentTimeMillis();
        try {
            chain.doFilter(request, response);
        } finally {
            long took = System.currentTimeMillis() - start;
            int status = response.getStatus();
            if (status >= 500) {
                log.error("{} {} status={} took={}ms",
                        request.getMethod(), request.getRequestURI(), status, took);
            } else if (status >= 400) {
                log.warn("{} {} status={} took={}ms",
                        request.getMethod(), request.getRequestURI(), status, took);
            }
            MDC.clear();
        }
    }
}