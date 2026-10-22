package com.example.websocket_demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Serves server-rendered HTML pages (Thymeleaf). The page itself is public;
 * the data it loads is protected by the REST API (admin-only for the CMS).
 */
@Controller
public class PageController {

    @GetMapping("/cms")
    public String cms() {
        return "cms";
    }
}
