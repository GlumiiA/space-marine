package ru.itmo.is.space_marine_backend.infrastructure;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Интерцептор для обработки fault injection параметров из заголовков HTTP.
 */
@Component
public class FaultInjectionFilter implements Filter {

    private static final String HEADER_FAIL_AFTER_FILE = "X-Fault-Injection-After-File";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;

        try {
            boolean failAfterFile = "true".equalsIgnoreCase(
                    httpRequest.getHeader(HEADER_FAIL_AFTER_FILE)
            );

            if (failAfterFile) {
                FaultInjectionContext.set(
                        new FaultInjectionContext.FaultInjectionConfig(failAfterFile)
                );
            }

            chain.doFilter(request, response);
        } finally {
            FaultInjectionContext.clear();
        }
    }
}
