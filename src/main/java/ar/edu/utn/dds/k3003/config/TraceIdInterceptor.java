package ar.edu.utn.dds.k3003.config;

import java.io.IOException;
import org.slf4j.MDC;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

/**
 * Agrega el header X-Trace-Id (con el traceId del MDC) a cada llamada saliente por RestTemplate,
 * asi el modulo que recibe reusa el mismo traceId.
 */
public class TraceIdInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(
            HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        String traceId = MDC.get("traceId");
        if (traceId != null) {
            request.getHeaders().set(RequestLoggingFilter.TRACE_ID_HEADER, traceId);
        }
        return execution.execute(request, body);
    }
}