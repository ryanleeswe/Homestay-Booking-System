package com.example.Homestay_Booking_System.util.error;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import com.example.Homestay_Booking_System.domain.dto.RestResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

/** Bridges filter-chain failures to the same error handlers used by MVC. */
@Component
public class SecurityExceptionHandler implements AuthenticationEntryPoint, AccessDeniedHandler {
    private final GlobalException globalException;
    private final ObjectMapper objectMapper;

    public SecurityExceptionHandler(GlobalException globalException, ObjectMapper objectMapper) {
        this.globalException = globalException;
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException exception) throws IOException {
        if (!response.isCommitted()) {
            response.setHeader("WWW-Authenticate", "Bearer");
            write(response, globalException.handleAuthenticationException(exception));
        }
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
            AccessDeniedException exception) throws IOException {
        write(response, globalException.handleAccessDeniedException(exception));
    }

    private void write(HttpServletResponse response, ResponseEntity<RestResponse<Void>> result) throws IOException {
        if (response.isCommitted()) {
            return;
        }
        response.setStatus(result.getStatusCode().value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        result.getHeaders().forEach((name, values) -> values.forEach(value -> response.addHeader(name, value)));
        objectMapper.writeValue(response.getWriter(), result.getBody());
    }
}
