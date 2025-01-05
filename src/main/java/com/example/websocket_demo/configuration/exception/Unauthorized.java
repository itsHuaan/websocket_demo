package com.example.websocket_demo.configuration.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class Unauthorized implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException) throws IOException, ServletException {
        // String checkRequest = request.getRequestURI().split("/")[1]; //For exception
        // admin thymleaf
        // if(checkRequest.equals("admin")){
        // response.sendRedirect("/login");
        // return;
        // }
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        final Map<String, Object> body = new HashMap<>();
        body.put("timestamp", new Date());
        body.put("status", HttpServletResponse.SC_UNAUTHORIZED);
        if (authException.getMessage().equals("Bad credentials")) {
            body.put("error", "Bad credentials");
            body.put("message", "Invalid username or password");
        } else {
            body.put("error", "Unauthorized");
            body.put("message", "Token invalid");
        }
        body.put("path", request.getRequestURI());
        final ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(response.getOutputStream(), body);
    }
}
