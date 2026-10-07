package com.example.Homestay_Booking_System.util;

import com.example.Homestay_Booking_System.domain.dto.RestResponse;
import com.example.Homestay_Booking_System.util.annotation.ApiMessage;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import tools.jackson.databind.ObjectMapper;

@RestControllerAdvice
public class ResponseBodyAdvice implements org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice<Object> {
    private final ObjectMapper objectMapper;

    public ResponseBodyAdvice(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType contentType,
            Class<? extends HttpMessageConverter<?>> converterType,
            ServerHttpRequest request, ServerHttpResponse response) {
        int status = response instanceof ServletServerHttpResponse servlet
                ? servlet.getServletResponse().getStatus() : 200;
        if (status == 204 || status == 205 || status == 304 || status < 200
                || request.getMethod() == HttpMethod.HEAD) {
            return null;
        }
        // Errors belong to GlobalException; never wrap an existing envelope twice.
        if (status >= 300 || body instanceof RestResponse<?>) {
            return body;
        }
        ApiMessage annotation = returnType.getMethodAnnotation(ApiMessage.class);
        RestResponse<Object> result = new RestResponse<>(status,
                annotation == null ? "Thành công" : annotation.value(), null, body);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        response.getHeaders().remove("Content-Length");
        // Converter selection occurs before this advice, including for String endpoints.
        if (StringHttpMessageConverter.class.isAssignableFrom(converterType)) {
            return objectMapper.writeValueAsString(result);
        }
        return result;
    }
}
